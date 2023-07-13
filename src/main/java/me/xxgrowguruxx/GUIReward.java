package me.xxgrowguruxx;

import me.xxgrowguruxx.commands.*;
import me.xxgrowguruxx.events.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class GUIReward extends JavaPlugin {

    private static GUIReward instance;

    @Override
    public void onEnable() {
        // Plugin startup logic

        instance = this;

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new FameShop(), this);
        pluginManager.registerEvents(new BuyEvent(), this);
        pluginManager.registerEvents(new LogIn(), this);
        pluginManager.registerEvents(new Join(), this);

        Objects.requireNonNull(getCommand("reward")).setExecutor(new GUI());
        Objects.requireNonNull(getCommand("addFame")).setExecutor(new Fame());
        Objects.requireNonNull(getCommand("removeFame")).setExecutor(new Fame());
        Objects.requireNonNull(getCommand("FameBalance")).setExecutor(new Fame());
        Objects.requireNonNull(getCommand("transfer")).setExecutor(new Transfer());
        Objects.requireNonNull(getCommand("adminreward")).setExecutor(new adminreward());

        Objects.requireNonNull(getCommand("transfer")).setTabCompleter(new Transfer());

        // Create the items file if it doesn't exist
        File configFile = new File(getDataFolder(), "Shop/items.yml");
        if (!configFile.exists()) {
            getLogger().info("§4Items file not found, creating default Items file.");
            saveResource("Shop/items.yml", false);
        }
        // Create the cooldown file if it doesn't exist
        configFile = new File(getDataFolder(), "PlayerData/cooldowns.yml");
        if (!configFile.exists()) {
            getLogger().info("§4Cooldown file not found, creating default Cooldown file.");
            saveResource("PlayerData/cooldowns.yml", false);
        }
        // Create the fame file if it doesn't exist
        configFile = new File(getDataFolder(), "PlayerData/Fame.yml");
        if (!configFile.exists()) {
            getLogger().info("§4Fame file not found, creating default Fame file.");
            saveResource("PlayerData/Fame.yml", false);
        }
        // Create the config file if it doesn't exist
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getLogger().info("§4Config file not found, creating default Config file.");
            saveResource("config.yml", false);
        }
        // Create the ShopSlots file if it doesn't exist
        configFile = new File(getDataFolder(), "Shop/slots.yml");
        if (!configFile.exists()) {
            getLogger().info("§4Slots file not found, creating default ShopSlots file.");
            saveResource("Shop/slots.yml", false);
        }
        // Create the fame file if it doesn't exist
        configFile = new File(getDataFolder(), "PlayerData/LogIn.yml");
        if (!configFile.exists()) {
            getLogger().info("§4LogIn file not found, creating default LogIn file.");
            saveResource("PlayerData/LogIn.yml", false);
        }
        // Create the DE file if it doesn't exist
        configFile = new File(getDataFolder(), "messages/de_DE.yml");
        if (!configFile.exists()) {
            getLogger().info("§4DE file not found, creating default DE file.");
            saveResource("messages/de_DE.yml", false);
        }
        // Create the EN file if it doesn't exist
        configFile = new File(getDataFolder(), "messages/en_EN.yml");
        if (!configFile.exists()) {
            getLogger().info("§4EN file not found, creating default EN file.");
            saveResource("messages/en_EN.yml", false);
        }
    }

    public static GUIReward getInstance() {
        return instance;
    }

    @Override
    public void onDisable(){
        // Plugin shutdown logic
    }
}
