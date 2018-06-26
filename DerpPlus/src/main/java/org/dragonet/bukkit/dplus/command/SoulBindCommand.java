package org.dragonet.bukkit.dplus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dragonet.bukkit.dplus.DerpPlus;
import org.dragonet.bukkit.dplus.Lang;
import org.dragonet.bukkit.dplus.listener.SoulBindListener;

import java.util.UUID;

/**
 * Created on 2017/11/13.
 */
public class SoulBindCommand extends DerpPlusCommandExecutor {


    private final SoulBindListener listener;

    public SoulBindCommand(DerpPlus plugin, SoulBindListener listener) {
        super(plugin);
        this.listener = listener;
    }

    @Override
    public boolean run(CommandSender sender, Command command, String s, String[] args) {
        if(!sender.hasPermission("derpplus.soulbind")) {
            Lang.sendMessage(sender, "messages.no-permission");
            return true;
        }
        Player playerA = plugin.getServer().getPlayer(args[0]);
        if(playerA == null) {
            Lang.sendMessage(sender, "soulbind.can-not-find", "playerA");
            return true;
        }
        if(args.length == 1) {
            UUID uuidB = listener.unbind(playerA);
            if(uuidB != null) {
                Lang.sendMessage(sender, "soulbind.unbind.success");
            } else {
                Lang.sendMessage(sender, "soulbind.unbind.failed", playerA.getName());
            }
        } else if (args.length == 2) {
            {
                Player dup = listener.getBinding(playerA);
                if(dup != null) {
                    Lang.sendMessage(sender, "soulbind.bind.duplicated", playerA.getName(), dup.getName());
                    return true;
                }
            }
            Player playerB = plugin.getServer().getPlayer(args[1]);
            if(playerB == null) {
                Lang.sendMessage(sender, "soulbind.bind.failed", args[1]);
                return true;
            }
            listener.bind(playerA, playerB);
            Lang.sendMessage(sender, "soulbind.bind.success");
        } else {
            Lang.sendMessageList(sender, "soulbind.help");
        }
        return true;
    }
}
