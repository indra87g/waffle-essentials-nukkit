package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;

public class MeCommand extends BaseCommand {

    public MeCommand(String description) {
        super("me", description, "/me", "waffle.me");
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        String playerName = player.getName();
        int playerLevel = player.getExperienceLevel();
        double playerHealth = player.getHealth();
        int playerFood = player.getFoodData().getLevel();
        int playerExperience = player.getExperience();
        int xpToLevelUp = player.getExperienceLevel() > 0 ? player.calculateRequireExperience(player.getExperienceLevel()) : player.calculateRequireExperience(0);

        Position respawn = player.getSpawn();
        String respawnPoint = String.format("X: %.2f, Y: %.2f, Z: %.2f, World: %s",
                respawn.getX(), respawn.getY(), respawn.getZ(), respawn.getLevel().getName());

        String myInfo = "====================\n" +
                TextFormat.AQUA + playerName + TextFormat.WHITE + " Level " + TextFormat.YELLOW + playerLevel + "\n" +
                TextFormat.WHITE + "HP: " + TextFormat.RED + playerHealth + "\n" +
                TextFormat.WHITE + "Hunger: " + TextFormat.GOLD + playerFood + "\n" +
                TextFormat.WHITE + "XP: " + TextFormat.GREEN + playerExperience + "/" + xpToLevelUp + "\n" +
                TextFormat.WHITE + "Respawn Point: " + TextFormat.LIGHT_PURPLE + respawnPoint + "\n" +
                TextFormat.WHITE + "====================";

        player.sendMessage(myInfo);
        return true;
    }
}
