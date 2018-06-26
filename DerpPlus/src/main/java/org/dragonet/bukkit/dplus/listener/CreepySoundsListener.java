package org.dragonet.bukkit.dplus.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.dragonet.bukkit.dplus.DerpPlus;
import org.dragonet.bukkit.dplus.Lang;
import org.dragonet.bukkit.dplus.command.CreepySoundsCommand;

/**
 * Created on 2017/11/13.
 */
public class CreepySoundsListener implements CloseableListener {

    private final DerpPlus plugin;
    private final CreepySoundsCommand command;

    public CreepySoundsListener(DerpPlus plugin, CreepySoundsCommand command) {
        this.plugin = plugin;
        this.command = command;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if(command.isEnabledFor(e.getPlayer())) {
            command.disableFor(e.getPlayer());
            plugin.broadcastToAdmins(Lang.build("creepy-sounds.update.target-quit", e.getPlayer().getName()));
        }
    }

    @Override
    public void close() {
        command.onDisable();
    }
}
