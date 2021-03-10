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
import java.util.ArrayList;
import java.util.Date;

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
                return true;
            }
        });


        //메인 기능
        getCommand("eventlist").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
                sender.sendMessage(getEventConfig().getString("EventList.defualtEvent.name"));

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

        getLogger().info("§bReload ok");
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
                            if (eventType.equals("loop")) { //loop 타입에 맞게 시간 검사
                                if (getEventConfig().getString("EventList." + key + ".time").equals(nowTime)) { //설정한 시간과 같으면 실행!
                                    eventStart(key);
                                }

                            } else if (eventType.equals("specific")) {
                                String setDay = getEventConfig().getString("EventList." + key + ".date") + " " + getEventConfig().getString("EventList." + key + ".time");
                                if (setDay.equals(timeFormat_specificDayType.format(time) + " " + nowTime)) { //설정한 시간과 같은지 확인
                                    eventStart(key);
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

        Bukkit.broadcastMessage(getConfig().getString("format") + "곧 \"" + eventName + "\" 핫타임 아이템이 지급됩니다! 인벤토리를 미리 비워주세요!");

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
                    itemMeta.addEnchant(new Utility().getEnchantment(text.split(" ")[0]), Integer.parseInt(text.split(" ")[1]), true);
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
            case "arrow_fire":
                return Enchantment.ARROW_FIRE;
            case "ARROW_DAMAGE":
            case "arrow_damage":
                return Enchantment.ARROW_DAMAGE;
            case "ARROW_INFINITE":
            case "arrow_infinite":
                return Enchantment.ARROW_INFINITE;
            case "ARROW_KNOCKBACK":
            case "arrow_knockback":
                return Enchantment.ARROW_KNOCKBACK;
            case "BINDING_CURSE":
            case "binding_curse":
                return Enchantment.BINDING_CURSE;
            case "CHANNELING":
            case "channeling":
                return Enchantment.CHANNELING;
            case "DAMAGE_ALL":
            case "damage_all":
                return Enchantment.DAMAGE_ALL;
            case "DAMAGE_ARTHROPODS":
            case "damage_arthropods":
                return Enchantment.DAMAGE_ARTHROPODS;
            case "DAMAGE_UNDEAD":
            case "damage_undead":
                return Enchantment.DAMAGE_UNDEAD;
            case "DEPTH_STRIDER":
            case "depth_strider":
                return Enchantment.DEPTH_STRIDER;
            case "DIG_SPEED":
            case "dig_speed":
                return Enchantment.DIG_SPEED;
            case "DURABILITY":
            case "durability":
                return Enchantment.DURABILITY;
            case "FIRE_ASPECT":
            case "fire_aspect":
                return Enchantment.FIRE_ASPECT;
            case "FROST_WALKER":
            case "frost_walker":
                return Enchantment.FROST_WALKER;
            case "IMPALING":
            case "impaling":
                return Enchantment.IMPALING;
            case "KNOCKBACK":
            case "knockback":
                return Enchantment.KNOCKBACK;
            case "LOOT_BONUS_BLOCKS":
            case "loot_bonus_blocks":
                return Enchantment.LOOT_BONUS_BLOCKS;
            case "LOOT_BONUS_MOBS":
            case "loot_bonus_mobs":
                return Enchantment.LOOT_BONUS_MOBS;
            case "LOYALTY":
            case "loyalty":
                return Enchantment.LOYALTY;
            case "LUCK":
            case "luck":
                return Enchantment.LUCK;
            case "LURE":
            case "lure":
                return Enchantment.LURE;
            case "MENDING":
            case "mending":
                return Enchantment.MENDING;
            case "MULTISHOT":
            case "multishot":
                return Enchantment.MULTISHOT;
            case "OXYGEN":
            case "oxygen":
                return Enchantment.OXYGEN;
            case "PIERCING":
            case "piercing":
                return Enchantment.PIERCING;
            case "PROTECTION_ENVIRONMENTAL":
            case "protection_environmental":
                return Enchantment.PROTECTION_ENVIRONMENTAL;
            case "PROTECTION_EXPLOSIONS":
            case "protection_explosions":
                return Enchantment.PROTECTION_EXPLOSIONS;
            case "PROTECTION_FIRE":
            case "protection_fire":
                return Enchantment.PROTECTION_FIRE;
            case "PROTECTION_FALL":
            case "protection_fall":
                return Enchantment.PROTECTION_FALL;
            case "PROTECTION_PROJECTILE":
            case "protection_projectile":
                return Enchantment.PROTECTION_PROJECTILE;
            case "QUICK_CHARGE":
            case "quick_charge":
                return Enchantment.QUICK_CHARGE;
            case "RIPTIDE":
            case "riptide":
                return Enchantment.RIPTIDE;
            case "SILK_TOUCH":
            case "silk_touch":
                return Enchantment.SILK_TOUCH;
            case "SOUL_SPEED":
            case "soul_speed":
                return Enchantment.SOUL_SPEED;
            case "SWEEPING_EDGE":
            case "sweeping_edge":
                return Enchantment.SWEEPING_EDGE;
            case "THORNS":
            case "thorns":
                return Enchantment.THORNS;
            case "VANISHING_CURSE":
            case "vanishing_curse":
                return Enchantment.VANISHING_CURSE;
            case "WATER_WORKER":
            case "water_worker":
                return Enchantment.WATER_WORKER;
            default:
                return Enchantment.LUCK;
        }
    }
}
