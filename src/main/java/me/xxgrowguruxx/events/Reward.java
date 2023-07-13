package me.xxgrowguruxx.events;

import dev.triumphteam.gui.components.GuiAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class Reward implements Listener {

    private final FileConfiguration messages;

    public Reward() {
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

    private boolean isPlayerInCooldown(Player player) {
        UUID playerID = Bukkit.getPlayerUniqueId(String.valueOf(player));
        File cooldownFile = new File("plugins/GUIReward/PlayerData/cooldowns.yml");
        YamlConfiguration cooldownConfig = YamlConfiguration.loadConfiguration(cooldownFile);

        // Überprüfe, ob der Spieler in der cooldowns.yml vorhanden ist
        assert playerID != null;
        return cooldownConfig.getKeys(false).contains(playerID.toString());
    }

    private boolean hasFreeInventorySlot(Player player) {
        Inventory inventory = player.getInventory();
        return inventory.firstEmpty() != -1;
    }

    private String formatDuration(long durationMillis, String language) {
        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (language.equals("de_DE")) {
            return String.format("%d Stunden, %d Minuten, %d Sekunden", hours, minutes % 60, seconds % 60);
        } else if (language.equals("en_EN")) {
            return String.format("%d Hours, %d Minutes, %d Seconds", hours, minutes % 60, seconds % 60);
        }
        return "";
    }

    public GuiAction<InventoryClickEvent> performAction() {
        return event -> {
            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();

                // Spieler anhand der UUID erhalten
                UUID playerID = Bukkit.getPlayerUniqueId(String.valueOf(player));

                String permission1 = "reward.cooldown";

                // Lade die Konfigurationsdatei
                File configFile = new File("plugins/GUIReward/config.yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                // Lade die Sprache aus der Konfigurationsdatei
                String language = config.getString("language");
                FileConfiguration menge = YamlConfiguration.loadConfiguration(configFile);

                // Überprüfe, ob der Spieler die Berechtigung 1 hat
                if (player.hasPermission(permission1)) {
                    // Führe den Code aus, unabhängig von einem Cooldown
                    // Wähle ein zufälliges Material aus
                    Material selectedMaterial = Objects.requireNonNull(getRandomItemStack(player)).getType();
                    boolean hasItem = hasItemInInventory(player, selectedMaterial);
                    int maxQuantity = menge.getInt("maxQuantity");
                    int randomQuantity = new Random().nextInt(maxQuantity) + 1;

                    if (hasItem) {
                        boolean isFull = isStackFull(player, selectedMaterial);
                        if (isFull) {
                            if (hasFreeInventorySlot(player)) {
                                // Füge das Item zum Inventar hinzu
                                player.getInventory().addItem(new ItemStack(selectedMaterial, randomQuantity));

                                // Lade die Nachrichtendatei basierend auf der Sprache
                                File messagesFile = new File("plugins/GUIReward/messages/" + language + ".yml");
                                FileConfiguration reward = YamlConfiguration.loadConfiguration(messagesFile);
                                // Lade die Nachricht basierend auf der Sprache
                                String getReward = reward.getString("getReward");
                                // Sende dem Spieler die Nachricht
                                assert getReward != null;
                                player.sendMessage(String.format(getReward, randomQuantity, selectedMaterial));

                            } else {
                                event.setCancelled(true);
                                player.sendMessage(getMessage("fullInv"));
                            }
                        } else {
                            addItemToStack(player, selectedMaterial, randomQuantity);

                            // Lade die Nachrichtendatei basierend auf der Sprache
                            File messagesFile = new File("plugins/GUIReward/messages/" + language + ".yml");
                            FileConfiguration reward = YamlConfiguration.loadConfiguration(messagesFile);
                            // Lade die Nachricht basierend auf der Sprache
                            String getReward = reward.getString("getReward");
                            // Sende dem Spieler die Nachricht
                            assert getReward != null;
                            player.sendMessage(String.format(getReward, randomQuantity, selectedMaterial));
                        }
                    } else {
                        if (hasFreeInventorySlot(player)) {
                            // Gebe dem Spieler das Item
                            player.getInventory().addItem(new ItemStack(selectedMaterial, randomQuantity));

                            // Lade die Nachrichtendatei basierend auf der Sprache
                            File messagesFile = new File("plugins/GUIReward/messages/" + language + ".yml");
                            FileConfiguration reward = YamlConfiguration.loadConfiguration(messagesFile);
                            // Lade die Nachricht basierend auf der Sprache
                            String getReward = reward.getString("getReward");
                            // Sende dem Spieler die Nachricht
                            assert getReward != null;
                            player.sendMessage(String.format(getReward, randomQuantity, selectedMaterial));
                        } else {
                            event.setCancelled(true);
                            player.sendMessage(getMessage("fullInv"));
                        }
                    }
                } else {
                    // Der Spieler hat die Berechtigung 1 nicht, daher Cooldown-Prüfung durchführen
                    // Überprüfe, ob der Spieler bereits in der cooldowns.yml vorhanden ist
                    assert playerID != null;
                    if (isPlayerInCooldown(player)) {
                        // Holen Sie den Cooldown-Ablaufzeitpunkt des Spielers
                        long cooldownExpiration = getCooldownExpiration(playerID);
                        long remainingCooldownMillis = cooldownExpiration - System.currentTimeMillis();

                        // Formatieren Sie die verbleibende Dauer des Cooldowns
                        assert language != null;
                        String remainingCooldown = formatDuration(remainingCooldownMillis, language);
                        // Lade die Nachrichtendatei basierend auf der Sprache
                        File messagesFile = new File("plugins/GUIReward/messages/" + language + ".yml");
                        FileConfiguration messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
                        // Lade die Nachricht basierend auf der Sprache
                        String cooldownMessage = messagesConfig.getString("cooldown");

                        // Sende dem Spieler die Nachricht
                        assert cooldownMessage != null;
                        player.sendMessage(String.format(cooldownMessage, remainingCooldown));
                        // Brechen Sie die Aktion ab
                        event.setCancelled(true);
                    } else {
                        // Der Spieler ist noch nicht in der cooldowns.yml
                        // Wähle ein zufälliges Material aus
                        Material selectedMaterial = Objects.requireNonNull(getRandomItemStack(player)).getType();
                        boolean hasItem = hasItemInInventory(player, selectedMaterial);
                        int maxQuantity = menge.getInt("maxQuantity");
                        int randomQuantity = new Random().nextInt(maxQuantity) + 1;

                        if (hasItem) {
                            boolean isFull = isStackFull(player, selectedMaterial);
                            if (isFull) {
                                if (hasFreeInventorySlot(player)) {
                                    // Füge das Item zum Inventar hinzu
                                    player.getInventory().addItem(new ItemStack(selectedMaterial, randomQuantity));

                                    // Lade die Nachrichtendatei basierend auf der Sprache
                                    File messagesFile = new File("plugins/GUIReward/messages/" + language + ".yml");
                                    FileConfiguration reward = YamlConfiguration.loadConfiguration(messagesFile);
                                    // Lade die Nachricht basierend auf der Sprache
                                    String getReward = reward.getString("getReward");
                                    // Sende dem Spieler die Nachricht
                                    assert getReward != null;
                                    player.sendMessage(String.format(getReward, randomQuantity, selectedMaterial));
                                    // Setze den Cooldown für den Spieler, sofern er keine Berechtigung hat
                                    if (!player.hasPermission(permission1)) {
                                        setCooldown(playerID);
                                    }
                                } else {
                                    event.setCancelled(true);
                                    player.sendMessage(getMessage("fullInv"));
                                }
                            } else {
                                addItemToStack(player, selectedMaterial, randomQuantity);

                                // Lade die Nachrichtendatei basierend auf der Sprache
                                File messagesFile = new File("plugins/GUIReward/messages/" + language + ".yml");
                                FileConfiguration reward = YamlConfiguration.loadConfiguration(messagesFile);
                                // Lade die Nachricht basierend auf der Sprache
                                String getReward = reward.getString("getReward");
                                // Sende dem Spieler die Nachricht
                                assert getReward != null;
                                player.sendMessage(String.format(getReward, randomQuantity, selectedMaterial));
                                // Setze den Cooldown für den Spieler, sofern er keine Berechtigung hat
                                if (!player.hasPermission(permission1)) {
                                    setCooldown(playerID);
                                }
                            }
                        } else {
                            if (hasFreeInventorySlot(player)) {
                                // Gebe dem Spieler das Item
                                player.getInventory().addItem(new ItemStack(selectedMaterial, randomQuantity));

                                // Lade die Nachrichtendatei basierend auf der Sprache
                                File messagesFile = new File("plugins/GUIReward/messages/" + language + ".yml");
                                FileConfiguration reward = YamlConfiguration.loadConfiguration(messagesFile);
                                // Lade die Nachricht basierend auf der Sprache
                                String getReward = reward.getString("getReward");
                                // Sende dem Spieler die Nachricht
                                assert getReward != null;
                                player.sendMessage(String.format(getReward, randomQuantity, selectedMaterial));
                                // Setze den Cooldown für den Spieler, sofern er keine Berechtigung hat
                                if (!player.hasPermission(permission1)) {
                                    setCooldown(playerID);
                                }
                            } else {
                                event.setCancelled(true);
                                player.sendMessage(getMessage("fullInv"));
                            }
                        }
                    }
                }
            }
            Player player = (Player) event.getWhoClicked();
            // Spieler anhand der UUID erhalten
            UUID playerID = Bukkit.getPlayerUniqueId(String.valueOf(player));

            if (!isPlayerInCooldown(player)) {
                // Das Item wurde erfolgreich hinzugefügt
                File fameFile = new File("plugins/GUIReward/playerdata/Fame.yml");
                FileConfiguration fameConfig = YamlConfiguration.loadConfiguration(fameFile);

                String playerUUIDString = null;
                for (String key : fameConfig.getKeys(false)) {
                    if (Objects.requireNonNull(fameConfig.getString(key + ".name")).equalsIgnoreCase(player.getName())) {
                        playerUUIDString = key;
                        break;
                    }
                }

                if (playerUUIDString == null) {
                    // Spieler nicht gefunden, neuen Eintrag erstellen
                    assert playerID != null;
                    playerUUIDString = playerID.toString();
                    fameConfig.set(playerUUIDString + ".name", player.getName());
                    fameConfig.set(playerUUIDString + ".balance", 0); // Standardeintrag mit Balance 0
                }

                File configFile = new File("plugins/GUIReward/config.yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

                int fameAmount = config.getInt("Fame");
                int currentBalance = fameConfig.getInt(playerUUIDString + ".balance");
                int newBalance = currentBalance + fameAmount;
                fameConfig.set(playerUUIDString + ".balance", newBalance);

                System.out.println(playerID);
                System.out.println(currentBalance);
                System.out.println(newBalance);

                try {
                    fameConfig.save(fameFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    player.sendMessage(getMessage("saveError"));
                    return;
                }

                String newFame = messages.getString("fame");
                assert newFame != null;
                player.sendMessage(String.format(newFame, newBalance, "Fame"));
            }
        };
    }
    private void addItemToStack(Player player, Material material, int amount) {
        PlayerInventory inventory = player.getInventory();
        ItemStack item = new ItemStack(material, amount);
        inventory.addItem(item);
        player.updateInventory();
    }
    private boolean isStackFull(Player player, Material material) {
        PlayerInventory inventory = player.getInventory();
        ItemStack item = new ItemStack(material);
        int maxStackSize = item.getMaxStackSize();
        int itemCount = 0;

        for (ItemStack stack : inventory.getContents()) {
            if (stack != null && stack.getType() == material) {
                itemCount += stack.getAmount();
            }
        }
        return itemCount >= maxStackSize;
    }
    private boolean hasItemInInventory(Player player, Material material) {
        PlayerInventory inventory = player.getInventory();
        ItemStack item = new ItemStack(material);
        return inventory.contains(item);
    }
    private long getCooldownExpiration(UUID uuid) {
        File cooldownFile = new File("plugins/GUIReward/PlayerData/cooldowns.yml");
        YamlConfiguration cooldownConfig = YamlConfiguration.loadConfiguration(cooldownFile);

        // Lese den Cooldown-Ablaufzeitstempel aus der cooldowns.yml
        return cooldownConfig.getLong(uuid.toString());
    }

    private void setCooldown(UUID uuid) {
        File cooldownFile = new File("plugins/GUIReward/PlayerData/cooldowns.yml");
        YamlConfiguration cooldownConfig = YamlConfiguration.loadConfiguration(cooldownFile);

        // Setze den Cooldown-Ablaufzeitstempel in der cooldowns.yml
        long cooldownDurationMillis = 86400000; // 24 Stunden
        long cooldownExpiration = System.currentTimeMillis() + cooldownDurationMillis;
        cooldownConfig.set(uuid.toString(), cooldownExpiration);

        try {
            cooldownConfig.save(cooldownFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ItemStack getRandomItemStack(Player player) {
        // Lade die Items-Konfigurationsdatei
        File itemsFile = new File("plugins/GUIReward/Shop/items.yml");
        FileConfiguration itemList = YamlConfiguration.loadConfiguration(itemsFile);
        List<String> itemStrings = itemList.getStringList("items");

        // Überprüfe, ob die Liste leer ist
        if (itemStrings.isEmpty()) {
            player.sendMessage(getMessage("emptyList"));
            return null; // Du kannst hier den Rückgabewert anpassen, je nachdem, was du tun möchtest
        }

        // Wähle ein zufälliges Item aus der Liste
        int randomIndex = new Random().nextInt(itemStrings.size());
        String itemString = itemStrings.get(randomIndex);

        // Erstelle den ItemStack mit der entsprechenden Menge
        return new ItemStack(Material.valueOf(itemString));
    }

}
