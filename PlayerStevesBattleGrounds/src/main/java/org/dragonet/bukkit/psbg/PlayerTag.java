package org.dragonet.bukkit.psbg;

/**
 * Created on 2017/12/1.
 */
public enum PlayerTag {

    /**
     * when a player is waiting or playing
     */
    NORMAL,

    /**
     * when a player spectating (join after game started or died),
     * player should be in adventure mode with flying enabled
     */
    SPECTATING

}
