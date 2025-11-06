package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.indra87g.Main;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

public class BankCommand extends BaseCommand {

    private final Main plugin;
    private final Config bankData;

    public BankCommand(String description, Main plugin) {
        super("bank", description);
        this.plugin = plugin;
        this.bankData = new Config(new File(plugin.getDataFolder(), "bank.yml"), Config.YAML);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(TextFormat.AQUA + "Waffle Bank Command Usage:");
            player.sendMessage(TextFormat.YELLOW + "/bank deposit <money|exp> <amount>");
            player.sendMessage(TextFormat.YELLOW + "/bank withdraw <money|exp> <amount>");
            player.sendMessage(TextFormat.YELLOW + "/bank account view");
            player.sendMessage(TextFormat.YELLOW + "/bank account register");
            player.sendMessage(TextFormat.YELLOW + "/bank account purge");
            player.sendMessage(TextFormat.YELLOW + "/bank account upgradelimit <nether_stars>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "deposit":
                handleDeposit(player, args);
                break;
            case "withdraw":
                handleWithdraw(player, args);
                break;
            case "account":
                handleAccount(player, args);
                break;
            default:
                 player.sendMessage(TextFormat.RED + "Unknown subcommand. Use /bank for help.");
                break;
        }
        return true;
    }

    private void handleAccount(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(TextFormat.RED + "Usage: /bank account <register|view|purge|upgradelimit>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "register":
                handleAccountRegister(player);
                break;
            case "view":
                sendBankStatus(player);
                break;
            case "purge":
                handleAccountPurge(player);
                break;
            case "upgradelimit":
                handleLimitUpgrade(player, args);
                break;
            default:
                player.sendMessage(TextFormat.RED + "Usage: /bank account <register|view|purge|upgradelimit>");
                break;
        }
    }

    private void handleAccountRegister(Player player) {
        String playerName = player.getName();
        if (bankData.exists(playerName)) {
            player.sendMessage(TextFormat.RED + "You already have a bank account.");
            return;
        }

        if (player.getExperience() < 500) {
            player.sendMessage(TextFormat.RED + "You need 500 EXP to register a bank account.");
            return;
        }

        player.addExperience(-500);
        bankData.set(playerName + ".money", 0);
        bankData.set(playerName + ".exp", 0);
        bankData.set(playerName + ".max_money", 1000000);
        bankData.set(playerName + ".max_exp", 100000);
        bankData.save();

        player.sendMessage(TextFormat.GREEN + "Bank account registered successfully for 500 EXP!");
    }

    private void handleAccountPurge(Player player) {
        String playerName = player.getName();
        if (!bankData.exists(playerName)) {
            player.sendMessage(TextFormat.RED + "You don't have a bank account.");
            return;
        }

        bankData.remove(playerName);
        bankData.save();
        player.sendMessage(TextFormat.GREEN + "Your bank account has been successfully purged.");
    }

