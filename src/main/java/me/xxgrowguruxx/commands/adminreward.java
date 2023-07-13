package me.xxgrowguruxx.commands;

import dev.triumphteam.gui.components.GuiAction;
import me.xxgrowguruxx.GUIReward;
import me.xxgrowguruxx.adminshop.add;
import me.xxgrowguruxx.adminshop.change;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class adminreward implements CommandExecutor {

    private final FileConfiguration messages;

    public adminreward() {
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

    private String getMessage(String key, Object... args) {
        String message = messages.getString(key);
        if (message != null) {
            return String.format(message, args);
        }
        return "";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("console"));
            return true;
        }

        Player player = (Player) sender;

        // Gui erstellen
        Gui adminshop = Gui.gui()
                .title(Component.text("§4§n§lFameShop - Admin"))
                .rows(6)
                .create();

        File slotsfile = new File("plugins/GUIReward/Shop/slots.yml");
        FileConfiguration slots = YamlConfiguration.loadConfiguration(slotsfile);

        if (!player.hasPermission("reward.shop")) {
            player.sendMessage(getMessage("noPermission"));
            return true;
        }

        ConfigurationSection slotsSection = slots.getConfigurationSection("");
        if (slotsSection != null) {
            for (String slot : slotsSection.getKeys(false)) {
                ConfigurationSection slotSection = slotsSection.getConfigurationSection(slot);
                if (slotSection != null) {
                    String material = slotSection.getString("Material");
                    int preis = slotSection.getInt("Preis");

                    if (material == null || Material.getMaterial(material) == null) {
                        GuiItem barrier = ItemBuilder.from(Material.BARRIER)
                                .name(Component.text(getMessage("emptySlot")))
                                .asGuiItem(click -> {
                                    add.loadMessages();
                                    GuiAction<InventoryClickEvent> action = add.performAction(Integer.parseInt(slot), player);
                                    action.execute(click);
                                });

                        adminshop.setItem(Integer.parseInt(slot), barrier);
                    } else {
                        int finalI = Integer.parseInt(slot);
                        GuiItem item = ItemBuilder.from(Material.valueOf(material))
                                .lore(Component.text(getMessage("price") + preis + " Fame"))
                                .asGuiItem(click -> {
                                    change.loadMessages();
                                    // Führe die Aktion mit dem Sender aus
                                    change.performAction(finalI, player, GUIReward.getInstance());
                                });

                        adminshop.setItem(Integer.parseInt(slot), item);
                    }
                }
            }
        }

        // Öffne das Inventar für den Spieler
        adminshop.disableAllInteractions();
        adminshop.open(player);

        return true;
    }
}
