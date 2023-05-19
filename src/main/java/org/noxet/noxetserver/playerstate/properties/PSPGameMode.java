package org.noxet.noxetserver.playerstate.properties;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.noxet.noxetserver.playerstate.PlayerStateProperty;

public class PSPGameMode extends PlayerStateProperty {
    @Override
    public String getConfigName() {
        return "game_mode";
    }

    @Override
    public Object getSerializedPropertyFromPlayer(Player player) {
        return player.getGameMode().name();
    }

    @Override
    public void restoreProperty(Player player, Object value) {
        player.setGameMode(GameMode.valueOf((String) value));
    }
}