package me.liamgiraldo.liteParkour;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class TimeConfig {
    private static File file;
    private static FileConfiguration config;
    public static void setup() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("LiteParkour").getDataFolder(), "times.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get() {
        return config;
    }

    public static void save() {
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
    }
}
