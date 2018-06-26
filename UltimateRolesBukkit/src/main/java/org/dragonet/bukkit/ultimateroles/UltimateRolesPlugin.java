package org.dragonet.bukkit.ultimateroles;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.dragonet.bukkit.ultimateroles.commands.ClearPermsCommand;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2017/7/23.
 */
public class UltimateRolesPlugin extends JavaPlugin implements Listener {


    private LuckPermsApi permsApi;

    private YamlConfiguration config;
    private YamlConfiguration lang;

    private ExecutorService threads;

    private MainThreadQueue queue;

    @Override
    public void onEnable() {
        try {
            loadConfig();
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().severe("Failed to load the config file! ");
            getServer().shutdown();
            return;
        }

        permsApi = LuckPerms.getApi();
        threads = Executors.newFixedThreadPool(config.getInt("api.threads", 8));

        // register events
        getServer().getPluginManager().registerEvents(this, this);

        getCommand("clearperms").setExecutor(new ClearPermsCommand(this));

        queue = new MainThreadQueue(this);
        getServer().getScheduler().runTaskTimer(this, queue, 0L, 20L);
    }

    private void loadConfig() throws Exception{
        saveResource("config.yml", false);
        saveResource("lang.yml", false);
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        lang = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "lang.yml"));
    }

    public MainThreadQueue getQueue() {
        return queue;
    }

    @EventHandler
    private void handleUserAdded(PlayerJoinEvent e) {
        User u = permsApi.getUser(e.getPlayer().getUniqueId());
        Player p = e.getPlayer();
        if(p == null) return; // not likely to happen but who knows
        p.sendMessage(getLang().getString("loading"));
        u.clearNodes();
        u.clearParents();
        u.clearMeta();
        u.refreshPermissions();
        threads.execute(new RoleLoader(this, p, u));
    }

    public LuckPermsApi getPermsApi() {
        return permsApi;
    }

    @Override
    public YamlConfiguration getConfig() {
        return config;
    }

    public YamlConfiguration getLang() {
        return lang;
    }

    public ExecutorService getThreads() {
        return threads;
    }

    /**
     * Calls UltimateRoles Server API and runs calls callback on the Spigot/Bukkit main thread.
     * @param endpoint Endpoint starts with /
     * @param parameters Map of HTTP GET parameters
     * @param callback Callback to run
     */
    public void queryAPI(String endpoint, Map<String, String> parameters, CallbackWithParameter<JsonObject> callback) {
        threads.execute(() -> {
            try {
                String url = getConfig().getString("api.url") + endpoint;
                Map<String, String> map = new HashMap<>();
                List<String> all = new ArrayList<>();
                map.putAll(parameters);
                map.put("key", getConfig().getString("api.key"));
                map.entrySet().forEach((ent) -> {
                    try {
                        all.add(ent.getKey() + "=" + URLEncoder.encode(ent.getValue(), "utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                });
                url += "?" + StringArray.implode("&", all.toArray(new String[0]));
                String result = HttpRequest.sendGet(url);
                if (result == null) throw new Exception("HTTP GET Error! ");
                JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
                queue.put(() -> callback.call(obj));
            } catch (Exception e) {
                e.printStackTrace();
                queue.put(() -> callback.call(null));
            }
        });
    }
}
