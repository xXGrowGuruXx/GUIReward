package me.xxgrowguruxx.adminshop.preis;

import dev.triumphteam.gui.components.GuiAction;
import me.xxgrowguruxx.GUIReward;
import me.xxgrowguruxx.adminshop.change;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;

public class rechne100 {

    public static GuiAction<InventoryClickEvent> performAction(int slot, Player player) {
        return event -> {

            File slotsfile = new File("plugins/GUIReward/Shop/slots.yml");
            FileConfiguration slots = YamlConfiguration.loadConfiguration(slotsfile);

            ConfigurationSection slotSection = slots.getConfigurationSection(Integer.toString(slot));

            if (event.isRightClick()) {
                // Rechtsklick
                assert slotSection != null;
                int currentPrice = slotSection.getInt("Preis");
                int newPrice = currentPrice + 100;

                // Begrenze den Preis auf 0, falls er kleiner als 0 ist
                newPrice = Math.max(newPrice, 0);

                slotSection.set("Preis", newPrice);

                // Speichere die geänderte Konfigurationsdatei
                try {
                    slots.save(slotsfile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (event.isLeftClick()) {
                // Linksklick
                assert slotSection != null;
                int currentPrice = slotSection.getInt("Preis");
                int newPrice = currentPrice - 100;

                // Begrenze den Preis auf 0, falls er kleiner als 0 ist
                newPrice = Math.max(newPrice, 0);

                slotSection.set("Preis", newPrice);

                // Speichere die geänderte Konfigurationsdatei
                try {
                    slots.save(slotsfile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Schließe das Inventar und öffne es erneut
            player.closeInventory();
            change.performAction(slot, player, GUIReward.getInstance());
        };
    }
}