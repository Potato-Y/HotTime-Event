package io.github.potato_y.hottimeevent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class HotTimeEvent extends JavaPlugin {
    private FileConfiguration eventConfig = null;
    private File eventConfigFile = null;

    @Override
    public void onEnable() {
        //플러그인 활성화
        getLogger().info("§bHotTime Event plugin on");
        this.saveDefaultConfig(); //기본 config

        //event.yml
        saveEventConfig();
        reloadEventConfig();

        //플러그인 활성화
        //getCommand("hottimeeventtest").setExecutor(new TestCommand());
        getCommand("giveall").setExecutor(new GiveAll());
        getCommand("htereload").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
                configReload();
                commandSender.sendMessage(getConfig().getString("format")+"Reload complete");
                return true;
            }
        });


        //메인 기능
        getCommand("eventlist").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
                sender.sendMessage(new Utility().colorCodeChange(getEventConfig().getString("EventList.defualtEvent.name")));

                return true;
            }
        });

        getCommand("eventtest").setExecutor(new CommandExecutor() { //설정한 이벤트 아이템이 정상적으로 생성되는지 확인하는 용도
            @Override
            public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
                ConfigurationSection section = getEventConfig().getConfigurationSection("EventList");
                for (String name : section.getKeys(false)) {
                    if (name.equals(strings[0])) {
                        Player p = (Player) sender;
                        ItemStack itemStack = itemCustom(name);
                        p.getInventory().addItem(itemStack);

                        return true;
                    }
                }

                return false;
            }
        });

        eventMonitering();
    }

    public void configReload() {
        this.reloadConfig();
        reloadEventConfig();
    }

    public void reloadEventConfig() {
        if (eventConfigFile == null) {
            eventConfigFile = new File(getDataFolder(), "event.yml");
        }
        eventConfig = YamlConfiguration.loadConfiguration(eventConfigFile);

        Reader defConfigStream = new InputStreamReader(this.getResource("event.yml"), StandardCharsets.UTF_8);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            eventConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getEventConfig() {
        if (eventConfig == null) {
            reloadEventConfig();
        }
        return eventConfig;
    }

    public void saveEventConfig() {
        if (eventConfig == null) {
            eventConfigFile = new File(getDataFolder(), "event.yml");
        }
        if (!eventConfigFile.exists()) {
            saveResource("event.yml", false);
        }
    }

    @Override
    public void onDisable() {
        //플러그인 비활성화
        getLogger().info("§bHotTime Event plugin end");
    }

    //메인 기능 모니터링
    static SimpleDateFormat timeFormat_loopType = new SimpleDateFormat("HH:mm");
    static SimpleDateFormat timeFormat_specificDayType = new SimpleDateFormat("yyyy-MM-dd");
    static String tempTime;
    static Date time;

    public void eventMonitering() {
        tempTime = "";
        HashMap<Integer, String> dayOfTheWeek = new HashMap<Integer, String>() {{
            put(1, "sun");
            put(2, "mon");
            put(3, "tue");
            put(4, "wed");
            put(5, "thu");
            put(6, "fri");
            put(7, "sat");
        }};

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                time = new Date();
                String nowTime = timeFormat_loopType.format(time);
                if (!tempTime.equals(nowTime)) { //초를 제외한 시간이 다르면 진행
                    //정보 가져오기
                    ConfigurationSection section = getEventConfig().getConfigurationSection("EventList");
                    for (String key : section.getKeys(false)) {  //이벤트 하나당 작동
                        try {
                            String eventType = getEventConfig().getString("EventList." + key + ".type");
                            if (eventType.equals("everyday")) { //loop 타입에 맞게 시간 검사
                                if (getEventConfig().getString("EventList." + key + ".time").equals(nowTime)) { //설정한 시간과 같으면 실행!
                                    eventStart(key);
                                }

                            } else if (eventType.equals("specific")) { //특정 날에 진행
                                String setDay = getEventConfig().getString("EventList." + key + ".date") + " " + getEventConfig().getString("EventList." + key + ".time");
                                if (setDay.equals(timeFormat_specificDayType.format(time) + " " + nowTime)) { //설정한 시간과 같은지 확인
                                    eventStart(key);
                                }
                            } else if (eventType.equals("week loop")) { //일주일에 지정한 요일에 진행
                                Calendar calendar = Calendar.getInstance();
                                String dayOfWeek = dayOfTheWeek.get(calendar.get(Calendar.DAY_OF_WEEK)); //오늘이 무슨 요일인지 지정
                                String setDate = getEventConfig().getString("EventList." + key + ".date"); //설정한 요일 가져오기

                                if (new Utility().textConversion(setDate, "a").contains(dayOfWeek)) { //오늘이 검색된다면
                                    if (getEventConfig().getString("EventList." + key + ".time").equals(nowTime)) { //설정한 시간과 같으면 실행!
                                        eventStart(key);
                                    }
                                }

                            } else {
                                getLogger().info("§4 " + key + "값이 정상인지 확인하세요.");
                            }
                        } catch (Exception e) {
                            getLogger().info("§4Plugin ERROR");
                        }
                    }
                    tempTime = timeFormat_loopType.format(time); //마지막으로 작동 시간 저장
                }
            }
        }, 0, 2 * 20);
    }

    public void eventStart(String eventName) { //이벤트 시작
        ItemStack itemStack = itemCustom(eventName);

        Bukkit.broadcastMessage(getConfig().getString("format") + "곧 \"" + getEventConfig().getString("EventList."+eventName+"name") + "\" 핫타임 아이템이 지급됩니다! 인벤토리를 미리 비워주세요!");

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(getConfig().getString("format") + "카운트 다운 시작!");
            }
        }, 20);

        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(getConfig().getString("format") + "10!");
            }
        }, 2 * 20);

        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(getConfig().getString("format") + "8!");
            }
        }, 4 * 20);

        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(getConfig().getString("format") + "6!");
            }
        }, 6 * 20);

        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(getConfig().getString("format") + "4!");
            }
        }, 8 * 20);

        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(getConfig().getString("format") + "3!");
            }
        }, 9 * 20);

        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(getConfig().getString("format") + "2!");
            }
        }, 10 * 20);

        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(getConfig().getString("format") + "1!");
            }
        }, 11 * 20);

        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                new GiveAll().giveAll(itemStack);
            }
        }, 12 * 20);
    }

    public ItemStack itemCustom(String eventName) { //설정한 아이템 생성
        try {
            String keyHeader = "EventList." + eventName + "."; //중복 내용 변수로 설정
            ItemStack itemStack = new ItemStack(Material.matchMaterial(getEventConfig().getString(keyHeader + "item"))); //유저가 원하는 아이템 설정
            ItemMeta itemMeta = itemStack.getItemMeta(); //메타 아이템 생성

            if (!getEventConfig().getString(keyHeader + "DisplayName").equals("")) {
                itemMeta.setDisplayName("§f" + new Utility().colorCodeChange(getEventConfig().getString(keyHeader + "DisplayName")));
            }

            ArrayList<String> lore = new ArrayList<>();
            for (String text : getEventConfig().getStringList(keyHeader + "lore")) {
                lore.add("§f" + new Utility().colorCodeChange(text));
            }
            itemMeta.setLore(lore);

            for (String text : getEventConfig().getStringList(keyHeader + "Enchant")) {
                try {
                    itemMeta.addEnchant(new Utility().getEnchantment(new Utility().textConversion(text.split(" ")[0], "A")), Integer.parseInt(text.split(" ")[1]), true);
                } catch (Exception e) {
                    getLogger().info("§4인첸트 옵션에 문제가 있습니다.");
                }
            }

            itemStack.setItemMeta(itemMeta);
            itemStack.setAmount(getEventConfig().getInt(keyHeader + "Amount"));

            return itemStack;
        } catch (Exception e) {
            getLogger().info("이벤트가 잘못 설정되어 있습니다.\n" + e);
        }
        return new ItemStack(Material.matchMaterial("air"));
    }
}

