package com.indra87g;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;
import com.indra87g.commands.ServersCommand;
import com.indra87g.commands.SetBlockCommand;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Main extends PluginBase {

    private List<Map<?, ?>> servers;
    private final Map<UUID, PluginTask<?>> countdowns = new HashMap<>();
    private final Map<UUID, Player> teleportingPlayers = new HashMap<>();
    private final Map<UUID, cn.nukkit.level.Location> playerLocations = new HashMap<>();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.saveResource("servers.yml");

        Config serversConfig = new Config(new File(this.getDataFolder(), "servers.yml"), Config.YAML);
        this.servers = serversConfig.getMapList("servers");

        getLogger().info("WaffleCoreNK has been enabled.");

        ServersCommand serversCommand = new ServersCommand(this);
        this.getServer().getPluginManager().registerEvents(new com.indra87g.listeners.PlayerMoveListener(this), this);
        this.getServer().getPluginManager().registerEvents(serversCommand, this);
        this.getServer().getCommandMap().register("setblock", new SetBlockCommand());
        this.getServer().getCommandMap().register("servers", serversCommand);
    }

    public List<Map<?, ?>> getServers() {
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
