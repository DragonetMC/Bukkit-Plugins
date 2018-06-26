package org.dragonet.bukkit.dplus.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.dragonet.bukkit.dplus.DerpPlus;
import org.dragonet.bukkit.dplus.Lang;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created on 2017/11/13.
 */
public class SoulBindListener implements CloseableListener {

    private final DerpPlus plugin;

    // A, B
    private Map<UUID, UUID> bindingsA = new HashMap<>();
    private Map<UUID, UUID> bindingsB = new HashMap<>();

    public SoulBindListener(DerpPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void close() {
        bindingsA.clear();
        bindingsB.clear();
    }

    public DerpPlus getPlugin() {
        return plugin;
    }

    public void bind(Player playerA, Player playerB) {
        bindingsA.put(playerA.getUniqueId(), playerB.getUniqueId());
        bindingsB.put(playerB.getUniqueId(), playerA.getUniqueId());
    }

    public UUID unbind(Player playerA) {
        if(bindingsA.containsKey(playerA.getUniqueId())) {
            return bindingsB.remove(bindingsA.remove(playerA.getUniqueId()));
        }
        return null;
    }

    public Player getBinding(Player playerA) {
        if(bindingsA.containsKey(playerA.getUniqueId())) return plugin.getServer().getPlayer(bindingsA.get(playerA.getUniqueId()));
        return null;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if(!bindingsB.containsKey(e.getEntity().getUniqueId())) return;
        double calculated = e.getFinalDamage();
        Player playerA = plugin.getServer().getPlayer(bindingsB.get(e.getEntity().getUniqueId()));
        playerA.damage(e.getFinalDamage(), e.getDamager());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamageByBlock(EntityDamageByBlockEvent e) {
        if(!bindingsB.containsKey(e.getEntity().getUniqueId())) return;
        double calculated = e.getFinalDamage();
        Player playerA = plugin.getServer().getPlayer(bindingsB.get(e.getEntity().getUniqueId()));
        playerA.setLastDamageCause(e);
        playerA.damage(e.getFinalDamage());
    }

    @EventHandler()
    public void onPlayerLeave(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if(bindingsA.containsKey(uuid)) {
            Player playerA = plugin.getServer().getPlayer(uuid);
            Player playerB = plugin.getServer().getPlayer(bindingsA.get(uuid));
            plugin.broadcastToAdmins(Lang.build("soulbind.update.player-left", playerA.getName(), playerA.getName(), playerB.getName()));
            bindingsB.remove(bindingsA.remove(uuid));
        }
        if(bindingsB.containsKey(uuid)) {
            Player playerA = plugin.getServer().getPlayer(bindingsA.get(uuid));
            Player playerB = plugin.getServer().getPlayer(uuid);
            plugin.broadcastToAdmins(Lang.build("soulbind.update.player-left", playerB.getName(), playerA.getName(), playerB.getName()));
            bindingsA.remove(bindingsB.remove(uuid));
        }
    }
}
