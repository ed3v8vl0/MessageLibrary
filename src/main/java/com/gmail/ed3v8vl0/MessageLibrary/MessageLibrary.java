package com.gmail.ed3v8vl0.MessageLibrary;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class MessageLibrary extends JavaPlugin implements Listener {
    private RabbitManager rabbitManager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        this.rabbitManager = new RabbitManager(config.getString("host"), config.getInt("port"), config.getString("username"), config.getString("password"));
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        this.rabbitManager.closeAll();
    }

    @EventHandler
    public void onPluginDisableEvent(PluginDisableEvent event) {
        Plugin plugin = event.getPlugin();
        this.rabbitManager.close(plugin);
    }

    public RabbitManager getRabbitManager() { return this.rabbitManager; }
}
