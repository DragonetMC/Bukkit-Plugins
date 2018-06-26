package org.dragonet.bukkit.ultimateroles;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.lucko.luckperms.api.Group;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;
import org.bukkit.entity.Player;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created on 2017/9/15.
 */
public class RoleLoader implements Runnable {

    private final UltimateRolesPlugin plugin;
    private final Player player;
    private final User user;

    public RoleLoader(UltimateRolesPlugin plugin, Player player, User user) {
        this.plugin = plugin;
        this.player = player;
        this.user = user;
    }

    @Override
    public void run() {
        try {
            String url = plugin.getConfig().getString("api.url");
            url += "?key=" + plugin.getConfig().getString("api.key");
            url += "&server=" + URLEncoder.encode(plugin.getConfig().getString("server.name"), "utf-8");
            url += "&group=" + URLEncoder.encode(
                    StringArray.implode(",", plugin.getConfig().getStringList("server.group").toArray(new String[0])),
                    "utf-8");
            url += "&username=" + URLEncoder.encode(player.getName(), "utf-8");
            url += "&uuid=" + URLEncoder.encode(player.getUniqueId().toString(), "utf-8");
            String result = HttpRequest.sendGet(url);
            if(result == null) throw new Exception("HTTP GET Error! ");
            JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
            process(obj);
        }catch (Exception e){
            e.printStackTrace();
            player.sendMessage(plugin.getLang().getString("error-loading"));
        }
    }

    private void process(JsonObject obj) throws Exception {
        if(!obj.get("status").getAsString().equalsIgnoreCase("success")) {
            throw new Exception("API Error: " + obj.get("message").getAsString());
        }

        JsonObject player_status = obj.getAsJsonObject("player");
        String user_change = player_status.get("change").getAsString();
        if(user_change.equalsIgnoreCase("NEW")) {
            player.sendMessage(String.format(plugin.getLang().getString("profile-created"), player_status.get("id").getAsInt()));
        } else if(user_change.equalsIgnoreCase("USERNAME")) {
            player.sendMessage(plugin.getLang().getString("update-username"));
        }
        JsonArray instances = obj.getAsJsonArray("data");
        instances.forEach((e) -> {
            JsonObject i = e.getAsJsonObject();
            String name = i.get("name").getAsString();
            String type = i.get("type").getAsString();
            long endTime = i.get("endTime").getAsLong() == -1L ? -1L : (i.get("endTime").getAsLong() * 1000L);
            if(type.equalsIgnoreCase("default")) {
                player.sendMessage(String.format(plugin.getLang().getString("role-notification.default"), name));
            } else if(type.equalsIgnoreCase("limited")) {
                player.sendMessage(String.format(plugin.getLang().getString("role-notification.limited"), name, DateFormat.getInstance().format(new Date(endTime))));
            } else if(type.equalsIgnoreCase("permanent")) {
                player.sendMessage(String.format(plugin.getLang().getString("role-notification.permanent"), name));
            }
            JsonArray items = i.getAsJsonArray("items");
            items.forEach((jsonElement -> {
                JsonObject item = jsonElement.getAsJsonObject();
                Node n = readRoleNodes(item.get("itemType").getAsInt(), item.get("value").getAsString(), endTime);
                if(n != null) {
                    user.setPermissionUnchecked(n);
                }
            }));
        });
        user.refreshPermissions();
        player.sendMessage(plugin.getLang().getString("done-loading"));
    }

    private Node readRoleNodes(int type, String value, long endTime) {
            Node.Builder builder;
            boolean negative = false;
            if(type <= 1 && value.startsWith("-")) {
                negative = true;
                value = value.substring(1);
            }
            if(type < 2) { // 0 or 1
                if(value.indexOf("|") > 0) {
                    String restriction = value.substring(0, value.indexOf("|")).toLowerCase();
                    value = value.substring(value.indexOf("|")+1);
                    List<String> server_belongs;
                    if(restriction.startsWith("g:")) {
                        server_belongs = Arrays.asList(plugin.getConfig().getString("server.name"));
                        restriction = restriction.substring(2);
                    } else {
                        server_belongs = plugin.getConfig().getStringList("server.group");
                    }
                    if(!server_belongs.contains(restriction)) return null;
                }
            }
            switch(type) {
                case 0:
                    builder = plugin.getPermsApi().getNodeFactory().newBuilder(value);
                    builder.setNegated(negative);
                    break;
                case 1:
                    Group g = plugin.getPermsApi().getGroup(value);
                    if(g == null) return null;
                    builder = plugin.getPermsApi().getNodeFactory().makeGroupNode(g);
                    builder.setNegated(negative);
                    break;
                case 2:
                case 3:
                    int i = value.indexOf(':');
                    if(i < 0) {
                        plugin.getLogger().severe(plugin.getLang().getString("prefix-suffix-format-error"));
                        return null;
                    }
                    int priority = Integer.parseInt(value.substring(0, i));
                    String data = value.substring(i+1);
                    builder = (type == 2 ? plugin.getPermsApi().getNodeFactory().makePrefixNode(priority, data) :
                            plugin.getPermsApi().getNodeFactory().makeSuffixNode(priority, data));
                    break;
                default:
                    return null;
            }
            if(endTime != -1L) {
                builder.setExpiry(endTime);
            }
            builder.setOverride(true);
            return builder.build();
    }
}
