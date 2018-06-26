package org.dragonet.bukkit.psbg.tasks;

import org.bukkit.WorldBorder;
import org.dragonet.bukkit.psbg.Battles;
import org.dragonet.bukkit.psbg.PlayerStevesBattleGrounds;
import org.dragonet.bukkit.psbg.PlayerTagMetadata;
import org.dragonet.bukkit.psbg.utils.Lang;

/**
 * Created on 2017/12/2.
 */
public class ShrinkBorderTask extends ManagedTask {

    private final PlayerStevesBattleGrounds plugin;
    private final Battles battles;

    private int times;
    private long interval;
    private int amount;
    private int shrinkTime;

    public ShrinkBorderTask(PlayerStevesBattleGrounds plugin, Battles battles) {
        this.plugin = plugin;
        this.battles = battles;
    }

    @Override
    public void start() {
        times = plugin.getConfig().getInt("battles.safe-zone.shrink.times");
        interval = plugin.getConfig().getInt("battles.safe-zone.shrink.interval");
        amount = plugin.getConfig().getInt("battles.safe-zone.shrink.amount");
        shrinkTime = plugin.getConfig().getInt("battles.safe-zone.shrink.shrinkTime");
        setTask(plugin.getServer().getScheduler().runTaskTimer(plugin, this, interval, interval));
    }

    @Override
    public void run() {
        plugin.getServer().getOnlinePlayers().forEach(p ->
            Lang.sendMessage(p, "messages.game.shrinking")
        );
        times--;
        WorldBorder wb = battles.getWorld().getWorldBorder();
        double newSize = wb.getSize() - amount;
        if(newSize > 0) {
            wb.setSize(newSize, shrinkTime);
        }

        if(times <= 0) cancel();
    }
}
