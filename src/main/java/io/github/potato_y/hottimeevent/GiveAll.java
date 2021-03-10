package io.github.potato_y.hottimeevent;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

public class GiveAll implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        String format = Bukkit.getPluginManager().getPlugin("HotTimeEvent").getConfig().getString("format");

        if (sender instanceof Player) { //플레이어야 명령어 실행 가능
            Bukkit.broadcastMessage(format + "곧 아이템이 지급됩니다! 인벤토리를 비워주세요!");

            ItemStack tempItem = ((Player) sender).getInventory().getItemInMainHand(); //유저가 들고 있는 아이템 정보 가져오기
            ItemStack giveItem = new ItemStack(tempItem); //유저가 가지고 있던 아이템 복사
            int value = 0; //아이템 갯수

            if (strings.length >= 1) { //strings 값이 있는지 확인
                if (strings[0].equals("now")) { //딜레이 없이 바로 지급 하고자 한다면, 아니라면 바로 일반 명령어로 넘어감
                    if (strings.length == 1) { //값이 없으면 기본 값 1
                        value = 1;
                    } else {
                        value = Integer.parseInt(strings[1]);
                    }
                    giveItem.setAmount(value); //유저가 입력한 아이텝 지금 갯수를 등록
                    giveAll(giveItem);

                    return true; //다음으로 넘어가지 않도록 return
                }
            }

            if (strings.length < 1) { //갯수 값이 없으면 기본 값 1
                value = 1;
            } else {
                value = Integer.parseInt(strings[0]);
            }

            giveItem.setAmount(value);

            //여기서 부턴 딜레이 후 아이템 지급
            BukkitScheduler scheduler = Bukkit.getPluginManager().getPlugin("HotTimeEvent").getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("HotTimeEvent"), new Runnable() {
                @Override
                public void run() {
                    Bukkit.broadcastMessage(format + 5 + "초 후 지급!");

                    BukkitScheduler scheduler = Bukkit.getPluginManager().getPlugin("HotTimeEvent").getServer().getScheduler();
                    scheduler.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("HotTimeEvent"), new Runnable() {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage(format + 4 + "초 후 지급!");

                            BukkitScheduler scheduler = Bukkit.getPluginManager().getPlugin("HotTimeEvent").getServer().getScheduler();
                            scheduler.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("HotTimeEvent"), new Runnable() {
                                @Override
                                public void run() {
                                    Bukkit.broadcastMessage(format + 3 + "초 후 지급!");

                                    BukkitScheduler scheduler = Bukkit.getPluginManager().getPlugin("HotTimeEvent").getServer().getScheduler();
                                    scheduler.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("HotTimeEvent"), new Runnable() {
                                        @Override
                                        public void run() {
                                            Bukkit.broadcastMessage(format + 2 + "초 후 지급!");

                                            BukkitScheduler scheduler = Bukkit.getPluginManager().getPlugin("HotTimeEvent").getServer().getScheduler();
                                            scheduler.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("HotTimeEvent"), new Runnable() {
                                                @Override
                                                public void run() {
                                                    Bukkit.broadcastMessage(format + 1 + "초 후 지급!");

                                                    BukkitScheduler scheduler = Bukkit.getPluginManager().getPlugin("HotTimeEvent").getServer().getScheduler();
                                                    scheduler.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("HotTimeEvent"), new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            giveAll(giveItem);
                                                        }
                                                    }, 20L);
                                                }
                                            }, 20L);
                                        }
                                    }, 20L);
                                }
                            }, 20L);
                        }
                    }, 20L);
                }
            }, 20L);


            return true;
        } else if (sender instanceof CommandSender) {
            sender.sendMessage(format + "플레이어가 아닙니다.");
            return true;
        }
        return false;
    }

    public void giveAll(ItemStack itemStack) {
        String format = Bukkit.getPluginManager().getPlugin("HotTimeEvent").getConfig().getString("format");
        Bukkit.broadcastMessage(format + "지급이 시작됩니다!");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().addItem(itemStack);
            player.sendMessage(format + player.getName() + "님 인벤토리로 아이템이 지급 되었습니다.");
        }

        Bukkit.broadcastMessage(format + "지급이 완료되었습니다!");
    }

}
