package org.dragonet.bukkit.psbg;

import org.bukkit.entity.Player;

/**
 * Created on 2017/12/1.
 */
public final class PlayerTagMetadata {

    public PlayerTag tag;

    public PlayerTagMetadata(PlayerTag tag) {
        this.tag = tag;
    }

    public static PlayerTag getTag(Player player) {
        if(!player.hasMetadata(PlayerStevesBattleGrounds.PLAYER_TAG_METADATA_KEY)) return null;
        return ((PlayerTagMetadata) player.getMetadata(PlayerStevesBattleGrounds.PLAYER_TAG_METADATA_KEY).get(0).value())
                .tag;
    }

    public static void updateMetadata(Player player, PlayerTag tag) {
        if(player == null || tag == null) {
            throw new IllegalArgumentException();
        }
        ((PlayerTagMetadata) player.getMetadata(PlayerStevesBattleGrounds.PLAYER_TAG_METADATA_KEY).get(0).value())
                .tag = tag;
    }
}
