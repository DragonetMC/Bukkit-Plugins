package org.dragonet.bukkit.utoolkit;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;

/**
 * Created on 2017/8/21.
 */
public class UToolkit extends JavaPlugin implements Listener {

    public final static String LORE_PREFIX = "\u00a70UTOOLKIT:";
    // public final static String LORE_SEPARATOR = "\u00a6";

    private YamlConfiguration config;

    private ItemMenu menus;

    @Override
    public YamlConfiguration getConfig() {
        return config;
    }

    public ItemMenu getMenus() {
        return menus;
    }

    @Override
    public void onEnable() {
        saveResource("config.yml", false);
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        menus = new ItemMenu(this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new InventoryLocker(this), this);
        getServer().getPluginManager().registerEvents(new ToolkitExpAdder(this), this);
        getServer().getPluginManager().registerEvents(menus, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        ItemStack first = e.getPlayer().getInventory().getItem(0);
        if(first == null || readToolExp(first) == -1) {
            getLogger().info("Initializing toolkit for player <" + e.getPlayer().getName() + ">. ");
            initializeToolkit(e.getPlayer());
            return;
        }
    }


    public static void initializeToolkit(Player player) {
        ItemStack[] toolkit = new ItemStack[5];
        toolkit[0] = new ItemStack(Material.WOOD_SWORD);
        toolkit[1] = new ItemStack(Material.WOOD_AXE);
        toolkit[2] = new ItemStack(Material.WOOD_PICKAXE);
        toolkit[3] = new ItemStack(Material.WOOD_SPADE);
        toolkit[4] = new ItemStack(Material.WOOD_HOE);
        final String defaultLore = LORE_PREFIX + "0";
        final String defaultHelpText = "\u00a76Hold it and press \u00a7lQ \u00a76to upgrade it! ";
        final String defaultExpDispaly = "\u00a7e0\u00a7bexp";
        for (int i = 0; i < 5; i++) {
            ItemStack item = toolkit[i];
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("\u00a7b" + item.getType().name().split("_")[1] + "\u00a7e (Toolkit)");
            meta.setLore(Arrays.asList(defaultExpDispaly, defaultHelpText, defaultLore));
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
            player.getInventory().setItem(i, item);
        }
    }

    public static int readToolExp(ItemStack item) {
        if(item == null || item.getType().equals(Material.AIR)) return -1;
        if(!item.hasItemMeta()) return -1;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) {
            return -1;
        }
        for (String l : meta.getLore()) {
            if (l.startsWith(LORE_PREFIX)) {
                try {
                    return Integer.parseInt(l.substring(LORE_PREFIX.length()));
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    public static ItemStack setToolExp(ItemStack item, int exp) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("\u00a7b" + item.getType().name().split("_")[1] + "\u00a7e (Toolkit) \u00a78@ \u00a77" + exp + "\u00a78exp");
        meta.setLore(Arrays.asList("\u00a7e" + exp + "\u00a7bexp", "\u00a76Hold it and press \u00a7lQ \u00a76to upgrade it! ", LORE_PREFIX + exp));
        item.setItemMeta(meta);
        return item;
    }
}
