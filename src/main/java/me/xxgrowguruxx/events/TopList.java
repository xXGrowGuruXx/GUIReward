package me.xxgrowguruxx.events;

import dev.triumphteam.gui.components.GuiAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.*;

public class TopList {
    public static GuiAction<InventoryClickEvent> performAction() {
        return event -> {
            Map<String, Integer> fameMap = new HashMap<>();

            FileConfiguration fameConfig = YamlConfiguration.loadConfiguration(new File("plugins/GUIReward/PlayerData/Fame.yml"));

            // Spieler-Fame-Mappings aus der Fame-Datei laden
            for (String uuidStr : fameConfig.getKeys(false)) {
                ConfigurationSection playerFameSection = fameConfig.getConfigurationSection(uuidStr);
                if (playerFameSection != null) {
                    int fameValue = playerFameSection.getInt(".balance");
                    String playerName = playerFameSection.getString("name");

                    if (playerName != null) {
                        fameMap.put(playerName, fameValue);
                    }
                }
            }

            // Map nach Fame-Werten sortieren und TopList generieren
            List<Map.Entry<String, Integer>> sortedFameList = new ArrayList<>(fameMap.entrySet());
            sortedFameList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            event.getWhoClicked().closeInventory();
            event.getWhoClicked().sendMessage("§6§l===============================");
            event.getWhoClicked().sendMessage("§3§l           TopList             ");
            event.getWhoClicked().sendMessage("§6§l===============================");

            int i = 1;
            for (Map.Entry<String, Integer> entry : sortedFameList) {
                if (i > 10) {
                    break;
                }
                Component playerFame = Component.text(i + ". " + entry.getKey() + " - " + entry.getValue() + " Fame");
                ComponentBuilder<TextComponent, TextComponent.Builder> builder = Component.text().append(playerFame);
                event.getWhoClicked().sendMessage(builder.color(NamedTextColor.GREEN).build());
                i++;
            }
            event.getWhoClicked().sendMessage("§6§l===============================");
        };
    }
}