package me.liamgiraldo.liteParkour;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class LiteParkour extends JavaPlugin {

    private ParkourController parkourController;
    private ParkourCreator parkourCreator;
    @Override
    public void onEnable() {
        System.out.println("LiteParkour enabled");

        saveDefaultConfig();
        TimeConfig.setup();
        TimeConfig.get().options().copyDefaults(true);
        TimeConfig.save();
        // Plugin startup logic

        //set command for the parkour creator

        parkourController = new ParkourController(this);
        parkourCreator = new ParkourCreator(this, parkourController);

        getCommand("parkour").setExecutor(parkourCreator);

        registerEvents();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(parkourCreator, this);
        getServer().getPluginManager().registerEvents(parkourController, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public FileConfiguration getTimeConfig() {
        return TimeConfig.get();
    }
}
