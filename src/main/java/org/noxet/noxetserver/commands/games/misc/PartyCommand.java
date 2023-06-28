package org.noxet.noxetserver.commands.games.misc;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.noxet.noxetserver.NoxetServer;
import org.noxet.noxetserver.messaging.ErrorMessage;
import org.noxet.noxetserver.messaging.Message;
import org.noxet.noxetserver.messaging.NoteMessage;
import org.noxet.noxetserver.minigames.party.Party;

public class PartyCommand implements CommandExecutor {
    @Override
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)) {
            new ErrorMessage(ErrorMessage.ErrorType.COMMON, "Only players can manage parties.").send(commandSender);
            return true;
        }

        Player player = (Player) commandSender;

        if(strings.length == 0) {
            new ErrorMessage(ErrorMessage.ErrorType.ARGUMENT, "Missing argument: what to do with party.").send(player);
            return true;
        }

        if(strings[0].equalsIgnoreCase("create")) {
            if(Party.isPlayerMemberOfParty(player)) {
                new ErrorMessage(ErrorMessage.ErrorType.COMMON, "You are already in a party. Please leave the party first.").send(player);
                return true;
            }

            new Party(player);

            return true;
        } else if(strings[0].equalsIgnoreCase("invite")) {
            if(strings.length < 2) {
                new ErrorMessage(ErrorMessage.ErrorType.ARGUMENT, "Missing argument: player to invite.").send(player);
                return true;
            }

            Player playerToInvite = NoxetServer.getPlugin().getServer().getPlayer(strings[1]);

            if(playerToInvite == null) {
                new ErrorMessage(ErrorMessage.ErrorType.COMMON, "Invalid player. You can only invite someone who is actually online.").send(player);
                return true;
            }

            Party party = Party.getPartyFromMember(player);

            if(party == null) {
                new NoteMessage("You are not in a party. To save you time, we will create one for you.").send(player);
                party = new Party(player);
            }

            if(!party.isOwner(player)) {
                new ErrorMessage(ErrorMessage.ErrorType.PERMISSION, "You are not the owner of this party, so you cannot invite players.").send(player);
                return true;
            }

            party.invitePlayer(playerToInvite);

            return true;
        } else if(strings[0].equalsIgnoreCase("kick")) {
            Party party = Party.getPartyFromMember(player);

            if(party == null) {
                new ErrorMessage(ErrorMessage.ErrorType.COMMON, "You are not in a party.").send(player);
                return true;
            }

            if(!party.isOwner(player)) {
                new ErrorMessage(ErrorMessage.ErrorType.PERMISSION, "You are not the owner of this party, so you cannot kick players.").send(player);
                return true;
            }

            if(strings.length < 2) {
                new ErrorMessage(ErrorMessage.ErrorType.ARGUMENT, "Missing argument: player to kick.").send(player);
                return true;
            }

            Player playerToKick = NoxetServer.getPlugin().getServer().getPlayer(strings[1]);

            if(playerToKick == null) {
                new ErrorMessage(ErrorMessage.ErrorType.COMMON, "Invalid player.").send(player);
                return true;
            }

            if(!party.getMembers().contains(playerToKick)) {
                new ErrorMessage(ErrorMessage.ErrorType.COMMON, "This player is not a member of your party.").send(player);
                return true;
            }

            party.kickMember(playerToKick);

            return true;
        } else if(strings[0].equalsIgnoreCase("transfer")) {
            Party party = Party.getPartyFromMember(player);

            if(party == null) {
                new ErrorMessage(ErrorMessage.ErrorType.COMMON, "You are not in a party.").send(player);
                return true;
            }

            if(!party.isOwner(player)) {
                new ErrorMessage(ErrorMessage.ErrorType.PERMISSION, "You are not the owner of this party, so you cannot transfer it.").send(player);
                return true;
            }

            if(strings.length < 2) {
                new ErrorMessage(ErrorMessage.ErrorType.ARGUMENT, "Missing argument: player to transfer the party to.").send(player);
                return true;
            }

            Player newOwner = NoxetServer.getPlugin().getServer().getPlayer(strings[1]);

            if(newOwner == null) {
                new ErrorMessage(ErrorMessage.ErrorType.COMMON, "Invalid player.").send(player);
                return true;
            }

            if(!party.getMembers().contains(newOwner)) {
                new ErrorMessage(ErrorMessage.ErrorType.COMMON, "This player is not a member of your party.").send(player);
                return true;
            }

            party.transfer(newOwner);

            return true;
        } else if(strings[0].equalsIgnoreCase("leave")) {
            Party party = Party.getPartyFromMember(player);

            if(party == null) {
                new ErrorMessage(ErrorMessage.ErrorType.COMMON, "You are not in a party.").send(player);
                return true;
            }

            if(party.isOwner(player)) {
                new ErrorMessage(ErrorMessage.ErrorType.COMMON, "You are the owner of this party. To leave it, you must disband it or transfer it.").send(player);
                return true;
            }

            party.memberLeave(player);

            return true;
        } else if(strings[0].equalsIgnoreCase("list")) {
            Party party = Party.getPartyFromMember(player);

            if(party == null) {
                new ErrorMessage(ErrorMessage.ErrorType.COMMON, "You are not in a party.").send(player);
                return true;
            }

            new Message("§eMembers (with owner): " + party.getMembers().size()).send(commandSender);

            for(Player member : party.getMembers()) {
                Message message = new Message(
                        "└§6§lMEMBER §e" + member.getName()
                );

                if(party.isOwner(player))
                    message.addButton(
                        "Kick",
                        ChatColor.RED,
                        "Kick this player from the party",
                        "party kick " + member.getName()
                    );

                message.send(player);
            }

            return true;
        } else if(strings[0].equalsIgnoreCase("disband")) {
            Party party = Party.getPartyFromMember(player);

            if(party == null) {
                new ErrorMessage(ErrorMessage.ErrorType.COMMON, "You are not in a party.").send(player);
                return true;
            }

            if(!party.isOwner(player)) {
                new ErrorMessage(ErrorMessage.ErrorType.PERMISSION, "You are not the owner of this party, so you cannot disband it.").send(player);
                return true;
            }

            party.disband();

            return true;
        } else {
            new ErrorMessage(ErrorMessage.ErrorType.ARGUMENT, "Invalid subcommand '" + strings[0] + "'.").send(player);
            return false;
        }
    }
}
