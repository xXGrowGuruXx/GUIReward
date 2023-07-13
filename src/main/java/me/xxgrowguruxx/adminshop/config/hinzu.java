package me.xxgrowguruxx.adminshop.config;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.xxgrowguruxx.adminshop.add;
import me.xxgrowguruxx.commands.adminreward;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class hinzu {
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

            Gui ask = Gui.gui()
                    .title(Component.text(getMessage("sure")))
                    .rows(3)
                    .create();

            ItemStack clickedItem = event.getCurrentItem();

            assert clickedItem != null;
            GuiItem YES = ItemBuilder.from(Material.GREEN_WOOL)
                    .name(Component.text(getMessage("YesName")))
                    .lore(Component.text(getMessage("AskLore", clickedItem.getType().toString())))
                    .asGuiItem(click -> {
                        assert slotSection != null;
                        slotSection.set("Material", clickedItem.getType().toString());
                        try {
                            slots.save(slotsfile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        player.closeInventory();
                        adminreward adminShopCommand = new adminreward();
                        adminShopCommand.onCommand(player, null, null, null);
                    });

            GuiItem NO = ItemBuilder.from(Material.RED_WOOL)
                    .name(Component.text(getMessage("NoName")))
                    .lore(Component.text(getMessage("AskLore", clickedItem.getType().toString())))
                    .asGuiItem(click -> {
                        player.closeInventory();
                        add.loadMessages();
                        GuiAction<InventoryClickEvent> action = add.performAction(slot, player);
                        action.execute(click);
                    });

            GuiItem filler = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                    .glow()
                    .name(Component.text(""))
                    .asGuiItem();

            ask.disableAllInteractions();
            ask.setItem(2, 3, YES);
            ask.setItem(2, 7, NO);
            ask.getFiller().fill(filler);

            player.closeInventory();
            ask.open(player);
        };
    }
}
