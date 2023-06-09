package org.noxet.noxetserver.commands.admin;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.noxet.noxetserver.NoxetServer;
import org.noxet.noxetserver.messaging.ErrorMessage;
import org.noxet.noxetserver.messaging.SuccessMessage;
import org.noxet.noxetserver.playerdata.PlayerDataManager;

import java.util.Collections;
import java.util.List;

public class Mute implements TabExecutor {
    @Override
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!commandSender.isOp()) {
            new ErrorMessage(ErrorMessage.ErrorType.PERMISSION, "You must be an operator to mute players.").send(commandSender);
            return true;
        }

        if(strings.length == 0) {
            new ErrorMessage(ErrorMessage.ErrorType.ARGUMENT, "Missing player to mute.").send(commandSender);
            return false;
        }

        Player playerToMute = NoxetServer.getPlugin().getServer().getPlayer(strings[0]);

        if(playerToMute == null) {
            new ErrorMessage(ErrorMessage.ErrorType.ARGUMENT, "That is not an online player.").send(commandSender);
            return true;
        }

        PlayerDataManager playerDataManager = new PlayerDataManager(playerToMute);

        if((boolean) playerDataManager.get(PlayerDataManager.Attribute.MUTED)) {
            new ErrorMessage(ErrorMessage.ErrorType.COMMON, "That player is already muted.").send(commandSender);
            return true;
        }

        playerDataManager.set(PlayerDataManager.Attribute.MUTED, true).save();
        new SuccessMessage(
                playerToMute.getName() + " has been muted and can no longer chat.")
                .addButton("Unmute", ChatColor.RED, "Undo the mute", "unmute " + playerToMute.getName()).send(commandSender);

        return true;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length == 1) {
            Player playerToRecommend = NoxetServer.getPlugin().getServer().getPlayer(strings[0]);
            if(playerToRecommend != null)
                return Collections.singletonList(playerToRecommend.getName());
        }

        return null;
    }
}
