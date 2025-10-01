package com.indra87g;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;
import com.indra87g.commands.ClearChatCommand;
import com.indra87g.commands.ServersCommand;
import com.indra87g.commands.SetBlockCommand;
import com.indra87g.util.ConfigManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Main extends PluginBase {

    private ConfigManager configManager;
    private List<Map> servers;
    private final Map<UUID, PluginTask<?>> countdowns = new HashMap<>();
    private final Map<UUID, Player> teleportingPlayers = new HashMap<>();
    private final Map<UUID, cn.nukkit.level.Location> playerLocations = new HashMap<>();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.saveResource("servers.yml");

        configManager = new ConfigManager(this);

        Config serversConfig = new Config(new File(this.getDataFolder(), "servers.yml"), Config.YAML);
        this.servers = serversConfig.getMapList("servers");

        getLogger().info("WaffleCoreNK has been enabled.");

        // Register commands based on config
        if (configManager.isCommandEnabled("servers")) {
            String description = configManager.getCommandDescription("servers", "Shows a list of available servers.");
            ServersCommand serversCommand = new ServersCommand(this, description);
            this.getServer().getPluginManager().registerEvents(serversCommand, this);
            this.getServer().getCommandMap().register("servers", serversCommand);
        }

        if (configManager.isCommandEnabled("setblock")) {
            String description = configManager.getCommandDescription("setblock", "Sets a block at the player's location. Usage: /setblock <block_id>");
            this.getServer().getCommandMap().register("setblock", new SetBlockCommand(description));
        }

        if (configManager.isCommandEnabled("clearchat")) {
            String description = configManager.getCommandDescription("clearchat", "Clear your chat");
            this.getServer().getCommandMap().register("clearchat", new ClearChatCommand(description));
        }

        this.getServer().getPluginManager().registerEvents(new com.indra87g.listeners.PlayerMoveListener(this), this);
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
}
