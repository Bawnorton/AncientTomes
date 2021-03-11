package com.bawnorton.ancienttomes.event;

import com.bawnorton.ancienttomes.AncientTomes;
import com.bawnorton.ancienttomes.item.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

import static com.bawnorton.ancienttomes.AncientTomes.instance;
import static com.bawnorton.ancienttomes.Matrix.entityMatrix;
import static com.bawnorton.ancienttomes.config.Config.dropRates;

public class EventManager implements Listener {

    double antiStack = 0;
    int cost = 0;

    public EventManager(AncientTomes plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        ItemStack[] ingredients;
        try {
            ingredients = event.getInventory().getMatrix();
        } catch (NullPointerException ignore) { return; }
        int ancientPageCount = 0;
        int paperCount = 0;
        int leatherCount = 0;
        Enchantment lockedEnchant = null;
        for (ItemStack ingredient : ingredients) {
            if (ingredient == null) continue;
            if (ingredient.getType() == Material.LEATHER) leatherCount++;
            else if (ingredient.getType() == Material.PAPER) {
                ItemMeta meta = ingredient.getItemMeta();
                assert meta != null;
                if(meta.getEnchants().isEmpty()) {
                    paperCount++;
                    continue;
                }
                if(lockedEnchant != null && !ingredient.containsEnchantment(lockedEnchant)) {
                    event.getInventory().setResult(null);
                    return;
                }
                lockedEnchant = (Enchantment) meta.getEnchants().keySet().toArray()[0];
                ancientPageCount++;
            }
        }
        if (leatherCount == 1 && paperCount == 3 && ancientPageCount == 0) event.getInventory().setResult(new ItemStack(Material.BOOK));
        else if (leatherCount == 1 && ancientPageCount == 3 && paperCount == 0) {
            ItemStack book = getBook(lockedEnchant);
            assert book != null;
            noStack(book);
            event.getInventory().setResult(book);
        }
    }

    @EventHandler
    public void onAnvilCraft(PrepareAnvilEvent event) {
        ItemStack[] components;
        try {
            components = event.getInventory().getContents();
        } catch (NullPointerException ignore) {
            return;
        }
        for (ItemStack component : components) {
            if (component == null) return;
        }
        try {
            if (components[1].getItemMeta().getDisplayName().contains("Ancient Tome")) {
                Enchantment enchantment = (Enchantment) components[1].getItemMeta().getEnchants().keySet().toArray()[0];
                Integer level = (Integer) components[1].getItemMeta().getEnchants().values().toArray()[0];
                ItemStack item = components[0];
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                if(!item.containsEnchantment(enchantment)) return;
                else if(level - 1 != item.getEnchantments().get(enchantment)) return;
                meta.addEnchant(enchantment, level, true);
                ItemStack newItem = new ItemStack(item.getType(), item.getAmount());
                newItem.setItemMeta(meta);
                event.setResult(newItem);
                String itemString = item.toString();
                cost = Integer.parseInt(itemString.substring(itemString.indexOf("repair-cost=") + 12, itemString.indexOf("}}")));
                instance.getServer().getScheduler().runTask(instance, () -> event.getInventory().setRepairCost(cost));
            }
        } catch (NullPointerException ignore) {}
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (inv instanceof AnvilInventory) {
            InventoryView view = event.getView();
            int rawSlot = event.getRawSlot();
            if (rawSlot == view.convertSlot(rawSlot)) {
                if (rawSlot == 2) {
                    Player player = (Player) event.getWhoClicked();
                    if(player.getLevel() >= cost) {
                        player.setLevel(player.getLevel() - cost);
                        event.setCurrentItem(inv.getItem(2));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(SpawnerSpawnEvent event) {
        event.getEntity().setMetadata("Spawner", new FixedMetadataValue(instance, true));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            if(!event.getEntity().hasMetadata("Spawner")) {
                addDrops(event, event.getEntityType());
            }
        }
    }


    private void addDrops(EntityDeathEvent event, EntityType type) {
        String name = entityMatrix.get(type);
        boolean bookDrop = false;
        for(String drop: dropRates.keySet()) {
            String key = drop.substring(0, drop.indexOf("."));
            if(key.equals(name)) {
                String enchantName = drop.substring(drop.indexOf(".") + 1);
                if(enchantName.equals("Book")) {
                    bookDrop = true;
                    continue;
                }
                double chance = Math.random();
                double rate;
                if(dropRates.get(drop) instanceof Integer) rate = new Double((Integer) dropRates.get(drop)) / 100;
                else rate = (double) dropRates.get(drop) / 100;
                if(bookDrop && chance <= rate) event.getDrops().add(getBook(enchantName));
                chance = Math.random();
                if(chance <= rate) event.getDrops().add(getPage(enchantName));
            }
        }
    }

    private void noStack(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        StringBuilder hidden = new StringBuilder();
        for (char c : String.valueOf(antiStack).toCharArray())
            hidden.append(ChatColor.COLOR_CHAR + "").append(c);
        antiStack += 0.01;
        List<String> lore = meta.getLore();
        assert lore != null;
        lore.set(0, lore.get(0) + hidden.toString());
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    private ItemStack getBook(String name) {
        return ItemManager.books.get(name);
    }

    private ItemStack getBook(Enchantment enchantment) {
        for(ItemStack item: ItemManager.books.values()) if(item.containsEnchantment(enchantment)) return item;
        return null;
    }

    private ItemStack getPage(String name) {
        return ItemManager.pages.get(name);
    }
}
