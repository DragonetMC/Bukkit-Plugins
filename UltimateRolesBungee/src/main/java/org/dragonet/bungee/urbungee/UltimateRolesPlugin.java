package org.dragonet.bungee.urbungee;

import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Created on 2017/9/27.
 */
public class UltimateRolesPlugin extends Plugin implements Listener {

    @Override
    public void onEnable() {
        if(getProxy().getConfig().isOnlineMode()) {
            getLogger().warning("You are in online-mode, this plugin is UNNECESSARY. ");
            getLogger().warning("This plugin will not working. ");
            return;
        }
        getProxy().getPluginManager().registerListener(this, this);
    }

    @EventHandler
    public void onPlayerJoin(LoginEvent e) {
        getLogger().info("Transforming player unique ID to UltimateRoles universal unique ID. ");
        e.getConnection().setUniqueId(UUID.nameUUIDFromBytes(e.getConnection().getName().toLowerCase().getBytes(StandardCharsets.UTF_8)));
    }
}
