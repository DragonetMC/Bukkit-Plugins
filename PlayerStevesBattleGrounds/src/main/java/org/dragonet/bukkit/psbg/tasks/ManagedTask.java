package org.dragonet.bukkit.psbg.tasks;

import org.bukkit.scheduler.BukkitTask;

/**
 * Created on 2017/12/2.
 */
public abstract class ManagedTask implements Runnable {

    private BukkitTask task;

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    public abstract void start();

    public void cancel() {
        if(task != null) {
            task.cancel();
            task = null;
        }
    }
}
