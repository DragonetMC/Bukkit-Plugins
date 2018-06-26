package org.dragonet.bukkit.psbg.listeners;

import org.bukkit.WorldBorder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.dragonet.bukkit.psbg.PlayerStevesBattleGrounds;

/**
 * Created on 2017/11/29.
 */
public class StaticWorldListener implements Listener {

    private final PlayerStevesBattleGrounds plugin;

    public StaticWorldListener(PlayerStevesBattleGrounds plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onWorldLoad(WorldLoadEvent e){
        e.getWorld().setAutoSave(false);
        WorldBorder b = e.getWorld().getWorldBorder();
        b.setCenter(0d, 0d);
        b.setSize(3000000d);
        plugin.getLogger().info(String.format("Auto-save for world [" + e.getWorld().getName() + "] is prevented! "));
    }

    @EventHandler
    private void onChunkUnload(ChunkUnloadEvent e) {
        e.setSaveChunk(false);
    }

}
