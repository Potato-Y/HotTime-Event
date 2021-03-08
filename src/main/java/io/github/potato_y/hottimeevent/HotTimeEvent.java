package io.github.potato_y.hottimeevent;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class HotTimeEvent extends JavaPlugin {

    @Override
    public void onEnable() {
        //플러그인 활성화
        getLogger().info("§eHotTime event on");

        //플러그인 활성화
        getCommand("hottimeeventtest").setExecutor(new TestCommand());
    }

    @Override
    public void onDisable() {
        //플러그인 비활성화
        getLogger().info("Jong Plugin end");
    }
}
