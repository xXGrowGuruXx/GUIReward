package me.xxgrowguruxx.events;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class BuyEvent implements Listener {

    private final FileConfiguration messages;

    public BuyEvent() {
        // Lade die Konfigurationsdatei
        File configFile = new File("plugins/GUIReward/config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        // Lade die Sprache aus der Konfigurationsdatei
        String language = config.getString("language");

        // Lade die Nachrichtendatei basierend auf der Sprache
        File messagesFile = new File("plugins/GUIReward/messages/" + language + ".yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private String getMessage(String key, Object... args) {
        String message = messages.getString(key);
        if (message != null) {
            return String.format(message, args);
        }
        return "";
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        UUID uuid = Bukkit.getPlayerUniqueId(String.valueOf(player));

        if (event.getView().title().equals(Component.text("§4§n§lFameShop"))) {
            FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(new File("plugins/GUIReward/Shop/slots.yml"));
            FileConfiguration fameConfig = YamlConfiguration.loadConfiguration(new File("plugins/GUIReward/PlayerData/Fame.yml"));

            int clickedSlot = event.getSlot();
            if (itemConfig.isConfigurationSection(Integer.toString(clickedSlot))) {
                String materialList = itemConfig.getString(clickedSlot + ".Material");
                int preis = itemConfig.getInt(clickedSlot + ".Preis");
                boolean activeRabatt = itemConfig.getBoolean(clickedSlot + ".RabattAktiv");
                int rabatt = itemConfig.getInt(clickedSlot + ".RabattProzent");

                Material material = Material.valueOf(materialList);
                int preisRabatt = preis - (preis * rabatt / 100);
                int currentFame = fameConfig.getInt(uuid + ".balance");

                if (activeRabatt) {
                    // Prüfen, ob der Spieler genug Ressourcen hat, um das Item zu kaufen
                    if (currentFame >= preisRabatt) {
                        ItemStack item = new ItemStack(material, 1);
                        int stackSize = item.getMaxStackSize();
                        boolean itemAdded = false;

                        for (ItemStack inventoryItem : player.getInventory().getContents()) {
                            if (inventoryItem != null && inventoryItem.getType() == material && inventoryItem.getAmount() < stackSize) {
                                int remainingSpace = stackSize - inventoryItem.getAmount();
                                int buyAmount = Math.min(remainingSpace, item.getAmount());

                                inventoryItem.setAmount(inventoryItem.getAmount() + buyAmount);
                                item.setAmount(item.getAmount() - buyAmount);

                                itemAdded = true;
                                if (item.getAmount() <= 0) {
                                    break;
                                }
                            }
                        }

                        if (!itemAdded) {
                            boolean hasEmptySlot = player.getInventory().firstEmpty() != -1;
                            if (hasEmptySlot) {
                                player.getInventory().addItem(item);
                                itemAdded = true;
                            }
                        }

                        if (itemAdded) {
                            int newFame = currentFame - preisRabatt;
                            fameConfig.set(uuid + ".balance", newFame);
                            try {
                                fameConfig.save(new File("plugins/GUIReward/PlayerData/Fame.yml"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String nextFame = messages.getString("fame");
                            assert nextFame != null;
                            player.sendMessage(String.format(nextFame, newFame, "Fame"));
                        } else {
                            event.setCancelled(true);
                            player.sendMessage(getMessage("fullInv"));
                        }
                    } else {
                        player.sendMessage(getMessage("noMoney"));
                    }
                } else {
                    // Prüfen, ob der Spieler genug Ressourcen hat, um das Item zu kaufen
                    if (currentFame >= preis) {
                        ItemStack item = new ItemStack(material, 1);
                        int stackSize = item.getMaxStackSize();
                        boolean itemAdded = false;

                        for (ItemStack inventoryItem : player.getInventory().getContents()) {
                            if (inventoryItem != null && inventoryItem.getType() == material && inventoryItem.getAmount() < stackSize) {
                                int remainingSpace = stackSize - inventoryItem.getAmount();
                                int buyAmount = Math.min(remainingSpace, item.getAmount());

                                inventoryItem.setAmount(inventoryItem.getAmount() + buyAmount);
                                item.setAmount(item.getAmount() - buyAmount);

                                itemAdded = true;
                                if (item.getAmount() <= 0) {
                                    break;
                                }
                            }
                        }

                        if (!itemAdded) {
                            boolean hasEmptySlot = player.getInventory().firstEmpty() != -1;
                            if (hasEmptySlot) {
                                player.getInventory().addItem(item);
                                itemAdded = true;
                            }
                        }

                        if (itemAdded) {
                            int newFame = currentFame - preis;
                            fameConfig.set(uuid + ".balance", newFame);
                            try {
                                fameConfig.save(new File("plugins/GUIReward/PlayerData/Fame.yml"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String nextFame = messages.getString("fame");
                            assert nextFame != null;
                            player.sendMessage(String.format(nextFame, newFame, "Fame"));
                        } else {
                            event.setCancelled(true);
                            player.sendMessage(getMessage("fullInv"));
                        }
                    } else {
                        player.sendMessage(getMessage("noMoney"));
                    }
                }
            }
        }
    }
}
