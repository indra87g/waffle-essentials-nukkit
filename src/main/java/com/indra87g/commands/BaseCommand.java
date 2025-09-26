package com.indra87g.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.Player;

public abstract class BaseCommand extends Command {

    public BaseCommand(String name, String description, String usage, String permission) {
        super(name);
        this.setDescription(description);
        this.setUsage(usage);
        this.setPermission(permission);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            sender.sendMessage("You do not have permission to use this command.");
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return false;
        }

        Player player = (Player) sender;

        if (!validateArgs(args, player)) {
            // The validateArgs method is responsible for sending the usage message.
            return false;
        }

        try {
            return handleCommand(player, args);
        } catch (Exception e) {
            sender.sendMessage("An unexpected error occurred while executing the command.");
            e.printStackTrace();
            return false;
        }
    }

    protected boolean validateArgs(String[] args, Player player) {
        return true;
    }

    protected abstract boolean handleCommand(Player player, String[] args);
}
