package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;

public class InfoCommand extends BaseCommand {

    public InfoCommand(String description) {
        super("info", description, "/info <me|server>", "waffle.info");
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        if (args.length == 0) {
            sendUsage(player);
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "me":
                if (!player.hasPermission("waffle.info.me")) {
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
            default:
                sendUsage(player);
                return false;
        }
        return true;
    }

    private void sendPlayerInfo(Player player) {
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

        String myInfo = "====================\n" +
                TextFormat.AQUA + playerName + TextFormat.WHITE + " Level " + TextFormat.YELLOW + playerLevel + "\n" +
                TextFormat.WHITE + "HP: " + TextFormat.RED + playerHealth + "\n" +
                TextFormat.WHITE + "Hunger: " + TextFormat.GOLD + playerFood + "\n" +
                TextFormat.WHITE + "XP: " + TextFormat.GREEN + playerExperience + "/" + xpToLevelUp + "\n" +
                TextFormat.WHITE + "Ping: " + TextFormat.AQUA + ping + "ms\n" +
                TextFormat.WHITE + "Respawn Point: " + TextFormat.LIGHT_PURPLE + respawnPoint + "\n" +
                TextFormat.WHITE + "====================";

        player.sendMessage(myInfo);
    }

    private void sendServerInfo(Player player) {
        Server server = player.getServer();
        Runtime runtime = Runtime.getRuntime();

        double maxMemory = runtime.maxMemory() / 1024.0 / 1024.0 / 1024.0;
        double totalMemory = runtime.totalMemory() / 1024.0 / 1024.0 / 1024.0;
        double freeMemory = runtime.freeMemory() / 1024.0 / 1024.0 / 1024.0;
        double usedMemory = totalMemory - freeMemory;

        String serverInfo = "============\n" +
                String.format("RAM Usage: %.2fGB/%.2fGB\n", usedMemory, maxMemory) +
                "CPU Usage: " + String.format("%.2f", server.getTickUsage()) + "%/100%\n" +
                "Player Online: " + server.getOnlinePlayers().size() + "/" + server.getMaxPlayers() + "\n" +
                "TPS: " + server.getTicksPerSecond() + "\n" +
                "\n" +
                "IP: " + server.getIp() + "\n" +
                "Port: " + server.getPort() + "\n" +
                "MOTD: " + server.getMotd() + "\n" +
                "=============";

        player.sendMessage(serverInfo);
    }

    private void sendUsage(Player player) {
        player.sendMessage(TextFormat.RED + "Usage: " + getUsage());
    }
}
