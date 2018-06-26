package org.dragonet.bukkit.psbg.tasks;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.dragonet.bukkit.psbg.GamePhase;
import org.dragonet.bukkit.psbg.PlayerStevesBattleGrounds;
import org.dragonet.bukkit.psbg.utils.Lang;

/**
 * a Bukkit task and a listener
 * Created on 2017/11/30.
 */
public class CountDownProcessor implements Runnable, Listener {

    private final PlayerStevesBattleGrounds plugin;

    private BukkitTask task;

    private BossBar barLine1;
    private BossBar barLine2;
    public int remainingSeconds;

    public CountDownProcessor(PlayerStevesBattleGrounds plugin) {
        this.plugin = plugin;
    }

    public void init() {
        barLine1 = plugin.getServer().createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        barLine2 = plugin.getServer().createBossBar("", BarColor.YELLOW, BarStyle.SOLID);
        barLine1.setVisible(true);
        barLine2.setVisible(true);
    }

    @Override
    public void run() {
        if(remainingSeconds <= 0) {
            stopTiming();
            plugin.onTimerEnded();
            return;
        } else {
            remainingSeconds --;
            barLine1.setTitle(Lang.build("bars.wait.line1", plugin.getServer().getOnlinePlayers().size(), plugin.getConfig().getInt("max-players")));
            barLine2.setTitle(Lang.build("bars.wait.line2", remainingSeconds));
            // this is actually showing player numbers, only
            barLine2.setProgress(plugin.getServer().getOnlinePlayers().size() / ((float)plugin.getConfig().getInt("max-players")));
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        if(plugin.getPhase().equals(GamePhase.WAIT) && task != null && barLine1 != null) {
            barLine1.addPlayer(e.getPlayer());
            barLine2.addPlayer(e.getPlayer());
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        if(barLine1 != null) {
            barLine1.removePlayer(e.getPlayer());
            barLine2.removePlayer(e.getPlayer());
        }
    }

    private void resetTimerByPlayersOnline() {
        int players = plugin.getServer().getOnlinePlayers().size();
        int max = plugin.getConfig().getInt("max-players");
        if(players < max) {
            remainingSeconds = plugin.getConfig().getInt("wait-time.normal");
        } else {
            remainingSeconds = plugin.getConfig().getInt("wait-time.full");
        }
    }

    public void startTiming() {
        if(task != null) throw new IllegalStateException("Timer already started! ");
        resetTimerByPlayersOnline();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this, 0L, 20L);
        plugin.getServer().getOnlinePlayers().forEach(barLine1::addPlayer);
        plugin.getServer().getOnlinePlayers().forEach(barLine2::addPlayer);
    }

    public void stopTiming() {
        if(task != null) {
            task.cancel();
            task = null;
        }
        if(barLine1 != null) {
            barLine1.removeAll();
            barLine2.removeAll();
        }
    }
}
