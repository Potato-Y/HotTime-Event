package io.github.potato_y.hottimeevent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        Bukkit.broadcastMessage("[ §cHotTime Event§f ] 곧 모든 유저에게 테스트 아이템을 지급합니다. 아이템 창을 비워주세요!"); //모든 유저에게 메시지 전송
        ItemStack item = new ItemStack(Material.matchMaterial("paper")); //종이 아이템 생성
        ItemMeta itemMeta = item.getItemMeta(); //아이템 메타 데이터 생성 및 미리 생성한 아이템 메타 데이터 가져오기기
        itemMeta.setDisplayName("§cHotTime Event!"); //아이템 이름 변경
        item.setItemMeta(itemMeta); //변경한 메타 아이템 입혀주기

        delayTimeInformation(5, 1);
        delayTimeInformation(4, 2);
        delayTimeInformation(3, 3);
        delayTimeInformation(2, 4);
        delayTimeInformation(1, 5);

        BukkitScheduler scheduler = Bukkit.getPluginManager().getPlugin("HotTimeEvent").getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("HotTimeEvent"), new Runnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) { //모든 플레이어에게
                    p.getInventory().addItem(item); //플레이어에게 아이템 부여
                    p.sendMessage("[ §cHotTime Event§f ] " + p.getName() + " 님 아이템이 도착하였습니다."); //도착 메시지
                }
            }
        }, 6 * 20L);
        return true; //정상 작동
    }

    public void delayTimeInformation(int time, int delay) {
        BukkitScheduler scheduler = Bukkit.getPluginManager().getPlugin("HotTimeEvent").getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("HotTimeEvent"), new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("[ §cHotTime Event§f ] " + time + "초 후!");
            }
        }, delay * 20L);
    }
}