class Utility {
    public String colorCodeChange(String s) {
        return s.replace("&0", "§0")
                .replace("&1", "§1")
                .replace("&2", "§2")
                .replace("&3", "§3")
                .replace("&4", "§4")
                .replace("&5", "§5")
                .replace("&6", "§6")
                .replace("&7", "§7")
                .replace("&8", "§8")
                .replace("&9", "§9")
                .replace("&a", "§a")
                .replace("&b", "§b")
                .replace("&c", "§c")
                .replace("&d", "§d")
                .replace("&e", "§e")
                .replace("&f", "§f")
                .replace("&l", "§l")
                .replace("&m", "§m")
                .replace("&n", "§n")
                .replace("&o", "§o");
    }

    public Enchantment getEnchantment(String value) {
        switch (value) {
            case "ARROW_FIRE":
                return Enchantment.ARROW_FIRE;
            case "ARROW_DAMAGE":
                return Enchantment.ARROW_DAMAGE;
            case "ARROW_INFINITE":
                return Enchantment.ARROW_INFINITE;
            case "ARROW_KNOCKBACK":
                return Enchantment.ARROW_KNOCKBACK;
            case "BINDING_CURSE":
                return Enchantment.BINDING_CURSE;
            case "CHANNELING":
                return Enchantment.CHANNELING;
            case "DAMAGE_ALL":
                return Enchantment.DAMAGE_ALL;
            case "DAMAGE_ARTHROPODS":
                return Enchantment.DAMAGE_ARTHROPODS;
            case "DAMAGE_UNDEAD":
                return Enchantment.DAMAGE_UNDEAD;
            case "DEPTH_STRIDER":
                return Enchantment.DEPTH_STRIDER;
            case "DIG_SPEED":
                return Enchantment.DIG_SPEED;
            case "DURABILITY":
                return Enchantment.DURABILITY;
            case "FIRE_ASPECT":
                return Enchantment.FIRE_ASPECT;
            case "FROST_WALKER":
                return Enchantment.FROST_WALKER;
            case "IMPALING":
                return Enchantment.IMPALING;
            case "KNOCKBACK":
                return Enchantment.KNOCKBACK;
            case "LOOT_BONUS_BLOCKS":
                return Enchantment.LOOT_BONUS_BLOCKS;
            case "LOOT_BONUS_MOBS":
                return Enchantment.LOOT_BONUS_MOBS;
            case "LOYALTY":
                return Enchantment.LOYALTY;
            case "LUCK":
                return Enchantment.LUCK;
            case "LURE":
                return Enchantment.LURE;
            case "MENDING":
                return Enchantment.MENDING;
            case "MULTISHOT":
                return Enchantment.MULTISHOT;
            case "OXYGEN":
                return Enchantment.OXYGEN;
            case "PIERCING":
                return Enchantment.PIERCING;
            case "PROTECTION_ENVIRONMENTAL":
                return Enchantment.PROTECTION_ENVIRONMENTAL;
            case "PROTECTION_EXPLOSIONS":
                return Enchantment.PROTECTION_EXPLOSIONS;
            case "PROTECTION_FIRE":
                return Enchantment.PROTECTION_FIRE;
            case "PROTECTION_FALL":
                return Enchantment.PROTECTION_FALL;
            case "PROTECTION_PROJECTILE":
                return Enchantment.PROTECTION_PROJECTILE;
            case "QUICK_CHARGE":
                return Enchantment.QUICK_CHARGE;
            case "RIPTIDE":
                return Enchantment.RIPTIDE;
            case "SILK_TOUCH":
                return Enchantment.SILK_TOUCH;
            case "SOUL_SPEED":
                return Enchantment.SOUL_SPEED;
            case "SWEEPING_EDGE":
                return Enchantment.SWEEPING_EDGE;
            case "THORNS":
                return Enchantment.THORNS;
            case "VANISHING_CURSE":
                return Enchantment.VANISHING_CURSE;
            case "WATER_WORKER":
                return Enchantment.WATER_WORKER;
            default:
                return Enchantment.LUCK;
        }
    }

    public String textConversion(String text, String type) { //모두 대문자로 변환
        if (type.length() == 0) {
            return "NOT FOUND";
        }

        char temp; //임시 저장
        String outputText = ""; //return
        boolean typeTemp; //어느 타입으로 할지 결정 true=대문자로 변환, false 소문자로 변환

        if ((97 <= type.toCharArray()[0]) && (type.toCharArray()[0] <= 122)) {
            typeTemp = false;
        } else {
            typeTemp = true;
        }

        for (int i = 0; i < text.length(); i++) {
            temp = text.charAt(i);

            if (typeTemp == true) { //대문자로 변환 시키기
                if ((97 <= temp) && (temp <= 122)) {
                    outputText += String.valueOf(temp).toUpperCase();
                } else {
                    outputText += (char) temp;
                }
            } else if (typeTemp == false) {
                if ((65 <= temp) && (temp <= 90)) {
                    outputText += String.valueOf(temp).toLowerCase();
                } else {
                    outputText += (char) temp;
                }
            }


        }
        return outputText;
    }
}
