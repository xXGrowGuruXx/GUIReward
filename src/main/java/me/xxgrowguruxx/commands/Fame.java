package me.xxgrowguruxx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class Fame implements CommandExecutor {
    private final FileConfiguration messages;

    public Fame() {
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
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("addfame") || command.getName().equalsIgnoreCase("removefame")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage(getMessage("console"));
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("reward.fame")) {
                player.sendMessage(getMessage("noPermission"));
                return true;
            }

            File fameFile = new File("plugins/GUIReward/playerdata/Fame.yml");
            FileConfiguration fameConfig = YamlConfiguration.loadConfiguration(fameFile);

            if (args.length < 2) {
                player.sendMessage(getMessage("FameMissingArgs"));
                return true;
            }

            String playerName = args[0];
            int fameAmount;

            try {
                fameAmount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(getMessage("cantUse"));
                return true;
            }

            String playerUUIDString = null;
            for (String key : fameConfig.getKeys(false)) {
                if (Objects.requireNonNull(fameConfig.getString(key + ".name")).equalsIgnoreCase(playerName)) {
                    playerUUIDString = key;
                    break;
                }
            }

            if (playerUUIDString == null) {
                // Spieler nicht gefunden, neuen Eintrag erstellen
                UUID playerUUID = player.getUniqueId();
                playerUUIDString = playerUUID.toString();
                fameConfig.set(playerUUIDString + ".name", playerName);
                fameConfig.set(playerUUIDString + ".balance", 0); // Standardeintrag mit Balance 0
            }

            int currentBalance = fameConfig.getInt(playerUUIDString + ".balance");
            int newBalance = command.getName().equalsIgnoreCase("addfame")
                    ? currentBalance + fameAmount
                    : currentBalance - fameAmount;
            fameConfig.set(playerUUIDString + ".balance", newBalance);

            try {
                fameConfig.save(fameFile);
            } catch (IOException e) {
                e.printStackTrace();
                player.sendMessage(getMessage("saveError"));
                return true;
            }
            String newFame = messages.getString("fame");
            assert newFame != null;
            player.sendMessage(String.format(newFame, newBalance, "Fame"));
        }

        if (command.getName().equalsIgnoreCase("famebalance")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                File fame = new File("plugins/GUIReward/PlayerData/Fame.yml");
                FileConfiguration fameconfig = YamlConfiguration.loadConfiguration(fame);

                UUID uuid = Bukkit.getPlayerUniqueId(String.valueOf(player));

                if (fameconfig.contains(String.valueOf(uuid))) {
                    ConfigurationSection playerSection = fameconfig.getConfigurationSection(String.valueOf(uuid));
                    assert playerSection != null;
                    double balance = playerSection.getDouble("balance");

                    player.sendMessage("§2Fame: " + balance);
                } else {
                    player.sendMessage(getMessage("noFameAccount"));
                }
            } else {
                sender.sendMessage(getMessage("console"));
            }
        }

        return true;
    }
}