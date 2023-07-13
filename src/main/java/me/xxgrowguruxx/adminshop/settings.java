package me.xxgrowguruxx.adminshop;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.xxgrowguruxx.GUIReward;
import me.xxgrowguruxx.adminshop.change;
import me.xxgrowguruxx.adminshop.config.Prozent;
import me.xxgrowguruxx.adminshop.config.Timer;
import me.xxgrowguruxx.adminshop.config.discountTimer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;

public class settings {

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

            Gui discount = Gui.gui()
                    .title(Component.text("§4§n§lFameShop - Admin"))
                    .rows(3)
                    .create();

            Component timerLore1 = Component.text(getMessage("TimerLore1"));
            Component timerLore2 = Component.text(getMessage("TimerLore2"));
            Component timerLore3 = Component.text(getMessage("TimerLore3"));
            GuiItem timer = ItemBuilder.from(Material.COMMAND_BLOCK)
                    .name(Component.text(getMessage("Timer")))
                    .lore(timerLore1, timerLore2, timerLore3)
                    .asGuiItem(click->{
                        Timer.loadMessages();
                        GuiAction<InventoryClickEvent> action = Timer.performAction(slot, player, instance);
                        action.execute(click); // Führt das Klickereignis aus
                    });

            Component discountLore1 = Component.text(getMessage("discountLore1"));
            Component discountLore2 = Component.text(getMessage("discountLore2"));
            Component discountLore3 = Component.text(getMessage("discountLore3"));
            GuiItem rabatt = ItemBuilder.from(Material.OAK_SIGN)
                    .name(Component.text(getMessage("discountTimer")))
                    .lore(discountLore1, discountLore2, discountLore3)
                    .asGuiItem(click->{
                        discountTimer.loadMessages();
                        GuiAction<InventoryClickEvent> action = discountTimer.performAction(slot, player, instance);
                        action.execute(click); // Führt das Klickereignis aus
                    });

            Component prozentLore1 = Component.text(getMessage("prozentLore1"));
            Component prozentLore2 = Component.text(getMessage("prozentLore2"));
            Component prozentLore3 = Component.text(getMessage("prozentLore3"));
            GuiItem prozent = ItemBuilder.from(Material.BEDROCK)
                    .name(Component.text(getMessage("Prozent")))
                    .lore(prozentLore1, prozentLore2, prozentLore3)
                    .asGuiItem(click->{
                        Prozent.loadMessages();
                        GuiAction<InventoryClickEvent> action = Prozent.performAction(slot, player, instance);
                        action.execute(click); // Führt das Klickereignis aus
                    });

            GuiItem back = ItemBuilder.from(Material.FEATHER)
                    .name(Component.text(getMessage("back")))
                    .asGuiItem(click ->{
                        change.performAction(slot, player, GUIReward.getInstance());
                    });

            GuiItem filler = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                    .glow()
                    .name(Component.text(""))
                    .asGuiItem();

            discount.disableAllInteractions();
            discount.setItem(2, 3, timer);
            discount.setItem(2, 5, rabatt);
            discount.setItem(2, 7, prozent);
            discount.setItem(3, 1, back);
            discount.getFiller().fill(filler);

            player.closeInventory();
            discount.open(player);
        };
    }
}
