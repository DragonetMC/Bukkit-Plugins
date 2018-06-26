package org.dragonet.bukkit.dplus;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.dragonet.bukkit.dplus.command.CreepySoundsCommand;
import org.dragonet.bukkit.dplus.command.DerpGodCommand;
import org.dragonet.bukkit.dplus.command.SoulBindCommand;
import org.dragonet.bukkit.dplus.listener.CloseableListener;
import org.dragonet.bukkit.dplus.listener.CreepySoundsListener;
import org.dragonet.bukkit.dplus.listener.SoulBindListener;
import org.dragonet.bukkit.dplus.utils.ResourceUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2017/11/7.
 */
public class DerpPlus extends JavaPlugin {

    public final static String GENERAL_PERMISSION = "derpplus.use";

    private YamlConfiguration config;
    private File dirConfigurations;

    private Map<String, CloseableListener> listeners = new HashMap<>();
    private Map<String, CommandExecutor> commands = new HashMap<>();

    @Override
    public YamlConfiguration getConfig() {
        return config;
    }

    public File getFunctionsConfigurationsFolder() {
        return dirConfigurations;
    }

    @Override
    public void onEnable() {
        saveResource("lang.yml", false);
        saveResource("config.yml", false);
        Lang.lang = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "lang.yml"));
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));

        dirConfigurations = new File(getDataFolder(), "functions");
        dirConfigurations.mkdirs();

        // soul bind
        {
            SoulBindListener listener = new SoulBindListener(this);
            SoulBindCommand command = new SoulBindCommand(this, listener);
            getCommand("soulbind").setExecutor(command);
            getServer().getPluginManager().registerEvents(listener, this);
            listeners.put("soulbind", listener);
            commands.put("soulbind", command);
        }

        // creepy sounds system
        {
            CreepySoundsCommand command = new CreepySoundsCommand(this);
            CreepySoundsListener listener = new CreepySoundsListener(this, command);
            ResourceUtil.saveResource("/functions/creepy-sounds.yml", new File(dirConfigurations, "creepy-sounds.yml"), false);
            command.reloadConfiguration();
            getCommand("creepysounds").setExecutor(command);
            getServer().getPluginManager().registerEvents(listener, this);
            listeners.put("creepysounds", listener);
            commands.put("creepysounds", command);
        }

        // derp god
        {
            DerpGodCommand command = new DerpGodCommand(this);
            commands.put("derpgod", command);
        }
    }

    @Override
    public void onDisable() {
        listeners.values().forEach((l) -> l.close());
        listeners.clear();
        commands.clear();
    }

    public void broadcastToAdmins(String s) {
        getServer().getOnlinePlayers().stream().filter(p -> p.hasPermission(GENERAL_PERMISSION)).forEach(p -> {
            p.sendMessage(s);
        });
    }
}
