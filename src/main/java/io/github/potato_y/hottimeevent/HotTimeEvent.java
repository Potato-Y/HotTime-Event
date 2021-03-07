package io.github.potato_y.hottimeevent;

import org.bukkit.plugin.java.JavaPlugin;

public class HotTimeEvent extends JavaPlugin {
    @Override
    public void onEnable(){
        //플러그인 활성화
        getLogger().info("HotTime event on");
    }

    @Override
    public void onDisable(){
        //플러그인 비활성화
        getLogger().info("Jong Plugin end");
    }
}
