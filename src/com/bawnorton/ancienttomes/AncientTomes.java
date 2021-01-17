package com.bawnorton.ancienttomes;

import com.bawnorton.ancienttomes.config.Config;
import com.bawnorton.ancienttomes.event.EventManager;
import com.bawnorton.ancienttomes.item.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class AncientTomes extends JavaPlugin {

    public static AncientTomes instance;

    @Override
    public void onEnable() {
        instance = this;
        new EventManager(this);
        ItemManager.init();
        Config.init();
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[AncientTomes] Enabled");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[AncientTomes] Disabled");
    }
}
