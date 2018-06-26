package org.dragonet.bukkit.dplus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.dragonet.bukkit.dplus.DerpPlus;
import org.dragonet.bukkit.dplus.Lang;
import org.dragonet.bukkit.dplus.task.DerpGodTask;

/**
 * Created on 2017/11/13.
 */
public class DerpGodCommand extends DerpPlusCommandExecutor {

    public DerpGodCommand(DerpPlus plugin) {
        super(plugin);
    }

    @Override
    public boolean run(CommandSender sender, Command command, String s, String[] args) {
        if(!sender.hasPermission("derpplus.derpgod")) {
            Lang.sendMessage(sender, "messages.no-permission");
            return true;
        }
        if(args.length != 2) {
            Lang.sendMessageList(sender, "derp-god.help");
            return true;
        }
        Player target = plugin.getServer().getPlayer(args[0]);
        if(target == null) {
            Lang.sendMessage(sender, "player-not-found");
            return true;
        }
        int seconds = Integer.parseInt(args[1]);
        DerpGodTask task = new DerpGodTask(plugin, target, seconds * 1000L);
        BukkitTask bukkitTask = plugin.getServer().getScheduler().runTaskTimer(plugin, task, 0L, 10L);
        task.setTask(bukkitTask);
        plugin.broadcastToAdmins(Lang.build("derp-god.initiated", target.getName(), seconds));
        return true;
    }
}
