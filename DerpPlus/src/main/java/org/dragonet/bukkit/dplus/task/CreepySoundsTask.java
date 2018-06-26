package org.dragonet.bukkit.dplus.task;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.dragonet.bukkit.dplus.DerpPlus;
import org.dragonet.bukkit.dplus.data.CreepySoundInstance;

import java.util.Random;

/**
 * Created on 2017/11/13.
 */
public class CreepySoundsTask extends ManagedTask {

    private final CreepySoundInstance instance;
    private final Random random = new Random(System.currentTimeMillis());

    private int counter = 0;

    public CreepySoundsTask(DerpPlus plugin, CreepySoundInstance instance) {
        super(plugin);
        this.instance = instance;
    }

    @Override
    public void run() {
        if(instance.isCancelled()) {
            cancel();
            return;
        }

        if(counter >= instance.getCurrentDelaySeconds()) {
            cancel();
            execute();
            counter = 0;
            instance.prepareNext();
        } else {
            counter ++;
            instance.updateProgress(counter);
        }
    }

    private void execute() {
        Location loc = instance.getTarget().getLocation();
        double x = random.nextInt(5) * (random.nextBoolean() ? 1d : -1d);
        double z = random.nextInt(5) * (random.nextBoolean() ? 1d : -1d);
        loc.add(x, 0d, z);
        for(Sound s : instance.getComponent()) {
            instance.getTarget().getWorld().playSound(loc, s, 1f, 1f);
        }

        instance.prepareNext();
    }
}
