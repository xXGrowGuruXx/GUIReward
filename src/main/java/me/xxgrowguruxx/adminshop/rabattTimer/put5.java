package me.xxgrowguruxx.adminshop.rabattTimer;

import dev.triumphteam.gui.components.GuiAction;
import me.xxgrowguruxx.GUIReward;
import me.xxgrowguruxx.adminshop.config.Timer;
import me.xxgrowguruxx.adminshop.config.discountTimer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;

public class put5 {
    public static GuiAction<InventoryClickEvent> performAction(int slot, Player player, GUIReward instance) {
        return event -> {

            File slotsfile = new File("plugins/GUIReward/Shop/slots.yml");
            FileConfiguration slots = YamlConfiguration.loadConfiguration(slotsfile);

            ConfigurationSection slotSection = slots.getConfigurationSection(Integer.toString(slot));

            if (event.isRightClick()) {
                // Rechtsklick
                assert slotSection != null;
                String currentTimer = slotSection.getString("discountTime");
                assert currentTimer != null;
                String[] timerParts = currentTimer.split(":");

                if (timerParts.length != 2) {
                    return; // Abbrechen, wenn das Timer-Format ungültig ist
                }

                int hours = Integer.parseInt(timerParts[0]);
                int minutes = Integer.parseInt(timerParts[1]);

                // Erhöhe die Stunden um 10
                hours += 5;

                // Überprüfe, ob die Minuten 60 oder mehr sind
                if (minutes >= 60) {
                    minutes %= 60; // Setze die Minuten auf den Restwert nach der Division durch 60
                    hours++; // Erhöhe die Stunden um 1
                }

                // Setze den neuen Timer
                String newTimer = String.format("%02d:%02d", hours, minutes);
                slotSection.set("discountTime", newTimer);

                // Speichere die geänderte Konfigurationsdatei
                try {
                    slots.save(slotsfile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (event.isLeftClick()) {
                // Linksklick
                assert slotSection != null;
                String currentTimer = slotSection.getString("discountTime");
                assert currentTimer != null;
                String[] timerParts = currentTimer.split(":");

                if (timerParts.length != 2) {
                    return; // Abbrechen, wenn das Timer-Format ungültig ist
                }

                int hours = Integer.parseInt(timerParts[0]);
                int minutes = Integer.parseInt(timerParts[1]);

                // Verringere die Stunden um 10
                hours -= 5;

                // Überprüfe, ob die Minuten kleiner als 0 sind
                if (minutes < 0) {
                    minutes += 60; // Addiere 60, um eine positive Minutenzahl zu erhalten
                    hours--; // Verringere die Stunden um 1
                }

                // Überprüfe, ob die Stunden kleiner als 0 sind
                if (hours < 0) {
                    hours += 24; // Addiere 24, um eine positive Stundenzahl zu erhalten
                }

                // Setze den neuen Timer
                String newTimer = String.format("%02d:%02d", hours, minutes);
                slotSection.set("discountTime", newTimer);

                // Speichere die geänderte Konfigurationsdatei
                try {
                    slots.save(slotsfile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            player.closeInventory();
            GuiAction<InventoryClickEvent> action = discountTimer.performAction(slot, player, instance);
            action.execute(event);
        };
    }
}
