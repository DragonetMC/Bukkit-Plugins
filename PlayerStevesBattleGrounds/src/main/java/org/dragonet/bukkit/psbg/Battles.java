package org.dragonet.bukkit.psbg;

import es.pollitoyeye.Bikes.ParachuteManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;
import org.dragonet.bukkit.psbg.tasks.BattlesTimerTask;
import org.dragonet.bukkit.psbg.tasks.ShrinkBorderTask;
import org.dragonet.bukkit.psbg.utils.InventoryUtils;
import org.dragonet.bukkit.psbg.utils.Lang;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created on 2017/12/2.
 */
public class Battles implements Listener {

    private final PlayerStevesBattleGrounds plugin;

    private String scoreboardLabelRank;
    private String scoreboardLabelKills;

    private boolean started;
    private World world;
    private ShrinkBorderTask taskShrinkBorder;
    private BattlesTimerTask taskTimer;

    public Battles(PlayerStevesBattleGrounds plugin) {
        this.plugin = plugin;
    }

    public void startBattles() {
        if(started) {
            throw new IllegalStateException();
        }
        started = true;

        world = plugin.getServer().getWorld(plugin.getConfig().getString("battles.world"));

        // prepare everyone
        plugin.getServer().getOnlinePlayers().forEach(c -> {
            InventoryUtils.clearInventory(c.getInventory());
            c.playSound(c.getLocation(), "psbg.battle-begin", 1f, 1f);
            c.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                    20*2,
                    1,
                    true,
                    false), true);
            Lang.sendMessage(c, "messages.start.begin");
        });

        // prepare scoreboard
        scoreboardLabelRank = Lang.build("scoreboard.game.rank");
        scoreboardLabelKills = Lang.build("scoreboard.game.kills");
        Scoreboard sb = plugin.getServer().getScoreboardManager().getNewScoreboard();
        {
            Objective title_objective = sb.registerNewObjective(Lang.build("scoreboard.game.title"), "B");
            title_objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            title_objective.setDisplayName("C");
            Team team = sb.registerNewTeam("T");
            team.addEntry(scoreboardLabelRank);
            team.addEntry(scoreboardLabelKills);
            title_objective.getScore("ENT-TEST").setScore(0);
            title_objective.getScore("ENT-TEST-2").setScore(123456);
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // spawn everyone
        double spawnCenterX = plugin.getConfig().getDouble("battles.spawn.center-x");
        double spawnCenterZ = plugin.getConfig().getDouble("battles.spawn.center-x");
        double spawnRadius = plugin.getConfig().getDouble("battles.spawn.radius");
        Random r = new Random();
        for(Player p : plugin.getServer().getOnlinePlayers()) {
            double x = spawnCenterX + (r.nextDouble() * spawnRadius * (r.nextBoolean() ? 1d : -1d));
            double z = spawnCenterZ + (r.nextDouble() * spawnRadius * (r.nextBoolean() ? 1d : -1d));
            double y = 192d + (r.nextDouble() * 32d);
            Location location = new Location(world, x, y, z);
            p.teleport(location);
            p.setScoreboard(sb);
            plugin.getServer().getScheduler().runTask(plugin, () ->
                plugin.getServer().getPluginManager().callEvent(new PlayerInteractEvent(
                        p,
                        Action.RIGHT_CLICK_AIR,
                        ParachuteManager.getParachuteItem(plugin.getConfig().getString("battles.parachute-type")),
                        null,
                        null
                ))
            );
        }

        // set world border
        WorldBorder border = world.getWorldBorder();
        border.setCenter(
                plugin.getConfig().getInt("battle-world.center-x"),
                plugin.getConfig().getInt("battle-world.center-z")
        );
        border.setDamageAmount(10000d);
        border.setSize(plugin.getConfig().getDouble("battles.safe-zone.initial-size"));
        border.setSize(100, 200);

        taskTimer = new BattlesTimerTask(plugin, this);
        taskShrinkBorder = new ShrinkBorderTask(plugin, this);

        taskTimer.start();
        taskShrinkBorder.start();
    }

    public void endBattles() {
        if(!started) {
            throw new IllegalStateException();
        }
        started = false;
        plugin.setEndPhase();
        if(taskShrinkBorder != null) {
            taskShrinkBorder.cancel();
            taskShrinkBorder = null;
        }
        if(taskTimer != null) {
            taskTimer.cancel();
            taskTimer = null;
        }

        // TODO: teleport all players back to the hub
        // ...

        plugin.getServer().shutdown();
    }

    @EventHandler
    private void onPlayerHurt(EntityDamageByEntityEvent e) {
        if(!Player.class.isAssignableFrom(e.getDamager().getClass())) return;
        if(!Player.class.isAssignableFrom(e.getEntity().getClass())) return;
        e.getEntity().setMetadata("last_hurt_by", new FixedMetadataValue(plugin, e.getDamager()));
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
        e.setDroppedExp(0);
        e.getEntity().setGameMode(GameMode.SPECTATOR);
        PlayerTagMetadata.updateMetadata(e.getEntity(), PlayerTag.SPECTATING);

        if(e.getEntity().hasMetadata("last_hurt_by")) {
            Player killer = (Player) e.getEntity().getMetadata("last_hurt_by").get(0).value();
            plugin.getServer().broadcastMessage(Lang.build("messages.game.kill", e.getEntity().getName(),
                    killer.getName()));
            if(killer.isOnline()) {
                Score s = killer.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(scoreboardLabelKills);
                s.setScore(s.getScore() + 1);
            }
        } else {
            plugin.getServer().broadcastMessage(Lang.build("messages.game.died", e.getEntity().getName()));
        }

        rerankPlayers();
    }

    public void rerankPlayers() {
        List<Player> playersOnline = new ArrayList<>();
        playersOnline.addAll(plugin.getServer().getOnlinePlayers());
        playersOnline.sort((a, b) -> {
            int killsA = a.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(scoreboardLabelKills).getScore();
            int killsB = b.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(scoreboardLabelKills).getScore();
            return killsA - killsB;
        });
        for(int i = 0; i < playersOnline.size(); i++) {
            Score r = playersOnline.get(i).getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(scoreboardLabelRank);
            r.setScore(i + 1);
        }
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent e) {
        if(plugin.getServer().getOnlinePlayers().size() < plugin.getConfig().getInt("min-players")) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                endBattles()
            , 100L);
        } else {
            rerankPlayers();
        }
    }

    public World getWorld() {
        return world;
    }

    public String getScoreboardLabelRank() {
        return scoreboardLabelRank;
    }

    public String getScoreboardLabelKills() {
        return scoreboardLabelKills;
    }
}
