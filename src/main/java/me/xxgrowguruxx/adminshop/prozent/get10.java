package me.xxgrowguruxx.adminshop.prozent;

import dev.triumphteam.gui.components.GuiAction;
import me.xxgrowguruxx.GUIReward;
import me.xxgrowguruxx.adminshop.config.Prozent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;

public class get10 {
    public static GuiAction<InventoryClickEvent> performAction(int slot, Player player, GUIReward instance) {
        return event -> {

            File slotsfile = new File("plugins/GUIReward/Shop/slots.yml");
            FileConfiguration slots = YamlConfiguration.loadConfiguration(slotsfile);

            ConfigurationSection slotSection = slots.getConfigurationSection(Integer.toString(slot));

            if (event.isRightClick()) {
                // Rechtsklick
                assert slotSection != null;
                int currentPrice = slotSection.getInt("RabattProzent");
                int newPrice = currentPrice + 10;

                // Begrenze den Preis auf 0, falls er kleiner als 0 ist
                newPrice = Math.max(newPrice, 0);

                slotSection.set("RabattProzent", newPrice);

                // Speichere die geänderte Konfigurationsdatei
                try {
                    slots.save(slotsfile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (event.isLeftClick()) {
                // Linksklick
                assert slotSection != null;
                int currentPrice = slotSection.getInt("RabattProzent");
                int newPrice = currentPrice - 10;

                // Begrenze den Preis auf 0, falls er kleiner als 0 ist
                newPrice = Math.max(newPrice, 0);

                slotSection.set("RabattProzent", newPrice);

                // Speichere die geänderte Konfigurationsdatei
                try {
                    slots.save(slotsfile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Schließe das Inventar und öffne es erneut
            player.closeInventory();
            GuiAction<InventoryClickEvent> action = Prozent.performAction(slot, player, instance);
            action.execute(event);
        };
    }
}
