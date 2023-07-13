package me.xxgrowguruxx.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Transfer implements CommandExecutor, TabCompleter {

    private final FileConfiguration messages;

    public Transfer() {
        // Lade die Konfigurationsdatei
        File configFile = new File("plugins/GUIReward/config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Lade die Sprache aus der Konfigurationsdatei
        String language = config.getString("language");

        // Erstelle den Dateinamen basierend auf der Sprache
        String fileName = language + ".yml";

        // Lade die Nachrichtendatei basierend auf dem Dateinamen
        File messagesFile = new File("plugins/GUIReward/messages/" + fileName);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private String getMessage(String key, Object... args) {
        String message = messages.getString(key);
        if (message != null) {
            return String.format(message, args);
        }
        return "";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("console"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(getMessage("FameMissingArgs"));
            return true;
        }

        String amountString = args[0];
        String playerName = args[1];

        File fameFile = new File("plugins/GUIReward/PlayerData/Fame.yml");
        FileConfiguration fame = YamlConfiguration.loadConfiguration(fameFile);

        Player senderPlayer = (Player) sender;
        int amount;
        try {
            amount = Integer.parseInt(amountString);
        } catch (NumberFormatException e) {
            sender.sendMessage(getMessage("InvalidAmount"));
            return true;
        }

        UUID senderUUID = Bukkit.getPlayerUniqueId(String.valueOf(senderPlayer));
        int senderBalance = fame.getInt(senderUUID + ".balance", 0);
        senderBalance = Math.max(0, senderBalance); // Sicherstellen, dass der Kontostand nicht negativ ist

        if (senderBalance < amount) {
            sender.sendMessage(getMessage("InsufficientBalance"));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(playerName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(getMessage("PlayerNotOnline"));
            return true;
        }

        UUID targetUUID = Bukkit.getPlayerUniqueId(String.valueOf(targetPlayer));
        int targetBalance = fame.getInt(targetUUID + ".balance", 0);

        assert senderUUID != null;
        if (senderUUID.equals(targetUUID)) {
            sender.sendMessage(getMessage("CannotSendToFameSelf"));
            return true;
        }

        // Aktualisiere den Fame-Betrag des Senders
        fame.set(senderUUID + ".balance", senderBalance - amount);

        // Aktualisiere den Fame-Betrag des Zielspielers
        fame.set(targetUUID + ".balance", targetBalance + amount);

        // Speichere die Änderungen in der Fame.yml-Datei
        try {
            fame.save(fameFile);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle die Fehlermeldung oder sende dem Spieler eine Fehlermeldung
        }

        // Sende die Nachrichten an den Sender und den Zielspieler
        sender.sendMessage(getMessage("TransferSent", amountString, targetPlayer.getName()));
        targetPlayer.sendMessage(getMessage("TransferReceived", amountString, senderPlayer.getName()));

        return true;
    }


    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> hints = new ArrayList<>();
            hints.add(ChatColor.GRAY + getMessage("AmountHint"));
            return hints;
        } else if (args.length == 2) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        }
        return null;
    }
}
