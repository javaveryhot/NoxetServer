package org.noxet.noxetserver.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.noxet.noxetserver.menus.inventory.CreeperSweeperGameMenu;
import org.noxet.noxetserver.messaging.NoxetErrorMessage;

public class CreeperSweeper implements CommandExecutor {
    @Override
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)) {
            new NoxetErrorMessage(NoxetErrorMessage.ErrorType.COMMON, "Only players can play Creeper Sweeper.").send(commandSender);
            return true;
        }

        Player player = (Player) commandSender;

        new CreeperSweeperGameMenu(6, 8).openInventory(player);

        return true;
    }
}
