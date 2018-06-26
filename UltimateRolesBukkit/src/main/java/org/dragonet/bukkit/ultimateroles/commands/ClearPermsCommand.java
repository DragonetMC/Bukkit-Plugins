package org.dragonet.bukkit.ultimateroles.commands;

import me.lucko.luckperms.api.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dragonet.bukkit.ultimateroles.UltimateRolesPlugin;

/**
 * Created on 2017/7/23.
 */
public class ClearPermsCommand implements CommandExecutor {

    private final UltimateRolesPlugin plugin;

    public ClearPermsCommand(UltimateRolesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!Player.class.isAssignableFrom(commandSender.getClass())) return false;
        User u = plugin.getPermsApi().getUser(((Player)commandSender).getUniqueId());
        if(u != null) {
            u.clearNodes();
            u.clearParents();
            u.clearMeta();
            u.refreshPermissions();
            commandSender.sendMessage("Congrats! You found this command and now you are a retard! ");
            return true;
        }else{
            return false;
        }
    }
}
