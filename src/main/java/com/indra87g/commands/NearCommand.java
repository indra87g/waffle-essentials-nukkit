package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import com.indra87g.Main;
import com.indra87g.util.ConfigManager;

import java.util.*;

public class NearCommand extends BaseCommand {
    private final Main plugin;
    private final ConfigManager configManager;
    private String TEXT_NEAR_NO, TEXT_NEAR_FOUND, TEXT_NEAR_PLAYER;

    public NearCommand(String description, Main plugin) {
        super("near", description, "/near");
        this.plugin = plugin;
        this.configManager = new ConfigManager(plugin);
        Config cfg = plugin.getConfig();
        TEXT_NEAR_NO = cfg.getString("near-no", "§cNo players found within %d blocks.");
        TEXT_NEAR_FOUND = cfg.getString("near-found", "§aPlayers within %d blocks:");
        TEXT_NEAR_PLAYER = cfg.getString("near-player", "§f%s (%d blocks)");
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        int radius = configManager.getNearDetectionRadius();
        near(radius, player);
        return true;
    }

    private void near(int radius, Player sender) {
        Map<String, Integer> nearPlayers = new HashMap<>();

        Collection<Player> allPlayers = Server.getInstance().getOnlinePlayers().values();
        for (Player player : allPlayers) {
            if (player.getLevel().getId() != sender.getLevel().getId() || player.getId() == sender.getId() || player.hasPermission("waffle.near.ignore")) {
                continue;
            }
            int distance = (int) sender.distance(player);
            if (distance > radius) {
                continue;
            }
            nearPlayers.put(player.getName(), distance);
        }

        sender.sendMessage(buildMessage(nearPlayers, radius));
    }

    private String buildMessage(Map<String, Integer> players, int radius) {
        if (players.isEmpty()) {
            return String.format(TEXT_NEAR_NO, radius);
        }

        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(players.entrySet());
        entryList.sort(Map.Entry.comparingByValue());

        String start = String.format(TEXT_NEAR_FOUND, radius);
        StringBuilder message = new StringBuilder(start);
        for (Map.Entry<String, Integer> entry : entryList) {
            if (message.length() > start.length()) {
                message.append(", ");
            }
            message.append(String.format(TEXT_NEAR_PLAYER, entry.getKey(), entry.getValue()));
        }

        return message.toString();
    }
}
