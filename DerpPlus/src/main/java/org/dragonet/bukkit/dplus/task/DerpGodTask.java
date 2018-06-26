package org.dragonet.bukkit.dplus.task;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.dragonet.bukkit.dplus.DerpPlus;
import org.dragonet.bukkit.dplus.Lang;

import java.util.Random;

/**
 * Created on 2017/11/13.
 */
public class DerpGodTask extends ManagedTask {

    private final Random random = new Random();
    private final Player target;

    private final long startTime;
    private final long derpTime;

    public DerpGodTask(DerpPlus plugin, Player target, long derpTime) {
        super(plugin);
        this.target = target;
        startTime = System.currentTimeMillis();
        this.derpTime = derpTime;
    }

    @Override
    public void run() {
        if(target.isDead() || !target.isValid() || !target.isOnline()) {
            cancel();
            getPlugin().broadcastToAdmins(Lang.build("derp-god.quit", target.getName()));
            return;
        }
        if(System.currentTimeMillis() >= startTime + derpTime) {
            cancel();
            getPlugin().broadcastToAdmins(Lang.build("derp-god.ended", target.getName()));
            return;
        }
        float yaw = random.nextFloat() * 360f;
        float pitch = random.nextFloat() * 360f;
        Location loc = target.getLocation();
        loc.setYaw(yaw);
        loc.setPitch(pitch);
        target.teleport(loc);
    }
}
