package org.dragonet.bukkit.dplus.data;

import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.dragonet.bukkit.dplus.DerpPlus;
import org.dragonet.bukkit.dplus.Lang;
import org.dragonet.bukkit.dplus.command.CreepySoundsCommand;
import org.dragonet.bukkit.dplus.task.CreepySoundsTask;

import java.util.Map;

/**
 * Created on 2017/11/13.
 */
public final class CreepySoundInstance {

    private final CreepySoundsCommand command;
    private final Player target;

    private BossBar bar;

    private boolean cancelled;

    private String componentName;
    private Sound[] component;
    private int currentDelaySeconds;
    private CreepySoundsTask task;

    public CreepySoundInstance(CreepySoundsCommand command, Player target) {
        this.command = command;
        this.target = target;
    }

    public void init() {
        bar = command.getPlugin().getServer().createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        command.getPlugin().getServer().getOnlinePlayers().stream().filter(p -> p.hasPermission(DerpPlus.GENERAL_PERMISSION)).forEach(p -> bar.addPlayer(p));

        task = new CreepySoundsTask(command.getPlugin(), this);
        prepareNext();
    }

    public void prepareNext() {
        if(cancelled) return;
        currentDelaySeconds = command.randomDelaySeconds();
        componentName = command.randomComponent();
        component = command.getComponent(componentName);
        bar.setTitle(Lang.build("creepy-sounds.bossbar", target.getName(), componentName));
        BukkitTask bukkitTask = command.getPlugin().getServer().getScheduler().runTaskTimer(command.getPlugin(), task, 0L, 20L);
        task.setTask(bukkitTask);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        if(task != null) task.cancel();
        cancelled = true;
        bar.removeAll();
    }

    public Player getTarget() {
        return target;
    }

    public String getComponentName() {
        return componentName;
    }

    public Sound[] getComponent() {
        return component;
    }

    public int getCurrentDelaySeconds() {
        return currentDelaySeconds;
    }

    public void updateProgress(int counted) {
        float left = currentDelaySeconds - counted;
        if(left < 0) left = 0f;
        float progress = left / currentDelaySeconds;
        if(progress > 1f) progress = 1f;
        bar.setProgress(progress);
    }
}
