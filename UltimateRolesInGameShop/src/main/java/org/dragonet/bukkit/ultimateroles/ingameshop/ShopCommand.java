package org.dragonet.bukkit.ultimateroles.ingameshop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dragonet.bukkit.menuapi.ItemMenuInstance;

import java.util.Collections;

/**
 * Created on 2017/11/16.
 */
public class ShopCommand implements CommandExecutor {

    public final URInGameShop plugin;

    public ShopCommand(URInGameShop plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!Player.class.isAssignableFrom(sender.getClass())) return true;
        final Player player = (Player) sender;

        if(args.length > 0) {
            int categoryId = Integer.parseInt(args[0]);
            plugin.openShopItemsWindow(player, categoryId);
            return true;
        }

        plugin.getUR().queryAPI("/shop/categories", Collections.emptyMap(), (result) -> {
            if(!URInGameShop.isResultOkay(player, result)) return;
            JsonArray categories = result.get("data").getAsJsonArray();
            ItemMenuInstance menu = new ItemMenuInstance(Lang.build("windows.categories"), categories.size());
            int i = 0;
            for(JsonElement c : categories) {
                menu.setButton(i, Material.ENCHANTED_BOOK, "\u00a7b" + c.getAsJsonObject().get("name").getAsString(),
                        Lang.getStringList("lores.choose-category"),
                        ((humanEntity, itemMenuInstance) -> {
                            humanEntity.closeInventory();

                            plugin.openShopItemsWindow(player, c.getAsJsonObject().get("id").getAsInt());
                            Lang.sendMessage(sender, "general.loading");
                        }));
                i++;
            }
            plugin.getMenus().open(player, menu);
        });

        Lang.sendMessage(sender, "general.loading");
        return true;
    }

}
