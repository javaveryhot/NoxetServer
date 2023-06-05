package org.noxet.noxetserver.commands.anarchy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.noxet.noxetserver.RealmManager;
import org.noxet.noxetserver.messaging.NoxetErrorMessage;

import static org.noxet.noxetserver.RealmManager.migrateToRealm;

@SuppressWarnings("ALL")
public class Anarchy implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)) {
            new NoxetErrorMessage(NoxetErrorMessage.ErrorType.COMMON, "Only players can be sent to the Anarchy server.").send(commandSender);
            return true;
        }

        migrateToRealm((Player) commandSender, RealmManager.Realm.ANARCHY);

        return true;
    }
}
