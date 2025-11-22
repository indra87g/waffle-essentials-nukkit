package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.indra87g.Main;
import com.indra87g.util.ConfigManager;

public class FastMsgCommand extends BaseCommand {

    private final ConfigManager configManager;
    private final Main plugin;

    public FastMsgCommand(String description, ConfigManager configManager, Main plugin) {
        super("fastmsg", description, "/fastmsg <message_key>", "waffle.fastmsg");
        this.configManager = configManager;
        this.plugin = plugin;
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

        this.plugin.getServer().broadcastMessage(message);
        player.sendMessage(TextFormat.GREEN + "Fast message '" + messageKey + "' sent successfully.");
        return true;
    }
}
