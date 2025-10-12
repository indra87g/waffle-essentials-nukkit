package com.indra87g;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;
import com.indra87g.commands.*;
import com.indra87g.util.ConfigManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Main extends PluginBase {

    private ConfigManager configManager;
    private List<Map> servers;
    private final Map<UUID, PluginTask<?>> countdowns = new HashMap<>();
    private final Map<UUID, Player> teleportingPlayers = new HashMap<>();
    private final Map<UUID, cn.nukkit.level.Location> playerLocations = new HashMap<>();
    private boolean economyAPIAvailable = false;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.saveResource("servers.yml");

        configManager = new ConfigManager(this);

        Config serversConfig = new Config(new File(this.getDataFolder(), "servers.yml"), Config.YAML);
        this.servers = serversConfig.getMapList("servers");

        getLogger().info("WaffleCoreNK has been enabled.");

        checkEconomyAPI();
        registerCommands();

        this.getServer().getPluginManager().registerEvents(new com.indra87g.listeners.PlayerMoveListener(this), this);
    }

    private void checkEconomyAPI() {
        if (getServer().getPluginManager().getPlugin("EconomyAPI") != null) {
            economyAPIAvailable = true;
            getLogger().info("[WaffleCoreNK] EconomyAPI is already installed on the server, allowing it to be integrated with this plugin.");
        } else {
            getLogger().info("[WaffleCoreNK] EconomyAPI is not installed. Some functionality of this plugin may be impaired.");
        }
    }

    private void registerCommands() {
        // Commands with special registration logic
        if (configManager.isCommandEnabled("servers")) {
            String description = configManager.getCommandDescription("servers", "Shows a list of available servers.");
            ServersCommand serversCommand = new ServersCommand(this, description);
            this.getServer().getPluginManager().registerEvents(serversCommand, this);
            this.getServer().getCommandMap().register("servers", serversCommand);
        }

        if (configManager.isCommandEnabled("fastmsg")) {
            String description = configManager.getCommandDescription("fastmsg", "Sends a predefined message. Usage: /fastmsg <message_key>");
            this.getServer().getCommandMap().register("fastmsg", new FastMsgCommand(description, configManager, this));
        }

        // Simple commands that only need a description
        registerSimpleCommand("setblock", "Sets a block at the player's location. Usage: /setblock <block_id>", (desc, main) -> new SetBlockCommand(desc));
        registerSimpleCommand("clearchat", "Clear your chat", (desc, main) -> new ClearChatCommand(desc));
        registerSimpleCommand("info", "Shows your player information", InfoCommand::new);
    }

    private void registerSimpleCommand(String name, String defaultDescription, BiFunction<String, Main, Command> constructor) {
        if (configManager.isCommandEnabled(name)) {
            String description = configManager.getCommandDescription(name, defaultDescription);
            this.getServer().getCommandMap().register(name, constructor.apply(description, this));
        }
    }

    public List<Map> getServers() {
        return servers;
    }

    public Map<UUID, PluginTask<?>> getCountdowns() {
        return countdowns;
    }

    public Map<UUID, Player> getTeleportingPlayers() {
        return teleportingPlayers;
    }

    public Map<UUID, cn.nukkit.level.Location> getPlayerLocations() {
        return playerLocations;
    }

    public boolean isEconomyAPIAvailable() {
        return economyAPIAvailable;
    }
}