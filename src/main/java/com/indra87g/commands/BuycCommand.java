package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.Config;
import com.indra87g.Main;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.util.*;

public class BuycCommand extends BaseCommand implements Listener {

    private final Main plugin;
    private final Config buycConfig;
    private static final Map<UUID, List<String>> pendingForms = new HashMap<>();
    private static final String FORM_TITLE = "Command Shop";

    public BuycCommand(String description, Main plugin) {
        super("buyc", description, "/buyc");
        this.plugin = plugin;
        this.buycConfig = new Config(new File(plugin.getDataFolder(), "buyc.yml"), Config.YAML);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.plugin.isEconomyAPIAvailable()) {
            sender.sendMessage("§cEconomyAPI is not available. This command is disabled.");
            return false;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return false;
        }
        return super.execute(sender, commandLabel, args);
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        return true; // No arguments are needed for this command
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        FormWindowSimple form = new FormWindowSimple(FORM_TITLE, "Select a command to purchase");

        Map<String, Object> commands = buycConfig.getSection("commands").getAll();
        List<String> commandKeys = new ArrayList<>(commands.keySet());

        if (commandKeys.isEmpty()) {
            player.sendMessage("§cThere are no commands available for purchase.");
            return false;
        }

        for (String key : commandKeys) {
            Map<String, Object> commandData = (Map<String, Object>) commands.get(key);
            String name = (String) commandData.get("name");
            int price = (int) commandData.get("price");
            form.addButton(new ElementButton(name + "\n§7Price: " + price));
        }

        pendingForms.put(player.getUniqueId(), commandKeys);
        player.showFormWindow(form);
        return true;
    }

    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (!pendingForms.containsKey(playerUUID)) {
            return;
        }

        FormWindow window = event.getWindow();
        if (window instanceof FormWindowSimple) {
            FormWindowSimple form = (FormWindowSimple) window;
            if (!form.getTitle().equals(FORM_TITLE)) {
                return;
            }

            List<String> commandKeys = pendingForms.remove(playerUUID);

            if (event.getResponse() == null) {
                return; // Form was closed
            }

            int buttonId = ((FormResponseSimple) event.getResponse()).getClickedButtonId();
            if (buttonId < 0 || buttonId >= commandKeys.size()) {
                return; // Invalid button
            }

            String commandKey = commandKeys.get(buttonId);
            Map<String, Object> commandData = buycConfig.getSection("commands." + commandKey).getAll();

            String commandToExecute = (String) commandData.get("execute");
            int price = (int) commandData.get("price");

            if (commandToExecute == null || price <= 0) {
                player.sendMessage("§cInvalid command configuration in buyc.yml.");
                return;
            }

            double playerMoney = EconomyAPI.getInstance().myMoney(player);
            if (playerMoney < price) {
                player.sendMessage("§cYou don't have enough money. Required: " + price + ", You have: " + playerMoney);
                return;
            }

            EconomyAPI.getInstance().reduceMoney(player, price);
            String finalCommand = commandToExecute.replace("%p%", player.getName());

            boolean success = this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), finalCommand);

            if (success) {
                player.sendMessage("§aYou have successfully purchased and executed the command for " + price + ".");
            } else {
                player.sendMessage("§cThe command failed to execute. You have been refunded " + price + ".");
                EconomyAPI.getInstance().addMoney(player, price);
            }
        }
    }
}
