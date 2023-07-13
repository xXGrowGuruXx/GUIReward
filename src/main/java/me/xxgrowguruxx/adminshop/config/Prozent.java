package me.xxgrowguruxx.adminshop.config;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.xxgrowguruxx.GUIReward;
import me.xxgrowguruxx.adminshop.settings;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.xxgrowguruxx.adminshop.prozent.get5;
import me.xxgrowguruxx.adminshop.prozent.get10;
import me.xxgrowguruxx.adminshop.prozent.get50;

import java.io.File;

public class Prozent {

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

            File slotsfile = new File("plugins/GUIReward/Shop/slots.yml");
            FileConfiguration slots = YamlConfiguration.loadConfiguration(slotsfile);

            // Überprüfe, ob der angeklickte Slot in der Konfigurationsdatei vorhanden ist
            ConfigurationSection slotSection = slots.getConfigurationSection(Integer.toString(slot));

            assert slotSection != null;
            int prozente = slotSection.getInt("RabattProzent");

            Gui proponent = Gui.gui()
                    .title(Component.text("§4§n§lFameShop - Admin"))
                    .rows(3)
                    .create();

            Component prozentLore = Component.text(prozente + "%");
            GuiItem seeProzent = ItemBuilder.from(Material.BEDROCK)
                    .name(Component.text(getMessage("Prozent")))
                    .lore(prozentLore)
                    .asGuiItem();

            Component add5Lore1 = Component.text(getMessage("get5RECHTS"));
            Component get5Lore2 = Component.text(getMessage("get5LINKS"));
            GuiItem get5ITEM = ItemBuilder.from(Material.BEDROCK)
                    .name(Component.text(getMessage("get5Name")))
                    .lore(add5Lore1, get5Lore2)
                    .asGuiItem(click->{
                        GuiAction<InventoryClickEvent> action = get5.performAction(slot, player, instance);
                        action.execute(click); // Führt das Klickereignis aus
                    });

            Component add10Lore1 = Component.text(getMessage("get10RECHTS"));
            Component add10Lore2 = Component.text(getMessage("get10LINKS"));

            GuiItem add10 = ItemBuilder.from(Material.BEDROCK)
                    .name(Component.text(getMessage("get10Name")))
                    .lore(add10Lore1, add10Lore2)
                    .asGuiItem(get10.performAction(slot, player, instance));

            Component get50Lore1 = Component.text(getMessage("get50RECHTS"));
            Component get50Lore2 = Component.text(getMessage("get50LINKS"));
            GuiItem get50ITEM = ItemBuilder.from(Material.BEDROCK)
                    .name(Component.text(getMessage("get50Name")))
                    .lore(get50Lore1, get50Lore2)
                    .asGuiItem(click->{
                        GuiAction<InventoryClickEvent> action = get50.performAction(slot, player, instance);
                        action.execute(click); // Führt das Klickereignis aus
                    });

            GuiItem back = ItemBuilder.from(Material.FEATHER)
                    .name(Component.text("back"))
                    .asGuiItem(click->{
                        settings.loadMessages();
                        GuiAction<InventoryClickEvent> action = settings.performAction(slot, player, instance);
                        action.execute(click); // Führt das Klickereignis aus
                    });

            GuiItem filler = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                    .glow()
                    .name(Component.text(""))
                    .asGuiItem();

            proponent.disableAllInteractions();
            proponent.setItem(1, 5, seeProzent);
            proponent.setItem(3, 3, get5ITEM);
            proponent.setItem(3, 5, add10);
            proponent.setItem(3, 7, get50ITEM);
            proponent.setItem(3, 1, back);
            proponent.getFiller().fill(filler);

            player.closeInventory();
            proponent.open(player);
        };
    }
}
