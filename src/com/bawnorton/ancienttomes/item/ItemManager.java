package com.bawnorton.ancienttomes.item;

import org.bukkit.inventory.ItemStack;

import java.util.Hashtable;

public class ItemManager {

    public static Hashtable<String, ItemStack> books = new Hashtable<>();
    public static Hashtable<String, ItemStack> pages = new Hashtable<>();

    public static void init() {
        createBooks();
        createPages();
    }

    private static void createBooks() {
        Hashtable<String, Object[]> enchantmentMatrix = AncientEnchants.getEnchantmentMatrix();
        for (String key: enchantmentMatrix.keySet()) {
            AncientEnchants enchant = new AncientEnchants(key);
            books.put(key, enchant.getBook());
        }
    }

    private static void createPages() {
        Hashtable<String, Object[]> enchantmentMatrix = AncientEnchants.getEnchantmentMatrix();
        for (String key: enchantmentMatrix.keySet()) {
            AncientEnchants enchant = new AncientEnchants(key);
            pages.put(key, enchant.getPage());
        }
    }
}