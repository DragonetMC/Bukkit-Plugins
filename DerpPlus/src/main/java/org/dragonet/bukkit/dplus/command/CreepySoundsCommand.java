package org.dragonet.bukkit.dplus.command;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.dragonet.bukkit.dplus.DerpPlus;
import org.dragonet.bukkit.dplus.Lang;
import org.dragonet.bukkit.dplus.data.CreepySoundInstance;

import java.io.File;
import java.util.*;

/**
 * Created on 2017/11/13.
 */
public class CreepySoundsCommand extends DerpPlusCommandExecutor {

    private final static Random random = new Random(System.currentTimeMillis());

    private YamlConfiguration config;

    private int delayBase;
    private int delayMaxRandom;
    private List<String> componentNames = new LinkedList<>();
    private Map<String, Sound[]> components = new HashMap<>();

    private Map<UUID, CreepySoundInstance> instances = new HashMap<>();

    public CreepySoundsCommand(DerpPlus plugin) {
        super(plugin);
    }

    public void onDisable() {
        instances.clear();
        componentNames.clear();
        components.clear();
    }

    public void reloadConfiguration() {
        config = YamlConfiguration.loadConfiguration(new File(plugin.getFunctionsConfigurationsFolder(), "creepy-sounds.yml"));
        delayBase = config.getInt("delay-base");
        delayMaxRandom = config.getInt("max-random-delay");
        componentNames.clear();
        components.clear();
        for(String name : config.getConfigurationSection("components").getKeys(false)) {
            List<String> sound_values = config.getStringList("components." + name);
            Sound[] sounds = new Sound[sound_values.size()];
            for(int i = 0; i < sounds.length; i++) {
                sounds[i] = Sound.valueOf(sound_values.get(i));
            }
            componentNames.add(name);
            components.put(name, sounds);
        }
    }

    @Override
    public boolean run(CommandSender sender, Command command, String s, String[] args) {
        if(!sender.hasPermission("derpplus.creepysounds")) {
            Lang.sendMessage(sender, "messages.no-permission");
            return true;
        }
        if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfiguration();
            Lang.sendMessage(sender, "creepy-sounds.reload-complete");
            return true;
        }
        if(args.length != 2) {
            Lang.sendMessageList(sender, "creepy-sounds.help");
            return true;
        }
        Player target = plugin.getServer().getPlayer(args[1]);
        if(target == null ){
            Lang.sendMessage(sender, "player-not-found");
            return true;
        }
        if(args[0].equalsIgnoreCase("start")) {
            if(instances.containsKey(target.getUniqueId())) {
                Lang.sendMessage(sender, "creepy-sounds.start.failed", target.getName());
                return true;
            }
            if(components.size() <= 0) {
                Lang.sendMessage(sender, "creepy-sounds.start.no-definition");
                return true;
            }
            enableFor(target);
            Lang.sendMessage(sender, "creepy-sounds.start.success", target.getName());
        } else if(args[0].equalsIgnoreCase("stop")) {
            if(!instances.containsKey(target.getUniqueId())) {
                Lang.sendMessage(sender, "creepy-sounds.stop.failed", target.getName());
                return true;
            }
            disableFor(target);
            Lang.sendMessage(sender, "creepysounds.stop.success", target.getName());
        } else {
            Lang.sendMessageList(sender, "creepy-sounds.help");
        }
        return true;
    }

    public void enableFor(Player target) {
        if(isEnabledFor(target)) return;
        CreepySoundInstance i = new CreepySoundInstance(this, target);
        instances.put(target.getUniqueId(), i);
        i.init();
    }

    public void disableFor(Player target) {
        if(!isEnabledFor(target)) return;
        CreepySoundInstance i = instances.remove(target.getUniqueId());
        i.cancel();
    }

    public boolean isEnabledFor(Player p) {
        return instances.containsKey(p.getUniqueId());
    }

    public Sound[] getComponent(String name) {
        return components.get(name);
    }

    public String randomComponent() {
        return componentNames.get(random.nextInt(componentNames.size()));
    }

    public int randomDelaySeconds() {
        return delayBase + (random.nextInt(delayMaxRandom));
    }
}
