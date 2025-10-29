package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import com.indra87g.Main;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.util.Map;

public class WbuyCommand extends BaseCommand {

    private final Main plugin;
    private final Config wshopConfig;

    public WbuyCommand(String description, Main plugin) {
        super("wbuy", description, "/wbuy <command|restock> <identifier> [amount]");
        this.plugin = plugin;
        this.wshopConfig = new Config(new File(plugin.getDataFolder(), "wshop.yml"), Config.YAML);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.plugin.isEconomyAPIAvailable()) {
            sender.sendMessage("§cEconomyAPI is not available. This command is disabled.");
            return false;
        }
        return super.execute(sender, commandLabel, args);
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        if (args.length < 1) {
            player.sendMessage(this.getUsage());
            return false;
        }
        String subCommand = args[0].toLowerCase();
        if (!"command".equals(subCommand) && !"restock".equals(subCommand)) {
            player.sendMessage(this.getUsage());
            return false;
        }

        if ("command".equals(subCommand) && args.length < 2) {
            player.sendMessage(this.getUsage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        String subCommand = args[0].toLowerCase();

        if ("restock".equals(subCommand)) {
            if (!player.hasPermission("wbuy.restock")) {
                player.sendMessage("§cYou do not have permission to use this command.");
                return false;
            }
            if (args.length < 3) {
                player.sendMessage("§cUsage: /wbuy restock <identifier> <amount>");
                return false;
            }
            String identifier = args[1];
            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid amount specified.");
                return false;
            }
            String path = "wshop.command." + identifier;
            if (!wshopConfig.exists(path)) {
                player.sendMessage("§cThe specified command identifier does not exist in wshop.yml.");
                return false;
            }
            int currentStock = wshopConfig.getInt(path + ".stock");
            wshopConfig.set(path + ".stock", currentStock + amount);
            wshopConfig.save();
            player.sendMessage("§aSuccessfully restocked " + identifier + " by " + amount + ".");
            return true;
        }

        if ("command".equals(subCommand)) {
            String identifier = args[1];
            String path = "wshop.command." + identifier;

            if (!wshopConfig.exists(path)) {
                player.sendMessage("§cThe specified command identifier does not exist in wshop.yml.");
                return false;
            }

            Map<String, Object> commandData = wshopConfig.getSection(path).getAll();
            String commandToExecute = (String) commandData.get("execute");
            int price = (int) commandData.get("price");
            int stock = wshopConfig.getInt(path + ".stock");

            if (stock <= 0) {
                player.sendMessage("§cThis item is out of stock.");
                return false;
            }

            if (commandToExecute == null || price <= 0) {
                player.sendMessage("§cInvalid command configuration in wshop.yml.");
                return false;
            }

            double playerMoney = EconomyAPI.getInstance().myMoney(player);
            if (playerMoney < price) {
                player.sendMessage("§cYou don't have enough money to purchase this command. Required: " + price + ", You have: " + playerMoney);
                return false;
            }

            EconomyAPI.getInstance().reduceMoney(player, price);

            String finalCommand = commandToExecute.replace("%p", player.getName());
            boolean commandSuccess = this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), finalCommand);

            if (commandSuccess) {
                wshopConfig.set(path + ".stock", stock - 1);
                wshopConfig.save();
                player.sendMessage("§aYou have successfully purchased and executed the command for " + price + " money.");
            } else {
                EconomyAPI.getInstance().addMoney(player, price);
                player.sendMessage("§cThe command failed to execute. Your money has been refunded.");
            }
            return true;
        }
        return false;
    }
}
