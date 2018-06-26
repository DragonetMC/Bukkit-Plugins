package org.dragonet.bukkit.dplus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.dragonet.bukkit.dplus.DerpPlus;
import org.dragonet.bukkit.dplus.Lang;

/**
 * Created on 2017/11/13.
 */
public abstract class DerpPlusCommandExecutor implements CommandExecutor {

    protected final DerpPlus plugin;

    public DerpPlusCommandExecutor(DerpPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!commandSender.hasPermission("derpplus.use")) {
            Lang.sendMessage(commandSender, "messages.no-general-permission");
            return true;
        }
        return run(commandSender, command, s, strings);
    }

    public abstract boolean run(CommandSender commandSender, Command command, String s, String[] strings);

    public DerpPlus getPlugin() {
        return plugin;
    }
}
