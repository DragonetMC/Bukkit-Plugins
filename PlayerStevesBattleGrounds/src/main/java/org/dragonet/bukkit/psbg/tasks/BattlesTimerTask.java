package org.dragonet.bukkit.psbg.tasks;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.dragonet.bukkit.psbg.Battles;
import org.dragonet.bukkit.psbg.PlayerStevesBattleGrounds;
import org.dragonet.bukkit.psbg.PlayerTag;
import org.dragonet.bukkit.psbg.PlayerTagMetadata;
import org.dragonet.bukkit.psbg.utils.Lang;

/**
 * Created on 2017/12/2.
 */
public class BattlesTimerTask extends ManagedTask {

    private final PlayerStevesBattleGrounds plugin;
    private final Battles battles;

    private int seconds;
    private BossBar bar1;
    private BossBar bar2;

    public BattlesTimerTask(PlayerStevesBattleGrounds plugin, Battles battles) {
        this.plugin = plugin;
        this.battles = battles;
    }

    @Override
    public void start() {
        seconds = plugin.getConfig().getInt("battles.time");
        bar1 = plugin.getServer().createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        bar2 = plugin.getServer().createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        bar1.setVisible(true);
        bar2.setVisible(true);
        plugin.getServer().getOnlinePlayers().forEach(p -> {
            bar1.addPlayer(p);
            bar2.addPlayer(p);
        });
        setTask(plugin.getServer().getScheduler().runTaskTimer(plugin, this, 0L, 20L));
    }

    @Override
    public void run() {
        seconds --;
        if(seconds > 0) {
            bar1.setTitle(Lang.build("bars.game.line1", plugin.countNormalPlayers()));
            String time;
            {
                if (seconds < 60) {
                    time = seconds + "s";
                } else {
                    int min = seconds / 60;
                    int sec = seconds % 60;
                    time = String.format("%d:%d", min, sec);
                }
            }
            bar2.setTitle(Lang.build("bars.game.line2", time));
        } else {
            // ends
            cancel();
            battles.endBattles();
        }
    }
}
