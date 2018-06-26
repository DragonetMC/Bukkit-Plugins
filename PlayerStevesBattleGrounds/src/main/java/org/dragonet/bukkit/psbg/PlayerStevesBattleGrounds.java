package org.dragonet.bukkit.psbg;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.dragonet.bukkit.psbg.listeners.PlayerListener;
import org.dragonet.bukkit.psbg.listeners.StaticWorldListener;
import org.dragonet.bukkit.psbg.tasks.CountDownProcessor;
import org.dragonet.bukkit.psbg.utils.Lang;

/**
 * Created on 2017/11/29.
 */
public class PlayerStevesBattleGrounds extends JavaPlugin {

    public final static String PLAYER_TAG_METADATA_KEY = "PSBG_PlayerTag";

    private YamlConfiguration config;

    private GamePhase phase = GamePhase.WAIT;
    private CountDownProcessor waitTimer = new CountDownProcessor(this);

    private Battles battles;

    @Override
    public void onEnable() {
        saveResource("lang.yml", false);
        saveResource("config.yml", false);

        Lang.lang = YamlConfiguration.loadConfiguration(new java.io.File(getDataFolder(), "lang.yml"));
        config = YamlConfiguration.loadConfiguration(new java.io.File(getDataFolder(), "config.yml"));

        waitTimer.init();

        getServer().getPluginManager().registerEvents(new StaticWorldListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(waitTimer, this);

        waitTimer.startTiming();
    }

    /**
     * called when timer counts down to zero.
     */
    public void onTimerEnded() {
        int currentPlayers = getServer().getOnlinePlayers().size();
        if(currentPlayers < config.getInt("min-players")) {
            getServer().broadcastMessage(Lang.build("messages.start.not-enough-players", config.getInt("min-players")));
            waitTimer.startTiming();
            return;
        }

        // starts the match, all player should have NORMAL tag

        battles = new Battles(this);
        battles.startBattles();
    }

    public int countNormalPlayers() {
        int c = 0;
        for(Player p : getServer().getOnlinePlayers()) {
            if(PlayerTagMetadata.getTag(p) == PlayerTag.NORMAL) c++;
        }
        return c;
    }

    public void setEndPhase() {
        if(!phase.equals(GamePhase.PLAY)) throw new IllegalStateException();
        phase = GamePhase.ENDED;
    }

    public GamePhase getPhase() {
        return phase;
    }

    @Override
    public YamlConfiguration getConfig() {
        return config;
    }
}
