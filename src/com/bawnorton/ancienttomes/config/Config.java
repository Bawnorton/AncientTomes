package com.bawnorton.ancienttomes.config;

import com.bawnorton.ancienttomes.AncientTomes;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

public class Config {
    private static final AncientTomes instance = AncientTomes.instance;
    private static FileConfiguration config;
    public static Hashtable<String, Object> dropRates = new Hashtable<>();

    public static void init() {
        File file = new File(instance.getDataFolder(), "config.yml");
        if (!file.exists()) {
            instance.getLogger().info(ChatColor.YELLOW + "config.yml not found");
            instance.getLogger().info(ChatColor.YELLOW + "creating config");
            instance.saveDefaultConfig();
        } else {
            instance.getLogger().info(ChatColor.GREEN + "config.yml found");
        }
        config = instance.getConfig();
        load();
    }
    private static void load() {
        Map<String, Object> values = config.getValues(true);
        for(String key: values.keySet()) {
            if(!(values.get(key) instanceof MemorySection)) dropRates.put(key, values.get(key));
        }
    }
    private static void save() {
        instance.saveConfig();
    }
    private static void relaod() {
        instance.reloadConfig();
    }

}
