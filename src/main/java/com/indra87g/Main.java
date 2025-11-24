package com.indra87g;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;
import com.indra87g.commands.*;
import com.indra87g.forms.InfoForm;
import com.indra87g.util.ConfigManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import lombok.Getter;

@Getter
public class Main extends PluginBase implements Listener {

    private ConfigManager configManager;
    private List<Map> servers;
    private InfoForm infoForm;
    private final Map<UUID, PluginTask<?>> countdowns = new HashMap<>();
    private final Map<UUID, Player> teleportingPlayers = new HashMap<>();
    private final Map<UUID, cn.nukkit.level.Location> playerLocations = new HashMap<>();
    private boolean economyAPIAvailable = false;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.saveResource("servers.yml");
        this.saveResource("redeem_codes.yml");
        this.saveResource("wshop.yml");

        configManager = new ConfigManager(this);
        infoForm = new InfoForm(this);

        Config serversConfig = new Config(new File(this.getDataFolder(), "servers.yml"), Config.YAML);
        this.servers = serversConfig.getMapList("servers");

        getLogger().info("WaffleCoreNK has been enabled.");

        checkEconomyAPI();
        registerCommands();

        this.getServer().getPluginManager().registerEvents(new com.indra87g.listeners.PlayerMoveListener(this), this);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    private void checkEconomyAPI() {
        if (getServer().getPluginManager().getPlugin("EconomyAPI") != null) {
            economyAPIAvailable = true;
            getLogger().info("EconomyAPI integration enabled!");
        } else {
            getLogger().info("EconomyAPI integration disabled!");
        }
    }

    private void registerCommands() {
        // Commands with special registration logic
        if (configManager.isCommandEnabled("servers")) {
            String description = configManager.getCommandDescription("servers", "Shows a list of available servers.");
            ServersCommand serversCommand = new ServersCommand(this, description);
            this.getServer().getPluginManager().registerEvents(serversCommand, this);
            this.getServer().getCommandMap().register("servers", serversCommand);
        }

        if (configManager.isCommandEnabled("fastmsg")) {
            String description = configManager.getCommandDescription("fastmsg", "Sends a predefined message. Usage: /fastmsg <message_key>");
            this.getServer().getCommandMap().register("fastmsg", new FastMsgCommand(description, configManager, this));
        }

        // Simple commands that only need a description
        registerSimpleCommand("setblock", "Sets a block at the player's location. Usage: /setblock <block_id>", (desc, main) -> new SetBlockCommand(desc));
        registerSimpleCommand("clearchat", "Clear your chat", (desc, main) -> new ClearChatCommand(desc));
        registerSimpleCommand("info", "Shows your player information", InfoCommand::new);
        registerSimpleCommand("redeem", "Redeem a code for a reward", RedeemCommand::new);
    }

    private void registerSimpleCommand(String name, String defaultDescription, BiFunction<String, Main, Command> constructor) {
        if (configManager.isCommandEnabled(name)) {
            String description = configManager.getCommandDescription(name, defaultDescription);
            this.getServer().getCommandMap().register(name, constructor.apply(description, this));
        }
    }

    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        if (event.getFormID() == InfoForm.MAIN_FORM_ID) {
            if (event.getWindow() instanceof FormWindowSimple) {
                FormWindowSimple form = (FormWindowSimple) event.getWindow();
                FormResponseSimple response = form.getResponse();
                int clickedButtonId = response.getClickedButtonId();

                switch (clickedButtonId) {
                    case 0: // Player Info
                        infoForm.sendPlayerInfoForm(player);
                        break;
                    case 1: // Item Info
                        infoForm.sendItemInfoForm(player);
                        break;
                    case 2: // Server Info
                        infoForm.sendServerInfoForm(player);
                        break;
                }
            }
        }
    }
}
