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
        super("wbuy", description, "/wbuy command <identifier>");
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
        if (args.length < 2 || !"command".equalsIgnoreCase(args[0])) {
            player.sendMessage(this.getUsage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        String identifier = args[1];
        String path = "wshop.command." + identifier;

        if (!wshopConfig.exists(path)) {
            player.sendMessage("§cThe specified command identifier does not exist in wshop.yml.");
            return false;
        }

        Map<String, Object> commandData = wshopConfig.getSection(path).getAll();
        String commandToExecute = (String) commandData.get("execute");
        int price = (int) commandData.get("price");

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
        this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), finalCommand);

        player.sendMessage("§aYou have successfully purchased and executed the command for " + price + " money.");
        return true;
    }
}
