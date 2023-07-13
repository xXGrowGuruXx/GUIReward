package me.xxgrowguruxx.adminshop.config;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.xxgrowguruxx.GUIReward;
import me.xxgrowguruxx.adminshop.settings;
import me.xxgrowguruxx.adminshop.timer.setze1;
import me.xxgrowguruxx.adminshop.timer.setze10;
import me.xxgrowguruxx.adminshop.timer.setze30;
import me.xxgrowguruxx.adminshop.timer.setze5;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Timer {

    private static FileConfiguration messages;

    public static void loadMessages() {
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

    private static String getMessage(String key, Object... args) {
        String message = messages.getString(key);
        if (message != null) {
            return String.format(message, args);
        }
        return "";
    }

    public static GuiAction<InventoryClickEvent> performAction(int slot, Player player, GUIReward instance) {
        return event -> {
            // Lade die Slot-Konfigurationsdatei
            File slotsFile = new File("plugins/GUIReward/Shop/slots.yml");
            FileConfiguration slots = YamlConfiguration.loadConfiguration(slotsFile);
            // Überprüfe, ob der angeklickte Slot in der Konfigurationsdatei vorhanden ist
            ConfigurationSection slotSection = slots.getConfigurationSection(Integer.toString(slot));

            Gui timerConfig = Gui.gui()
                    .title(Component.text("§4§n§lFameShop - Admin"))
                    .rows(3)
                    .create();

            ItemStack itemStack = new ItemStack(Material.COMMAND_BLOCK);
            List<Component> lore = new ArrayList<>();

                if (slotSection != null) {
                    String timer = slotSection.getString("Timer");
                        // Zerlege die Timer in Stunden und Minuten
                        assert timer != null;
                        String[] timeParts = timer.split(":");
                        int hours = Integer.parseInt(timeParts[0]);
                        int minutes = Integer.parseInt(timeParts[1]);

                        String displayTime = generateDisplayTime(hours, minutes);
                        lore.add(Component.text(" " + displayTime));
                    }

                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(Component.text("§fTimer"));
                itemMeta.lore(lore);
                itemStack.setItemMeta(itemMeta);

                // Konvertiere den ItemStack in ein GuiItem
                GuiItem seeTimer = new GuiItem(itemStack);

                Component set10Lore1 = Component.text(getMessage("set10RECHTS"));
                Component set10Lore2 = Component.text(getMessage("set10LINKS"));
                GuiItem set10 = ItemBuilder.from(Material.COMMAND_BLOCK)
                        .name(Component.text(getMessage("set10Name")))
                        .lore(set10Lore1, set10Lore2)
                        .asGuiItem(click -> {
                            GuiAction<InventoryClickEvent> action = setze10.performAction(slot, player, instance);
                            action.execute(click); // Führt das Klickereignis aus
                        });

            Component set30Lore1 = Component.text(getMessage("set30RECHTS"));
            Component set30Lore2 = Component.text(getMessage("set30LINKS"));
            GuiItem set30 = ItemBuilder.from(Material.COMMAND_BLOCK)
                    .name(Component.text(getMessage("set30Name")))
                    .lore(set30Lore1, set30Lore2)
                    .asGuiItem(click -> {
                        GuiAction<InventoryClickEvent> action = setze30.performAction(slot, player, instance);
                        action.execute(click); // Führt das Klickereignis aus
                    });

            Component set1Lore1 = Component.text(getMessage("set1RECHTS"));
            Component set1Lore2 = Component.text(getMessage("set1LINKS"));
            GuiItem set1 = ItemBuilder.from(Material.COMMAND_BLOCK)
                    .name(Component.text(getMessage("set1Name")))
                    .lore(set1Lore1, set1Lore2)
                    .asGuiItem(click -> {
                        GuiAction<InventoryClickEvent> action = setze1.performAction(slot, player, instance);
                        action.execute(click); // Führt das Klickereignis aus
                    });

            Component set5Lore1 = Component.text(getMessage("set5RECHTS"));
            Component set5Lore2 = Component.text(getMessage("set5LINKS"));
            GuiItem set5 = ItemBuilder.from(Material.COMMAND_BLOCK)
                    .name(Component.text(getMessage("set5Name")))
                    .lore(set5Lore1, set5Lore2)
                    .asGuiItem(click -> {
                        GuiAction<InventoryClickEvent> action = setze5.performAction(slot, player, instance);
                        action.execute(click); // Führt das Klickereignis aus
                    });

            GuiItem back = ItemBuilder.from(Material.FEATHER)
                    .name(Component.text(getMessage("back")))
                    .asGuiItem(click -> {
                        GuiAction<InventoryClickEvent> action = settings.performAction(slot, player, instance);
                        action.execute(click); // Führt das Klickereignis aus
                    });

            GuiItem filler = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                    .glow()
                    .name(Component.text(""))
                    .asGuiItem();

                timerConfig.disableAllInteractions();
                timerConfig.setItem(3, 1, back);
                timerConfig.setItem(1, 5, seeTimer);
                timerConfig.setItem(3, 3, set10);
                timerConfig.setItem(3, 5, set30);
                timerConfig.setItem(3, 7, set1);
                timerConfig.setItem(3, 9, set5);
                timerConfig.getFiller().fill(filler);

                player.closeInventory();
                timerConfig.open(player);
            };
        }

    private static String generateDisplayTime(int hours, int minutes) {
        String hoursText = getMessage("Hours");
        String minutesText = getMessage("Minutes");

        StringBuilder displayTime = new StringBuilder();
        if (hours > 0) {
            displayTime.append(hours).append(" ").append(hoursText).append(", ");
        }
        displayTime.append(minutes).append(" ").append(minutesText);

        return displayTime.toString();
    }
}
