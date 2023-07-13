package me.xxgrowguruxx.events;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Join implements Listener {

    private final FileConfiguration messages;

    public Join() {
        // Lade die Konfigurationsdatei für die Sprachen
        File configFile = new File("plugins/GUIReward/config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Lade den Dateinamen der Sprachdatei aus der Konfiguration
        String language = config.getString("language");
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        File fameFile = new File("plugins/GUIReward/PlayerData/Fame.yml");
        FileConfiguration fame = YamlConfiguration.loadConfiguration(fameFile);

        Player player = event.getPlayer();
        UUID uuid = Bukkit.getPlayerUniqueId(String.valueOf(player));

        assert uuid != null;
        if (!fame.contains(uuid.toString())) {
            // Spieler hat noch keinen Fame-Eintrag

            fame.set(uuid + ".name", player.getName());
            fame.set(uuid + ".balance", 0);

            // Speichern der Fame.yml
            try {
                fame.save(fameFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Nachricht an den Spieler senden
            player.sendMessage(getMessage("CreateAcc"));
        }
    }
}