    private void sendBankStatus(Player player) {
        String playerName = player.getName();
        if (!bankData.exists(playerName)) {
            player.sendMessage(TextFormat.RED + "You don't have a bank account. Use '/bank account register' to create one.");
            return;
        }

        double money = bankData.getDouble(playerName + ".money", 0);
        int exp = bankData.getInt(playerName + ".exp", 0);
        double maxMoney = bankData.getDouble(playerName + ".max_money", 1000000);
        int maxExp = bankData.getInt(playerName + ".max_exp", 100000);
        String bankName = plugin.getConfig().getString("bank.bank_name", "Waffle Bank");

        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));

        player.sendMessage(TextFormat.GOLD + "=============");
        player.sendMessage(TextFormat.AQUA + bankName);
        player.sendMessage(TextFormat.WHITE + "Name: " + playerName);
        player.sendMessage(TextFormat.YELLOW + "Money: " + TextFormat.WHITE + "Rp" + nf.format(money) + " / Rp" + nf.format(maxMoney));
        player.sendMessage(TextFormat.YELLOW + "EXP: " + TextFormat.WHITE + nf.format(exp) + " / " + nf.format(maxExp));
        player.sendMessage(TextFormat.GOLD + "=============");
    }

    private void handleDeposit(Player player, String[] args) {
        String playerName = player.getName();
        if (!bankData.exists(playerName)) {
            player.sendMessage(TextFormat.RED + "You don't have a bank account. Use '/bank account register' to create one.");
            return;
        }

        if (args.length < 3) {
            player.sendMessage(TextFormat.RED + "Usage: /bank deposit <money|exp> <amount>");
            return;
        }

        String type = args[1].toLowerCase();
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(TextFormat.RED + "Invalid amount.");
            return;
        }

        if (amount <= 0) {
            player.sendMessage(TextFormat.RED + "Amount must be positive.");
            return;
        }

        if ("money".equals(type)) {
            int minDeposit = plugin.getConfig().getInt("bank.min_deposit_money", 1000);
            if (amount < minDeposit) {
                player.sendMessage(TextFormat.RED + "Minimum money deposit is " + minDeposit);
                return;
            }

            double currentBalance = EconomyAPI.getInstance().myMoney(player);
            if (currentBalance < amount) {
                player.sendMessage(TextFormat.RED + "You don't have enough money.");
                return;
            }

            double currentBankMoney = bankData.getDouble(playerName + ".money", 0);
            double maxMoney = bankData.getDouble(playerName + ".max_money", 1000000);
            if (currentBankMoney + amount > maxMoney) {
                player.sendMessage(TextFormat.RED + "You have reached your maximum money limit in the bank.");
                return;
            }

            EconomyAPI.getInstance().reduceMoney(player, amount);
            bankData.set(playerName + ".money", currentBankMoney + amount);
            bankData.save();
            player.sendMessage(TextFormat.GREEN + "Successfully deposited " + amount + " money.");
        } else if ("exp".equals(type)) {
            int minDeposit = plugin.getConfig().getInt("bank.min_deposit_exp", 500);
            if (amount < minDeposit) {
                player.sendMessage(TextFormat.RED + "Minimum EXP deposit is " + minDeposit);
                return;
            }

            if (player.getExperience() < amount) {
                player.sendMessage(TextFormat.RED + "You don't have enough EXP.");
                return;
            }

            int currentBankExp = bankData.getInt(playerName + ".exp", 0);
            int maxExp = bankData.getInt(playerName + ".max_exp", 100000);
            if (currentBankExp + amount > maxExp) {
                player.sendMessage(TextFormat.RED + "You have reached your maximum EXP limit in the bank.");
                return;
            }

            player.addExperience(-amount);
            bankData.set(playerName + ".exp", currentBankExp + amount);
            bankData.save();
            player.sendMessage(TextFormat.GREEN + "Successfully deposited " + amount + " EXP.");
        } else {
            player.sendMessage(TextFormat.RED + "Usage: /bank deposit <money|exp> <amount>");
        }
    }

    private void handleWithdraw(Player player, String[] args) {
         String playerName = player.getName();
        if (!bankData.exists(playerName)) {
            player.sendMessage(TextFormat.RED + "You don't have a bank account. Use '/bank account register' to create one.");
            return;
        }

        if (args.length < 3) {
            player.sendMessage(TextFormat.RED + "Usage: /bank withdraw <money|exp> <amount>");
            return;
        }

        String type = args[1].toLowerCase();
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(TextFormat.RED + "Invalid amount.");
            return;
        }

        if (amount <= 0) {
            player.sendMessage(TextFormat.RED + "Amount must be positive.");
            return;
        }

        if ("money".equals(type)) {
            int minWithdraw = plugin.getConfig().getInt("bank.min_withdraw_money", 1000);
            if (amount < minWithdraw) {
                player.sendMessage(TextFormat.RED + "Minimum money withdraw is " + minWithdraw);
                return;
            }

            double currentBankMoney = bankData.getDouble(playerName + ".money", 0);
            if (currentBankMoney < amount) {
                player.sendMessage(TextFormat.RED + "You don't have enough money in the bank.");
                return;
            }

            EconomyAPI.getInstance().addMoney(player, amount);
            bankData.set(playerName + ".money", currentBankMoney - amount);
            bankData.save();
            player.sendMessage(TextFormat.GREEN + "Successfully withdrew " + amount + " money.");
        } else if ("exp".equals(type)) {
            int minWithdraw = plugin.getConfig().getInt("bank.min_withdraw_exp", 500);
            if (amount < minWithdraw) {
                player.sendMessage(TextFormat.RED + "Minimum EXP withdraw is " + minWithdraw);
                return;
            }

            int currentBankExp = bankData.getInt(playerName + ".exp", 0);
            if (currentBankExp < amount) {
                player.sendMessage(TextFormat.RED + "You don't have enough EXP in the bank.");
                return;
            }

            player.addExperience(amount);
            bankData.set(playerName + ".exp", currentBankExp - amount);
            bankData.save();
            player.sendMessage(TextFormat.GREEN + "Successfully withdrew " + amount + " EXP.");
        } else {
            player.sendMessage(TextFormat.RED + "Usage: /bank withdraw <money|exp> <amount>");
        }
    }

    private void handleLimitUpgrade(Player player, String[] args) {
         String playerName = player.getName();
        if (!bankData.exists(playerName)) {
            player.sendMessage(TextFormat.RED + "You don't have a bank account. Use '/bank account register' to create one.");
            return;
        }

        if (args.length < 3) {
            player.sendMessage(TextFormat.RED + "Usage: /bank account upgradelimit <amount>");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(TextFormat.RED + "Invalid amount.");
            return;
        }

        if (amount <= 0) {
            player.sendMessage(TextFormat.RED + "Amount must be positive.");
            return;
        }

        Item netherStar = Item.get(Item.NETHER_STAR, 0, amount);
        if (!player.getInventory().contains(netherStar)) {
            player.sendMessage(TextFormat.RED + "You don't have enough Nether Stars.");
            return;
        }

        player.getInventory().removeItem(netherStar);

        double maxMoney = bankData.getDouble(playerName + ".max_money", 1000000);
        int maxExp = bankData.getInt(playerName + ".max_exp", 100000);

        double newMaxMoney = maxMoney * (1 + (0.25 * amount));
        int newMaxExp = (int) (maxExp * (1 + (0.25 * amount)));

        bankData.set(playerName + ".max_money", newMaxMoney);
        bankData.set(playerName + ".max_exp", newMaxExp);
        bankData.save();

        player.sendMessage(TextFormat.GREEN + "Successfully upgraded your bank limit by " + (25 * amount) + "%");
        sendBankStatus(player);
    }
}
