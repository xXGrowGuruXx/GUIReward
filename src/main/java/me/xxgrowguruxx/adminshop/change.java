package me.xxgrowguruxx.adminshop;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.xxgrowguruxx.GUIReward;
import me.xxgrowguruxx.commands.adminreward;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.xxgrowguruxx.adminshop.preis.rechne10;
import me.xxgrowguruxx.adminshop.preis.rechne100;
import me.xxgrowguruxx.adminshop.preis.rechne1000;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class change {

    public static ItemStack item;
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

    public static void performAction(int slot, Player player, GUIReward instance) {
        File slotsfile = new File("plugins/GUIReward/Shop/slots.yml");
        FileConfiguration slots = YamlConfiguration.loadConfiguration(slotsfile);

        // Überprüfe, ob der angeklickte Slot in der Konfigurationsdatei vorhanden ist
        ConfigurationSection slotSection = slots.getConfigurationSection(Integer.toString(slot));
        if (slotSection != null) {
            boolean isRabattAktiv = slotSection.getBoolean("RabattAktiv");

            activateDiscount(slotSection,slots,slotsfile,player,instance);

            // Gui erstellen
            Gui change = Gui.gui()
                    .title(Component.text("§4§n§lFameShop - Admin"))
                    .rows(4)
                    .create();

            GuiItem delete = ItemBuilder.from(Material.BARRIER)
                    .name(Component.text(getMessage("delete")))
                    .asGuiItem(event -> {
                        // Leere den Eintrag bei .Material in slots.yml
                        slotSection.set(".Material", "empty");
                        // Speichere die geänderte Konfigurationsdatei
                        try {
                            slots.save(slotsfile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        player.closeInventory();

                        adminreward adminShopCommand = new adminreward();
                        adminShopCommand.onCommand(player, null, null, null);
                    });

            Component add10Lore1 = Component.text(getMessage("add10RECHTS"));
            Component add10Lore2 = Component.text(getMessage("add10LINKS"));

            GuiItem add10 = ItemBuilder.from(Material.IRON_INGOT)
                    .name(Component.text(getMessage("add10Name")))
                    .lore(add10Lore1, add10Lore2)
                    .asGuiItem(rechne10.performAction(slot, player));

            Component add100Lore1 = Component.text(getMessage("add100RECHTS"));
            Component add100Lore2 = Component.text(getMessage("add100LINKS"));

            GuiItem add100 = ItemBuilder.from(Material.GOLD_INGOT)
                    .name(Component.text(getMessage("add100Name")))
                    .lore(add100Lore1, add100Lore2)
                    .asGuiItem(rechne100.performAction(slot, player));

            Component add1000Lore1 = Component.text(getMessage("add1000RECHTS"));
            Component add1000Lore2 = Component.text(getMessage("add1000LINKS"));

            GuiItem add1000 = ItemBuilder.from(Material.DIAMOND)
                    .name(Component.text(getMessage("add1000Name")))
                    .lore(add1000Lore1, add1000Lore2)
                    .asGuiItem(rechne1000.performAction(slot, player));

            GuiItem back = ItemBuilder.from(Material.FEATHER)
                    .name(Component.text(getMessage("back")))
                    .asGuiItem(event -> {
                        adminreward adminShopCommand = new adminreward();
                        adminShopCommand.onCommand(player, null, null, null);
                    });

            GuiItem onANDof = ItemBuilder.from(Material.EMERALD_BLOCK)
                    .name(Component.text(getMessage("rabatte")))
                    .asGuiItem(event -> {
                        // bei klick ändere aufs gegenteil
                        slotSection.set("RabattAktiv", !isRabattAktiv); // Invertiere den Wert von "RabattAktiv"
                        try {
                            slots.save(slotsfile); // Speichere die geänderte Konfigurationsdatei
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        player.closeInventory();
                        performAction(slot, player, GUIReward.getInstance());
                    });

            Component settingLore1 = Component.text(getMessage("settingLORE1"));
            Component settingLore2 = Component.text(getMessage("settingLORE2"));

            GuiItem setting = ItemBuilder.from(Material.EMERALD)
                    .name(Component.text(getMessage("settings")))
                    .lore(settingLore1, settingLore2)
                    .asGuiItem(click -> {
                        settings.loadMessages();
                        GuiAction<InventoryClickEvent> action = settings.performAction(slot, player, instance);
                        action.execute(click); // Führt das Klickereignis aus
                    });
            GuiItem info = new GuiItem(item);

            GuiItem filler = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                    .glow()
                    .name(Component.text(""))
                    .asGuiItem();

            change.setItem(2, 5, info);
            change.setItem(4, 9, delete);
            change.setItem(4, 3, add10);
            change.setItem(4, 5, add100);
            change.setItem(4, 7, add1000);
            change.setItem(4, 1, back);
            change.setItem(2, 3, setting);
            change.setItem(2, 7, onANDof);
            change.getFiller().fill(filler);

            change.disableAllInteractions();

            player.closeInventory();
            change.open(player);
        }
    }

    private static void activateTimer(ConfigurationSection slotSection, FileConfiguration slots, File slotsfile, Player player, GUIReward instance) {
        // Überprüfe, ob die Option "Timer" in der Konfigurationsdatei vorhanden ist
        if (slotSection.contains("Timer")) {
            // Hole den Wert der Option "Timer"
            String timerValue = slotSection.getString("Timer");
            // Überprüfe, ob die Zeit im 24-Stunden-Format vorliegt
            assert timerValue != null;
            if (isValid24HourFormat(timerValue)) {
                // Zerlege die Timer-Zeit in Stunden und Minuten
                String[] timeParts = timerValue.split(":");
                int hours = Integer.parseInt(timeParts[0]);
                int minutes = Integer.parseInt(timeParts[1]);

                // Berechne die Gesamtanzahl der Millisekunden
                long totalMilliseconds = ((hours * 60L + minutes) * 60L) * 1000L;
                // Berechne die Anzahl der Ticks (1 Tick = 1/20 Sekunde)
                long delayTicks = totalMilliseconds / 50L;

                Bukkit.getScheduler().runTaskLater(instance, () -> {
                    // Rabatt aktivieren und den Timer starten
                    slotSection.set("RabattAktiv", true);
                    try {
                        slots.save(slotsfile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    activateDiscount(slotSection, slots, slotsfile, player,  instance);
                    System.out.println("Rabatt startet");
                }, delayTicks);
            }
        }
    }

    private static void activateDiscount (ConfigurationSection slotSection, FileConfiguration slots, File slotsfile, Player player, GUIReward instance) {
        String material = slotSection.getString("Material");
        int preis = slotSection.getInt("Preis");

        // Erstelle ein ItemStack mit dem Material
        ItemStack itemStack = new ItemStack(Material.valueOf(material));
        // Erstelle ein leeres Lore-List-Objekt
        List<Component> lore = new ArrayList<>();
        // Überprüfe, ob der Rabatt aktiv ist
        boolean isRabattAktiv = slotSection.getBoolean("RabattAktiv");
        if (isRabattAktiv) {
            // Rabatt ist aktiv, berechne den rabattierten Preis
            double rabattProzent = slotSection.getDouble("RabattProzent");
            double rabattierterPreis = preis - (preis * rabattProzent / 100);
            // Füge den rabattierten Preis zur Lore hinzu
            lore.add(Component.text("§m" + (getMessage("price") + preis + " Fame")));
            lore.add(Component.text(getMessage("rabatt") + String.format("%.2f", rabattierterPreis) + " Fame"));
            // Überprüfe, ob die Option "discountTime" in der Konfigurationsdatei vorhanden ist
            if (slotSection.contains("discountTime")) {
                // Hole den Wert der Option "discountTime"
                String discountTime = slotSection.getString("discountTime");
                // Überprüfe, ob die Zeit im 24-Stunden-Format vorliegt
                assert discountTime != null;
                if (isValid24HourFormat(discountTime)) {
                    // Zerlege die discountTime in Stunden und Minuten
                    String[] timeParts = discountTime.split(":");
                    int hours = Integer.parseInt(timeParts[0]);
                    int minutes = Integer.parseInt(timeParts[1]);

                    // Berechne die Gesamtanzahl der Millisekunden
                    long totalMilliseconds = ((hours * 60L + minutes) * 60L) * 1000L;
                    // Berechne die Anzahl der Ticks (1 Tick = 1/20 Sekunde)
                    long delayTicks = totalMilliseconds / 50L;

                    String displayTime = generateDisplayTime(hours, minutes);
                    lore.add(Component.text(" " + displayTime));

                    Bukkit.getScheduler().runTaskLater(instance, () -> {
                        // Rabatt beenden und zum normalen Preis zurückkehren
                        slotSection.set("RabattAktiv", false);
                        try {
                            slots.save(slotsfile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Timer aktivieren
                        activateTimer(slotSection, slots, slotsfile, player,  instance);
                        System.out.println("Rabatt endet");
                    }, delayTicks);
                }
            }
        } else {
            // Rabatt ist nicht aktiv, zeige nur den normalen Preis in der Lore an
            lore.add(Component.text(getMessage("price") + preis + " Fame"));
        }
        // Setze die Lore des ItemStacks
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        // Konvertiere den ItemStack in ein GuiItem
        item = new ItemStack(itemStack);

    }

    private static boolean isValid24HourFormat(String time) {
        return time.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]");
    }

    private static String generateDisplayTime(int hours, int minutes) {
        String displayTime = "";
        if (hours > 0) {
            displayTime += hours + getMessage("Hours");
        }
        if (minutes > 0) {
            displayTime += minutes + getMessage("Minutes");
        }
        return displayTime.trim();
    }
}
