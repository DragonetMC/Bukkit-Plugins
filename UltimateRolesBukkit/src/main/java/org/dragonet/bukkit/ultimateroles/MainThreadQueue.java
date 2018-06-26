package org.dragonet.bukkit.ultimateroles;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * process things on the main thread
 * Created on 2017/11/16.
 */
public class MainThreadQueue implements Runnable {

    private final UltimateRolesPlugin plugin;

    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

    public MainThreadQueue(UltimateRolesPlugin plugin) {
        this.plugin = plugin;
    }

    public void put(Runnable thing) {
        queue.add(thing);
    }

    @Override
    public void run() {
        while(queue.size() > 0) {
            queue.poll().run();
        }
    }
}
