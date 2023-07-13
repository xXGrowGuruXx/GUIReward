package me.xxgrowguruxx.commands;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.xxgrowguruxx.events.FameShop;
import me.xxgrowguruxx.events.Reward;
import me.xxgrowguruxx.events.TopList;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GUI implements CommandExecutor {

    private final FileConfiguration messages;

    public GUI() {
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("console"));
            return true;
        }

        Player player = (Player) sender;
        UUID targetUUID = Bukkit.getPlayerUniqueId(String.valueOf(player));
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/GUIReward/PlayerData/Fame.yml"));
        int currentFame = 0;

        if (config.contains(targetUUID + ".balance")) {
            currentFame = Integer.parseInt(Objects.requireNonNull(config.getString(targetUUID + ".balance")));
        }

        // Datei und Konfiguration laden
        File logInFile = new File("plugins/GUIReward/PlayerData/LogIn.yml");
        YamlConfiguration logInConfig = YamlConfiguration.loadConfiguration(logInFile);

        // Letzten Login aus der Konfiguration abrufen
        String lastLoginString = logInConfig.getString(targetUUID + ".LastLogIn");
        LocalDateTime lastLogin = null;
        if (lastLoginString != null) {
            lastLogin = LocalDateTime.parse(lastLoginString, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }

        // Aktuellen Login aus der Konfiguration abrufen
        String nowLoginString = logInConfig.getString(targetUUID + ".NowLogIn");
        LocalDateTime nowLogin = null;
        if (nowLoginString != null) {
            nowLogin = LocalDateTime.parse(nowLoginString, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }

        // Gui erstellen
        Gui main = Gui.gui()
                .title(Component.text("§4§n§lDayliReward"))
                .rows(3)
                .create();

        Reward reward = new Reward();
        GuiItem take = ItemBuilder.from(Material.GOLD_INGOT)
                .name(Component.text("§3§lReward"))
                .lore(Component.text(getMessage("reward")))
                .asGuiItem(reward.performAction());

        GuiItem shop = ItemBuilder.from(Material.BOOK)
                .name(Component.text("§3§lFameShop"))
                .lore(Component.text(getMessage("shop")))
                .asGuiItem(FameShop.performAction());

        GuiItem toplist = ItemBuilder.from(Material.PAPER)
                .name(Component.text("§6§lTopList"))
                .lore(Component.text(getMessage("topList")))
                .asGuiItem(TopList.performAction());

        // Auslesen der Fame-Werte aller Spieler aus der Fame.yml-Datei
        Map<UUID, Map<String, Integer>> fameData = new HashMap<>();
        File fameFile = new File("plugins/GUIReward/PlayerData/Fame.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(fameFile);
        cfg.getKeys(false).forEach(uuid -> {
            Map<String, Integer> playerData = new HashMap<>();
            ConfigurationSection playerSection = cfg.getConfigurationSection(uuid);
            assert playerSection != null;
            playerSection.getKeys(false).forEach(name -> {
                int fame = playerSection.getInt(name);
                playerData.put(name, fame);
            });
            fameData.put(UUID.fromString(uuid), playerData);
        });

        // Sortieren der Spieler basierend auf ihren Fame-Werten
        List<UUID> sortedPlayers = fameData.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().values().stream().mapToInt(Integer::intValue).sum()
                        - e1.getValue().values().stream().mapToInt(Integer::intValue).sum())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Bestimmen des Platzes des Spielers basierend auf ihrem Fame-Wert
        int playerRank = sortedPlayers.indexOf(Bukkit.getPlayerUniqueId(String.valueOf(player))) + 1;

        // Erstellen der Lore-Komponenten
        Component infoLore1;
        if (currentFame > 0) {
            infoLore1 = Component.text("Fame: " + currentFame);
        } else {
            infoLore1 = Component.text("Fame: ----");
        }

        // Überprüfung, ob der letzte Login und der aktuelle Login gleich sind
        Component infoLore2 = Component.empty();
        if (lastLogin != null && lastLogin.equals(nowLogin)) {
            infoLore2 = infoLore2.append(Component.text(getMessage("login1")));
        } else {
            assert lastLogin != null;
            infoLore2 = infoLore2.append(Component.text(getMessage("login2") + lastLogin.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
        }

        Component infoLore3 = Component.text("TopList Rank: " + playerRank);

        // Erstellen des GuiItems
        GuiItem info = ItemBuilder.from(Material.PLAYER_HEAD)
                .name(Component.text(player.getName()))
                .lore(infoLore2, infoLore1, infoLore3)
                .asGuiItem();

        GuiItem filler = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                .glow()
                .name(Component.text(""))
                .asGuiItem();

        // Gui zusammenstellen und öffnen
        main.disableAllInteractions();
        main.setItem(2, 3, take);
        main.setItem(2, 7, shop);
        main.setItem(1, 5, info);
        main.setItem(3, 5, toplist);
        main.getFiller().fill(filler);

        main.open(player);

        return true;
    }
}