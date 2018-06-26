package org.dragonet.bukkit.dplus.task;

import org.bukkit.scheduler.BukkitTask;
import org.dragonet.bukkit.dplus.DerpPlus;

/**
 * Created on 2017/11/13.
 */
public abstract class ManagedTask implements Runnable {

    private final DerpPlus plugin;
    private BukkitTask task;

    public ManagedTask(DerpPlus plugin) {
        this.plugin = plugin;
    }

    public BukkitTask getTask() {
        return task;
    }

    public void setTask(BukkitTask task) {
        if(this.task != null) this.task.cancel();
        this.task = task;
    }

    public DerpPlus getPlugin() {
        return plugin;
    }

    public void cancel() {
        if(task != null) task.cancel();
    }
}
