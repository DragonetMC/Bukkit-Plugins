package org.dragonet.bukkit.utoolkit;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2017/8/23.
 */
public class ItemMenu implements Listener{

    public final static String MENU_IDENTIFIER_START = "\u00a70[MENU";
    public final static String MENU_IDENTIFIER_END = "]";


    private final UToolkit plugin;

    private final Map<String, ItemMenuInstance> instances = new HashMap<>();

    public ItemMenu(UToolkit plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, ItemMenuInstance instance) {
        instances.put(instance.getId(), instance);
        player.openInventory(instance.getInventory());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        String title = e.getInventory().getTitle();
        if(title.contains(MENU_IDENTIFIER_START)) {
            int start = title.indexOf(MENU_IDENTIFIER_START) + MENU_IDENTIFIER_START.length();
            int end = title.indexOf(MENU_IDENTIFIER_END, start + 1) ;
            String id = title.substring(start, end);
            // e.getPlayer().sendMessage("Closing menu = " + id);
            if(e.getInventory().getViewers().size() <= 1) {
                // e.getPlayer().sendMessage("Removing menu = " + id);
                instances.remove(id);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String title = e.getInventory().getTitle();
        if (!title.contains(MENU_IDENTIFIER_START)) return;
        int start = title.indexOf(MENU_IDENTIFIER_START) + MENU_IDENTIFIER_START.length();
        int end = title.indexOf(MENU_IDENTIFIER_END, start + 1);
        String id = title.substring(start, end);
        if(!instances.containsKey(id)) return;
        ItemMenuInstance instance = instances.get(id);
        if(instance.getHandlers().containsKey(e.getRawSlot())) {
            MenuItemHandler handler = instance.getHandlers().get(e.getRawSlot());
            if(handler != null) {
                handler.onClick(e.getWhoClicked(), instance);
            }
        }
        e.setCancelled(true);
    }

    public interface MenuItemHandler {
        void onClick(HumanEntity player, ItemMenuInstance menu);
    }
}
