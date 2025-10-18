package com.indra87g.commands;

import cn.nukkit.Player;
import com.indra87g.Main;
import com.indra87g.util.ConfigManager;
import coins.Coins;
import me.onebone.economyapi.EconomyAPI;

public class ConvertCoinCommand extends BaseCommand {

    private final Main plugin;
    private final ConfigManager configManager;

    public ConvertCoinCommand(String description, Main plugin) {
        super("convertcoin", description, "/convertcoin <amount>");
        this.plugin = plugin;
        this.configManager = new ConfigManager(plugin);
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        if (args.length != 1) {
            player.sendMessage(this.getUsage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        if (!plugin.isCoinsAPIAvailable() || !plugin.isEconomyAPIAvailable()) {
            player.sendMessage("Coin or Economy integration is not available.");
            return true;
        }

        if (!configManager.isCoinConversionEnabled()) {
            player.sendMessage("Coin conversion is disabled.");
            return true;
        }

        int coinsToConvert;

        try {
            coinsToConvert = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("Please enter a valid number.");
            return true;
        }

        if (coinsToConvert <= 0) {
            player.sendMessage("Please enter a positive number.");
            return true;
        }

        int playerCoins = Coins.getInstance().getPlayerCoins(player);
        if (playerCoins < coinsToConvert) {
            player.sendMessage("You don't have enough coins.");
            return true;
        }

        int conversionRateCoins = configManager.getConversionRateCoins();
        int conversionRateMoney = configManager.getConversionRateMoney();

        if (coinsToConvert % conversionRateCoins != 0) {
            player.sendMessage("You can only convert coins in multiples of " + conversionRateCoins);
            return true;
        }

        int moneyToReceive = (coinsToConvert / conversionRateCoins) * conversionRateMoney;

        Coins.getInstance().getCoinsManager().reduceCoins(player, coinsToConvert);
        EconomyAPI.getInstance().addMoney(player, moneyToReceive);

        player.sendMessage("You have successfully converted " + coinsToConvert + " coins to " + moneyToReceive + " money.");

        return true;
    }
}