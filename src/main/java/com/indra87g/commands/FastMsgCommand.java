package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.indra87g.util.ConfigManager;

public class FastMsgCommand extends BaseCommand {

    private final ConfigManager configManager;

    public FastMsgCommand(String description, ConfigManager configManager) {
        super("fastmsg", description, "/fastmsg <message_key>", "waffle.fastmsg");
        this.configManager = configManager;
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        if (args.length != 1) {
            player.sendMessage(TextFormat.RED + "Usage: " + this.getUsage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        String messageKey = args[0];
        String message = configManager.getFastMessage(messageKey);

        if (message == null) {
            player.sendMessage(TextFormat.RED + "Message with key '" + messageKey + "' not found in config.yml.");
            return false;
        }

        player.getServer().broadcastMessage(message);
        return true;
    }
}