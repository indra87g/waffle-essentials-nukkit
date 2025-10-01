package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;

public class ClearChatCommand extends BaseCommand {

    public ClearChatCommand(String description) {
        super("clearchat", description, "/clearchat [amount]", "waffle.clear.chat");
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        if (args.length > 1) {
            player.sendMessage(TextFormat.RED + "Too many arguments.");
            player.sendMessage(TextFormat.GRAY + "Usage: " + this.getUsage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        int amount = 100; // default

        if (args.length == 1) {
            try {
                amount = Integer.parseInt(args[0]);
                if (amount <= 0 || amount > 500) { // Limit to a reasonable number
                    player.sendMessage(TextFormat.RED + "Amount must be between 1 and 500.");
                    return false;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(TextFormat.RED + "The amount must be a valid number.");
                return false;
            }
        }

        // Using StringBuilder is efficient for concatenating strings in a loop.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            sb.append("\n");
        }
        player.sendMessage(sb.toString());

        player.sendMessage(TextFormat.GREEN + "Successfully cleared " + amount + " lines of chat.");
        return true;
    }
}