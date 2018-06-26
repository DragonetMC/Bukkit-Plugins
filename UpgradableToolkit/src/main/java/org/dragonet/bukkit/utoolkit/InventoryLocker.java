package org.dragonet.bukkit.utoolkit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * Created on 2017/8/22.
 */
public class InventoryLocker implements Listener {

    public final UToolkit plugin;

    public InventoryLocker(UToolkit plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.setKeepInventory(true);
        Location loc = e.getEntity().getLocation();
        ItemStack[] contents = e.getEntity().getInventory().getContents();
        for (int i = 5; i < contents.length; i++) { // drop except for first 4 items
            if(contents[i] == null || contents[i].getType().equals(Material.AIR)) continue;
            e.getEntity().getWorld().dropItemNaturally(loc, contents[i]);
            e.getEntity().getInventory().setItem(i, null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWindowClick(InventoryClickEvent e) {
        if(e.getSlot() < 5) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropEvent(PlayerDropItemEvent e) {
        if(e.getPlayer().getInventory().getHeldItemSlot() < 5) {
            ItemStack item = e.getItemDrop().getItemStack();
            ItemMenuInstance menu = new ItemMenuInstance("\u00a70\u00a7lUpgrade for " + item.getType().name().replace('_', ' '), 9*3);
            String current_material = item.getType().name().split("_")[0]; // WOOD / IRON / GOLD / DIAMOND
            String tool_type = item.getType().name().split("_")[1];
            String next_material = null;
            if(current_material.equals("WOOD")) {
                next_material = "STONE";
            } else if(current_material.equals("STONE")) {
                next_material = "IRON";
            } else if(current_material.equals("IRON")) {
                next_material = "GOLD";
            } else if(current_material.equals("GOLD")) {
                next_material = "DIAMOND";
            }
            String upgradeMessage;
            if(next_material == null) {
                upgradeMessage = "\u00a7bYou're at the highest level of tool material. ";
            } else {
                int exp_curr = UToolkit.readToolExp(item);
                int exp_needed = plugin.getConfig().getInt("level-up." + tool_type.toLowerCase() + "." + current_material.toLowerCase());
                upgradeMessage = "\u00a77You have \u00a7e" + exp_curr + "\u00a77exp , " + exp_needed + "exp needed to upgrade. ";
            }
            if(next_material != null) {
                final String f_next_material = next_material;
                menu.setButton(12, Material.valueOf(next_material + "_" + tool_type), "\u00a76upgrade to " + next_material.toLowerCase(),
                        Arrays.asList(upgradeMessage, "\u00a77Enchantments will be preserved. "), (p, m) -> {
                            int exp_curr = UToolkit.readToolExp(item);
                            int exp_needed = plugin.getConfig().getInt("level-up." + tool_type.toLowerCase() + "." + current_material.toLowerCase());
                            if(exp_curr < exp_needed) {
                                e.getPlayer().closeInventory();
                                e.getPlayer().sendMessage("\u00a7cYou still need " + (exp_needed - exp_curr) + "exp to do that! ");
                            } else {
                                int exp_left = exp_curr - exp_needed;
                                item.setType(Material.valueOf(f_next_material + "_" + tool_type));
                                UToolkit.setToolExp(item, exp_left);
                                e.getPlayer().getInventory().setItem(e.getPlayer().getInventory().getHeldItemSlot(), item);
                                e.getPlayer().closeInventory();
                            }
                        });
                menu.setButton(14, Material.ENCHANTED_BOOK, "\u00a76upgrade enchantments", (p, m) -> {
                    // e.getPlayer().closeInventory();
                    openEnchantmentShop(e.getPlayer(), e.getPlayer().getInventory().getHeldItemSlot(), e.getItemDrop().getItemStack());
                });
            } else {
                menu.setButton(13, Material.ENCHANTED_BOOK, "\u00a76upgrade enchantments", (p, m) -> {
                    // e.getPlayer().closeInventory();
                    openEnchantmentShop(e.getPlayer(), e.getPlayer().getInventory().getHeldItemSlot(), e.getItemDrop().getItemStack());
                });
            }
            plugin.getMenus().open(e.getPlayer(), menu);
            e.setCancelled(true);
        }
    }

    private void openEnchantmentShop(Player player, int slot, ItemStack tool) {
        String tool_type = tool.getType().name().split("_")[1].toLowerCase();
        Map<Enchantment, Integer> current_enchants = tool.getItemMeta().getEnchants();
        int currentExp = UToolkit.readToolExp(tool);

        ConfigurationSection costConfig = plugin.getConfig().getConfigurationSection("enchantment-cost." + tool_type);
        Set<String> items = costConfig.getKeys(false);
        ItemMenuInstance menu = new ItemMenuInstance("\u00a72\u00a7lEnchantment shop for " + tool_type, items.size());
        int i = 0;
        for(String enchantment_name : items){
            String name = costConfig.getString(enchantment_name + ".name");
            double base_cost = costConfig.getDouble(enchantment_name + ".base");
            double multiplier = costConfig.getDouble(enchantment_name + ".multiplier");
            Enchantment enchantment = Enchantment.getByName(enchantment_name);
            int currentLevel = current_enchants.containsKey(enchantment) ? current_enchants.get(enchantment).intValue() : 0;
            int maxLevel = costConfig.getInt(enchantment_name + ".max-level");
            int costNeeded = (int)(base_cost + (base_cost * multiplier * currentLevel));
            menu.setButton(i, currentExp >= costNeeded ? Material.ENCHANTED_BOOK : Material.BOOK,
                    (currentExp >= costNeeded ? "\u00a7a" : "\u00a7c") + name,
                    Arrays.asList("\u00a77costs " + costNeeded + "exp", "\u00a77max level is " + maxLevel),
                    (p, m) -> {
                        if(currentLevel >= maxLevel) {
                            p.sendMessage("\u00a7b\u00a7lMax level exceeded! ");
                            return;
                        }
                        if(currentExp < costNeeded) {
                            p.sendMessage("\u00a7cYou still need " + (costNeeded - currentExp) + "exp to do that! ");
                        } else {
                            p.closeInventory();
                            ItemMeta meta = tool.getItemMeta();
                            meta.addEnchant(enchantment, currentLevel + 1, true);
                            tool.setItemMeta(meta);
                            int exp_left = currentExp - costNeeded;
                            UToolkit.setToolExp(tool, exp_left);
                            p.getInventory().setItemInMainHand(tool);
                            p.sendMessage("\u00a7aUpgrade success! ");
                        }
                    });

            i++;
        }
        plugin.getMenus().open(player, menu);
    }

    /*
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDamage(PlayerItemDamageEvent e) {
        if(UToolkit.readToolExp(e.getItem()) != -1) {
            e.setCancelled(true);
        }
    }
    */
}
