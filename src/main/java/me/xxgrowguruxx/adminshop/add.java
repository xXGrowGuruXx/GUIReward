package me.xxgrowguruxx.adminshop;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.xxgrowguruxx.adminshop.config.hinzu;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class add {

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

    public static GuiAction<InventoryClickEvent> performAction(int slot, Player player) {
        return event -> {
            File slotsfile = new File("plugins/GUIReward/Shop/slots.yml");
            FileConfiguration slots = YamlConfiguration.loadConfiguration(slotsfile);

            // Überprüfe, ob der angeklickte Slot in der Konfigurationsdatei vorhanden ist
            ConfigurationSection slotSection = slots.getConfigurationSection(Integer.toString(slot));

            Gui playerInv = Gui.gui()
                    .title(Component.text(getMessage("Inv")))
                    .rows(6)
                    .create();

            List<GuiItem> guiItems = getInventoryItems(player); // Lade die Inventaritems

            for (GuiItem item : guiItems) {
                item.setAction(click -> {
                    hinzu.loadMessages();
                    GuiAction<InventoryClickEvent> action = hinzu.performAction(slot, player);
                    action.execute(click);
                });
                playerInv.addItem(item); // Füge das Item zum playerInv hinzu
            }

            GuiItem filler = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                    .glow()
                    .name(Component.text(""))
                    .asGuiItem();

            playerInv.disableAllInteractions();
            playerInv.getFiller().fill(filler);
            player.closeInventory();
            playerInv.open(player);
        };
    }

    private static List<GuiItem> getInventoryItems(Player player) {
        List<GuiItem> guiItems = new ArrayList<>();

        // Hole das Inventar des Spielers
        Inventory playerInventory = player.getInventory();
        ItemStack[] contents = playerInventory.getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null) {
                ItemMeta itemMeta = item.getItemMeta();
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text(getMessage("choose")));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                // Konvertiere den ItemStack in ein GuiItem und füge es zur Liste hinzu
                guiItems.add(new GuiItem(item));
            }
        }

        return guiItems;
    }
}
