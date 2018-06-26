package org.dragonet.bukkit.psbg.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created on 2017/12/2.
 */
public final class InventoryUtils {

    /**
     * fully clear the inventory, even armors
     * @param inv
     */
    public static void clearInventory(Inventory inv) {
        inv.setContents(new ItemStack[inv.getSize()]);
    }

}
