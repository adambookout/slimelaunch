package com.slimelaunch;

import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Hello, SpigotMC!");

        // Create default config file
        this.saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new LaunchpadListeners(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("See you again, SpigotMC!");
    }
}