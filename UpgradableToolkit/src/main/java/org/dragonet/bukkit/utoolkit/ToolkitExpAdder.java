package org.dragonet.bukkit.utoolkit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created on 2017/8/22.
 */
public class ToolkitExpAdder implements Listener {
    public final UToolkit plugin;

    public ToolkitExpAdder(UToolkit plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        int exp = UToolkit.readToolExp(item);
        if(!item.getType().name().endsWith("_PICKAXE") && !item.getType().name().endsWith("_AXE") && !item.getType().name().endsWith("_SPADE")) return;
        if(exp >= 0) {
            // it is a toolkit item, add exp to it
            int add = plugin.getConfig().getInt("exp." + item.getType().name().split("_")[1].toLowerCase(), 50);
            exp += add;
            e.getPlayer().getInventory().setItemInMainHand(UToolkit.setToolExp(item, exp));
        }
    }

    @EventHandler
    public void onSwordUse(EntityDamageByEntityEvent e){
        if(!Player.class.isAssignableFrom(e.getDamager().getClass())) return;
        Player p = (Player) e.getDamager();
        ItemStack item = p.getInventory().getItemInMainHand();
        if(!item.getType().name().endsWith("_SWORD")) return;
        int exp = UToolkit.readToolExp(item);
        if(exp >= 0) {
            int add = plugin.getConfig().getInt("exp.sword", 50);
            exp += add;
            p.getInventory().setItemInMainHand(UToolkit.setToolExp(item, exp));
        } else{
            ItemMenuInstance i = new ItemMenuInstance("STUPID MENU", 18);
            i.setButton(1, Material.ACACIA_FENCE, "SHIT", (clicker, menuInstance) -> clicker.sendMessage("FOOLISH! "));
            plugin.getMenus().open(p, i);
        }
    }

    @EventHandler
    public void onHoeUse(PlayerInteractEvent e) {
        if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Material type = e.getClickedBlock().getType();
        if(!type.equals(Material.DIRT) && !type.equals(Material.GRASS) && !type.equals(Material.GRASS_PATH)) return;
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        int exp = UToolkit.readToolExp(item);
        if(!item.getType().name().endsWith("_HOE")) return;
        if(exp >= 0) {
            int add = plugin.getConfig().getInt("exp.hoe", 50);
            exp += add;
            e.getPlayer().getInventory().setItemInMainHand(UToolkit.setToolExp(item, exp));
        }
    }

}
