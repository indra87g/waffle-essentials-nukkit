package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import com.indra87g.Main;
import me.onebone.economyapi.EconomyAPI;

public class InfoCommand extends BaseCommand {

    private final Main plugin;

    public InfoCommand(String description, Main plugin) {
        super("info", description, "/info <player|server|playerpp>");
        this.plugin = plugin;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        if (args.length == 0) {
            sendUsage(player);
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "player":
                if (!player.hasPermission("waffle.info.player")) {
                    player.sendMessage(TextFormat.RED + "You don't have permission to use this command.");
                    return false;
                }
                sendPlayerInfo(player);
                break;
            case "server":
                if (!player.hasPermission("waffle.info.server")) {
                    player.sendMessage(TextFormat.RED + "You don't have permission to use this command.");
                    return false;
                }
                sendServerInfo(player);
                break;
            case "playerpp":
                if (!plugin.isEconomyAPIAvailable()) {
                    player.sendMessage(TextFormat.RED + "EconomyAPI is not installed on the server.");
                    return false;
                }
                if (!player.hasPermission("waffle.info.playerpp")) {
                    player.sendMessage(TextFormat.RED + "You don't have permission to use this command.");
                    return false;
                }
                sendPlayerPpInfo(player);
                break;
            default:
                sendUsage(player);
                return false;
        }
        return true;
    }

    private void sendPlayerInfo(Player player) {
        sendPlayerInfo(player, -1);
    }

    private void sendPlayerInfo(Player player, double money) {
        String playerName = player.getName();
        int playerLevel = player.getExperienceLevel();
        double playerHealth = player.getHealth();
        int playerFood = player.getFoodData().getLevel();
        int playerExperience = player.getExperience();
        int xpToLevelUp = player.getExperienceLevel() > 0 ? player.calculateRequireExperience(player.getExperienceLevel()) : player.calculateRequireExperience(0);
        int ping = player.getPing();

        Position respawn = player.getSpawn();
        String respawnPoint = String.format("X: %.2f, Y: %.2f, Z: %.2f, World: %s",
                respawn.getX(), respawn.getY(), respawn.getZ(), respawn.getLevel().getName());

        StringBuilder myInfo = new StringBuilder();
        myInfo.append("====================\n");
        myInfo.append(TextFormat.AQUA).append(playerName).append(TextFormat.WHITE).append(" Level ").append(TextFormat.YELLOW).append(playerLevel).append("\n");
        myInfo.append(TextFormat.WHITE).append("HP: ").append(TextFormat.RED).append(playerHealth).append("\n");
        myInfo.append(TextFormat.WHITE).append("Hunger: ").append(TextFormat.GOLD).append(playerFood).append("\n");
        myInfo.append(TextFormat.WHITE).append("XP: ").append(TextFormat.GREEN).append(playerExperience).append("/").append(xpToLevelUp).append("\n");
        if (money != -1) {
            myInfo.append(TextFormat.WHITE).append("Money: ").append(TextFormat.GOLD).append(String.format("%.2f", money)).append("\n");
        }
        myInfo.append(TextFormat.WHITE).append("Ping: ").append(TextFormat.AQUA).append(ping).append("ms\n");
        myInfo.append(TextFormat.WHITE).append("Respawn Point: ").append(TextFormat.LIGHT_PURPLE).append(respawnPoint).append("\n");
        myInfo.append(TextFormat.WHITE).append("====================");

        player.sendMessage(myInfo.toString());
    }

    private void sendServerInfo(Player player) {
        Server server = player.getServer();
        Runtime runtime = Runtime.getRuntime();

        double maxMemory = runtime.maxMemory() / 1024.0 / 1024.0 / 1024.0;
        double totalMemory = runtime.totalMemory() / 1024.0 / 1024.0 / 1024.0;
        double freeMemory = runtime.freeMemory() / 1024.0 / 1024.0 / 1024.0;
        double usedMemory = totalMemory - freeMemory;

        String serverInfo = "====================\n" +
                TextFormat.WHITE + "RAM Usage: " + TextFormat.GREEN + String.format("%.2fGB/%.2fGB\n", usedMemory, maxMemory) +
                TextFormat.WHITE + "CPU Usage: " + TextFormat.YELLOW + String.format("%.2f", server.getTickUsage()) + "%/100%\n" +
                TextFormat.WHITE + "Player Online: " + TextFormat.AQUA + server.getOnlinePlayers().size() + "/" + server.getMaxPlayers() + "\n" +
                TextFormat.WHITE + "TPS: " + TextFormat.GOLD + server.getTicksPerSecond() + "\n" +
                "\n" +
                TextFormat.WHITE + "IP: " + TextFormat.LIGHT_PURPLE + server.getIp() + "\n" +
                TextFormat.WHITE + "Port: " + TextFormat.LIGHT_PURPLE + server.getPort() + "\n" +
                TextFormat.WHITE + "MOTD: " + TextFormat.GRAY + server.getMotd() + "\n" +
                "====================";

        player.sendMessage(serverInfo);
    }

    private void sendPlayerPpInfo(Player player) {
        double money = EconomyAPI.getInstance().myMoney(player);
        sendPlayerInfo(player, money);
    }

    private void sendUsage(Player player) {
        player.sendMessage(TextFormat.RED + "Usage: " + getUsage());
    }
}