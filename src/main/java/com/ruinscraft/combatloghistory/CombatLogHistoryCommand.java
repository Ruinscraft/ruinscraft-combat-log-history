package com.ruinscraft.combatloghistory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CombatLogHistoryCommand implements CommandExecutor {

    private CombatLogHistoryStorage storage;

    public CombatLogHistoryCommand(CombatLogHistoryStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "No username supplied. /combatloghistory <username>");
            return true;
        }

        String lookup = args[0];

        if (lookup.length() > 16) {
            sender.sendMessage(ChatColor.RED + "Not a valid username.");
            return true;
        }

        OfflinePlayer lookupPlayer = Bukkit.getOfflinePlayer(lookup);

        if (!lookupPlayer.hasPlayedBefore() && !lookupPlayer.isOnline()) {
            sender.sendMessage(ChatColor.RED + lookup + " has never played before.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "Looking up combat log history for " + lookup + "...");

        storage.queryHistory(lookup).thenAccept(result -> {
            if (result.isEmpty()) {
                try {
                    sender.sendMessage(ChatColor.GOLD + "No combat log history for " + lookup);
                } catch (NullPointerException e) {
                    // ignore, the CommandSender may have been a player who is now offline...
                }
            }

            for (CombatLog entry : result) {
                try {
                    String logger = entry.getLogger();
                    LocalDate date = LocalDate.ofEpochDay(entry.getTime());
                    String dateString = date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    String locationString = entry.getLocationString();

                    sender.sendMessage(ChatColor.GOLD + "> " + logger + " combat logged on " + dateString + " @ " + locationString);
                } catch (Exception e) {
                    // ignore, the CommandSender may have been a player who is now offline...
                }
            }
        });

        return true;
    }

}
