package me.xxgrowguruxx.events;

import dev.triumphteam.gui.components.GuiAction;
import me.xxgrowguruxx.utils.Inv;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FameShop implements Listener {

    private static FileConfiguration messages;

    public FameShop() {
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Überprüfen, ob der Klick von einem Spieler stammt
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInv = event.getClickedInventory();

        // Überprüfen, ob der Klick im eigenen Inventar des Spielers stattfindet
        if (clickedInv == player.getInventory()) {
            // Aktion im eigenen Inventar durchführen
            return;
        }

        // Überprüfen, ob der Klick im Inventar vom Besitzer des Inv-Objekts stattfindet
        if (clickedInv != null && clickedInv.getHolder() instanceof Inv) {
            // Aktion im Inventar abbrechen
            event.setCancelled(true);
        }
    }

    public static GuiAction<InventoryClickEvent> performAction() {
        return event -> {
            // Erstellt die Shop GUI
            Inventory shop = Bukkit.createInventory(new Inv(), 6 * 9, Component.text("§4§n§lFameShop"));

            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();

                if (player.isOp()) {
                    player.closeInventory();
                    event.setCancelled(true);
                    player.sendMessage(getMessage("OPShop"));
                } else {
                    player.closeInventory();
                    player.openInventory(shop);

                    // Lädt Items aus der Shop.yml
                    File shopFile = new File("plugins/GUIReward/Shop/slots.yml");
                    FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(shopFile);

                    for (int i = 0; i < 54; i++) {
                        if (!itemConfig.isConfigurationSection(Integer.toString(i))) {
                            break;
                        }

                        String material = itemConfig.getString(i + ".Material");
                        String timer = itemConfig.getString(i+ ".discountTime");
                        int rabatt = itemConfig.getInt(i + ".RabattProzent");
                        int preis = itemConfig.getInt(i + ".Preis");
                        boolean activeRabatt = itemConfig.getBoolean(i + ".RabattAktiv");

                        if (material == null || preis == 0 || !isMaterialValid(material)) {
                            ItemStack barrier = new ItemStack(Material.BARRIER);
                            ItemMeta barrierMeta = barrier.getItemMeta();
                            barrierMeta.displayName(Component.text(getMessage("emptySlot")));
                            barrier.setItemMeta(barrierMeta);
                            shop.setItem(i, barrier);
                        } else {
                            ItemStack item = new ItemStack(Material.valueOf(material));
                            ItemMeta meta = item.getItemMeta();
                            meta.displayName(Component.text(material));
                            List<Component> lore = new ArrayList<>();

                            if (activeRabatt){
                                int preisRabatt = preis - (preis * rabatt / 100);

                                // Zerlege die Timer in Stunden und Minuten
                                assert timer != null;
                                String[] timeParts = timer.split(":");
                                int hours = Integer.parseInt(timeParts[0]);
                                int minutes = Integer.parseInt(timeParts[1]);

                                lore.add(Component.text("§m" + getMessage("price") + preis + " Fame"));
                                lore.add(Component.text(getMessage("rabatt") + rabatt + "%"));
                                lore.add(Component.text(getMessage("price") + preisRabatt + " Fame"));
                                String displayTime = generateDisplayTime(hours, minutes);
                                lore.add(Component.text(" " + displayTime));
                            }else {
                                lore.add(Component.text(getMessage("price") + preis + " Fame"));
                            }
                            meta.lore(lore);
                            item.setItemMeta(meta);
                            shop.setItem(i, item);
                        }
                    }
                }
            }
        };
    }

    private static boolean isMaterialValid(String materialName) {
        try {
            Material.valueOf(materialName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
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