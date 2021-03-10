package io.github.potato_y.hottimeevent;

import io.github.potato_y.hottimeevent.Event.EventList;
import io.github.potato_y.hottimeevent.Event.EventMonitoring;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class HotTimeEvent extends JavaPlugin {
    private FileConfiguration eventConfig = null;
    private File eventConfigFile = null;

    @Override
    public void onEnable() {
        //플러그인 활성화
        getLogger().info("§aHotTime Event plugin on");
        this.saveDefaultConfig(); //기본 config

        //event.yml
        saveEventConfig();
        reloadEventConfig();

        //플러그인 활성화
        getCommand("hottimeeventtest").setExecutor(new TestCommand());
        getCommand("giveall").setExecutor(new GiveAll());
        getCommand("htereload").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
                configReload();
                return true;
            }
        });
        getCommand("eventlist").setExecutor(new EventList());
        new EventMonitoring();
    }

    public void configReload() {
        this.reloadConfig();
        reloadEventConfig();
        getLogger().info(getEventConfig().getString("EventList.defualtEvent.name"));

        getLogger().info(getConfig().getString("format") + "Reload ok");
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
        getLogger().info("§aHotTime Event plugin end");
    }
}
