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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class LogIn implements Listener {

    private final FileConfiguration messages;

    public LogIn() {
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = Bukkit.getPlayerUniqueId(String.valueOf(player));
        player.sendMessage(getMessage("login"));

        // Format des Datums und der Uhrzeit im deutschen Format festlegen
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        // Datei und Konfiguration laden
        File logInFile = new File("plugins/GUIReward/PlayerData/LogIn.yml");
        YamlConfiguration logInConfig = YamlConfiguration.loadConfiguration(logInFile);

        // Letzten Login aus der Konfiguration abrufen
        String nowLoginString = logInConfig.getString(uuid + ".NowLogIn");
        LocalDateTime nowLogin = null;
        if (nowLoginString != null) {
            nowLogin = LocalDateTime.parse(nowLoginString, formatter);
        }

        // Aktuellen Login speichern und in der Konfiguration aktualisieren
        LocalDateTime now = LocalDateTime.now();
        logInConfig.set(uuid + ".NowLogIn", formatter.format(now));
        logInConfig.set(uuid + ".LastLogIn", formatter.format(nowLogin != null ? nowLogin : now));

        // Konfiguration in Datei speichern
        try {
            logInConfig.save(logInFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
