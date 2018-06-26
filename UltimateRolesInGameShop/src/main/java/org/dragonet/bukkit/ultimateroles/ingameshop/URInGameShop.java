package org.dragonet.bukkit.ultimateroles.ingameshop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.dragonet.bukkit.menuapi.ItemMenu;
import org.dragonet.bukkit.menuapi.ItemMenuInstance;
import org.dragonet.bukkit.menuapi.MenuAPIPlugin;
import org.dragonet.bukkit.ultimateroles.UltimateRolesPlugin;

import java.io.File;
import java.text.DateFormat;
import java.util.*;

/**
 * Created on 2017/11/16.
 */
public class URInGameShop extends JavaPlugin {

    private UltimateRolesPlugin ur;

    private MenuAPIPlugin menus;

    @Override
    public void onEnable() {
        saveResource("lang.yml", false);
        Lang.lang = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "lang.yml"));

        ur = (UltimateRolesPlugin) getServer().getPluginManager().getPlugin("UltimateRoles");
        menus = (MenuAPIPlugin) getServer().getPluginManager().getPlugin("MenuAPI");

        getLogger().info("Starting up UltimateRoles In-Game Shop (v" + getDescription().getVersion() + ")");
        getCommand("urshop").setExecutor(new ShopCommand(this));
    }

    public UltimateRolesPlugin getUR() {
        return ur;
    }

    public void openShopItemsWindow(Player player, int categoryId) {
        ur.queryAPI("/shop/all/categoryId/" + categoryId, Collections.emptyMap(), (result) -> {
            if(!URInGameShop.isResultOkay(player, result)) return;
            JsonArray categories = result.get("data").getAsJsonArray();
            ItemMenuInstance menu = new ItemMenuInstance(Lang.build("windows.items"), categories.size());
            int i = 0;
            for(JsonElement c : categories) {
                JsonObject obj = c.getAsJsonObject();
                String[] desc = obj.get("description").getAsString().replace("<br>", "<br />").replace("\0a", "").replace("<br />", "\n").split("\n");
                ArrayList<String> lore = new ArrayList<>();
                for(String d : desc) {
                    lore.add("\u00a73" + d);
                }
                lore.add(Lang.build("lores.buy-or-renew"));
                menu.setButton(i, Material.ENCHANTED_BOOK, "\u00a7b" + obj.get("name").getAsString(),
                        lore,
                        ((humanEntity, itemMenuInstance) ->
                            confirmBox(player, Lang.build("purchase.confirm", obj.get("name").getAsString()), () ->
                                purchaseItem(player, obj.get("id").getAsInt()))
                        ));
                i++;
            }
            getMenus().open(player, menu);
        });
    }

    public void purchaseItem(Player player, int itemId) {
        Lang.sendMessageList(player, "purchase.progress");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("uuid", player.getUniqueId().toString());
        parameters.put("itemId", Integer.toString(itemId));
        ur.queryAPI("/shop/purchase", parameters, (result) -> {
            String status = result.get("message").getAsString();
            player.sendMessage(status);
            if(status.equals("buy") || status.equals("renew")) {
                Lang.sendMessage(player, "purchase.success.message", Lang.build("purchase.actions." + status));
                Lang.sendMessage(player, "purchase.success.expire-time-notice",
                        DateFormat.getDateTimeInstance().format(new Date(result.get("endTime").getAsLong() * 1000L))
                );
            } else {
                Lang.sendMessage(player, "purchase.failed." + status);
            }
        });
    }

    public ItemMenu getMenus() {
        return menus.getMenus();
    }

    public void confirmBox(Player player, String title, Runnable action) {
        ItemMenuInstance box = new ItemMenuInstance(Lang.build("confirmation.title-prefix") + title, 9);
        box.setButton(2, Material.STAINED_CLAY, (short) 5, Lang.build("confirmation.option-yes"), Collections.emptyList(), ((humanEntity, itemMenuInstance) -> {
            player.closeInventory();
            action.run();
        }));
        box.setButton(6, Material.STAINED_CLAY, (short) 14, Lang.build("confirmation.option-no"), Collections.emptyList(), ((humanEntity, itemMenuInstance) -> {
            player.closeInventory();
            Lang.sendMessage(player, "confirmation.cancelled");
        }));
        getMenus().open(player, box);
    }

    public final static boolean isResultOkay(Player player, JsonObject result) {
        if(result == null) {
            Lang.sendMessage(player, "general.server-error");
            return false;
        }
        if(!result.get("status").getAsString().equals("success")) {
            Lang.sendMessage(player, "general.ur-error", result.get("status").getAsString());
            return false;
        }
        return true;
    }
}
