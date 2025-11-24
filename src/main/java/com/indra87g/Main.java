package com.indra87g;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;
import com.indra87g.commands.*;
import com.indra87g.forms.InfoForm;
import com.indra87g.forms.RedeemForm;
import com.indra87g.util.ConfigManager;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.time.Instant;
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

        primeRedeemCodeTimestamps();

        configManager = new ConfigManager(this);
        infoForm = new InfoForm(this);

        Config serversConfig = new Config(new File(this.getDataFolder(), "servers.yml"), Config.YAML);
        this.servers = serversConfig.getMapList("servers");

        getLogger().info("WaffleEssentialsNK has been enabled.");

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

    private void primeRedeemCodeTimestamps() {
        Config redeemCodesConfig = new Config(new File(getDataFolder(), "redeem_codes.yml"), Config.YAML);
        boolean changed = false;
        for (String code : redeemCodesConfig.getKeys(false)) {
            Map<String, Object> codeData = redeemCodesConfig.getSection(code).getAll();
            if (codeData.containsKey("expired") && !codeData.containsKey("creation_timestamp")) {
                long expiredHours = ((Number) codeData.get("expired")).longValue();
                if (expiredHours != -1) {
                    codeData.put("creation_timestamp", Instant.now().getEpochSecond());
                    redeemCodesConfig.set(code, codeData);
                    changed = true;
                }
            }
        }
        if (changed) {
            redeemCodesConfig.save();
            getLogger().info("Primed timestamps for new redeem codes.");
        }
    }

    private void registerCommands() {
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
        } else if (event.getFormID() == RedeemForm.REDEMPTION_FORM_ID) {
            if (event.getWindow() instanceof FormWindowCustom) {
                FormWindowCustom form = (FormWindowCustom) event.getWindow();
                FormResponseCustom response = form.getResponse();
                String code = response.getInputResponse(0).toUpperCase();

                Config redeemCodesConfig = new Config(new File(getDataFolder(), "redeem_codes.yml"), Config.YAML);
                File redeemUsersFile = new File(getDataFolder(), "redeem_users.yml");
                if (!redeemUsersFile.exists()) {
                    saveResource("redeem_users.yml");
                }
                Config redeemUsersConfig = new Config(redeemUsersFile, Config.YAML);

                if (!redeemCodesConfig.exists(code)) {
                    player.sendMessage("§cInvalid redeem code.");
                    return;
                }

                Map<String, Object> codeData = redeemCodesConfig.getSection(code).getAll();
                String permission = (String) codeData.getOrDefault("permission", "none");
                if (!"none".equalsIgnoreCase(permission) && !player.hasPermission(permission)) {
                    player.sendMessage("§cYou do not have permission to redeem this code.");
                    return;
                }

                List<String> redeemedUsers = redeemUsersConfig.getStringList(player.getUniqueId().toString());
                if (redeemedUsers.contains(code)) {
                    player.sendMessage("§cYou have already redeemed this code.");
                    return;
                }

                if (!codeData.containsKey("creation_timestamp")) {
                    codeData.put("creation_timestamp", Instant.now().getEpochSecond());
                }
                long creationTimestamp = ((Number) codeData.get("creation_timestamp")).longValue();
                long expiredHours = ((Number) codeData.getOrDefault("expired", -1)).longValue();
                if (expiredHours != -1 && (creationTimestamp + (expiredHours * 3600)) < Instant.now().getEpochSecond()) {
                    player.sendMessage("§cThis redeem code has expired.");
                    redeemCodesConfig.remove(code);
                    redeemCodesConfig.save();
                    return;
                }

                String quotaStr = (String) codeData.getOrDefault("quota", "infinite");
                if (!"infinite".equalsIgnoreCase(quotaStr)) {
                    int quota = Integer.parseInt(quotaStr);
                    if (quota <= 0) {
                        player.sendMessage("§cThis redeem code has reached its quota.");
                        redeemCodesConfig.remove(code);
                        redeemCodesConfig.save();
                        return;
                    }
                    codeData.put("quota", String.valueOf(quota - 1));
                }

                giveRewards(player, codeData);

                redeemedUsers.add(code);
                redeemUsersConfig.set(player.getUniqueId().toString(), redeemedUsers);
                redeemUsersConfig.save();

                if (!"infinite".equalsIgnoreCase(quotaStr) && Integer.parseInt((String) codeData.get("quota")) <= 0) {
                    redeemCodesConfig.remove(code);
                } else {
                    redeemCodesConfig.set(code, codeData);
                }
                redeemCodesConfig.save();

                player.sendMessage("§aCode redeemed successfully!");
            }
        }
    }

    private void giveRewards(Player player, Map<String, Object> codeData) {
        int type = (int) codeData.get("type");
        switch (type) {
            case 1: // EconomyAPI money
                if (isEconomyAPIAvailable()) {
                    double moneyValue = ((Number) codeData.get("money_value")).doubleValue();
                    EconomyAPI.getInstance().addMoney(player, moneyValue);
                    player.sendMessage("§aYou have received " + moneyValue + " money.");
                }
                break;
            case 2: // Items
                int itemId = (int) codeData.get("item_id");
                int itemValue = (int) codeData.get("item_value");
                player.getInventory().addItem(new Item(itemId, 0, itemValue));
                player.sendMessage("§aYou have received " + itemValue + " of item " + itemId + ".");
                break;
            case 3: // Effects
                String effectName = (String) codeData.get("effect_name");
                int effectTier = (int) codeData.get("effect_tier");
                String effectTimeStr = (String) codeData.get("effect_time");
                int effectTime = "infinite".equalsIgnoreCase(effectTimeStr) ? Integer.MAX_VALUE : Integer.parseInt(effectTimeStr);
                Effect effect = Effect.getEffectByName(effectName);
                if (effect != null) {
                    effect.setAmplifier(effectTier - 1).setDuration(effectTime * 20);
                    player.addEffect(effect);
                    player.sendMessage("§aYou have received the " + effectName + " effect.");
                }
                break;
            case 4: // Multiple Rewards
                List<Map<String, Object>> rewards = (List<Map<String, Object>>) codeData.get("rewards");
                for (Map<String, Object> reward : rewards) {
                    if (reward.containsKey("money")) {
                        Map<String, Object> moneyData = (Map<String, Object>) reward.get("money");
                        if (isEconomyAPIAvailable()) {
                            double value = ((Number) moneyData.get("value")).doubleValue();
                            EconomyAPI.getInstance().addMoney(player, value);
                            player.sendMessage("§aYou have received " + value + " money.");
                        }
                    } else if (reward.containsKey("item")) {
                        Map<String, Object> itemData = (Map<String, Object>) reward.get("item");
                        int id = (int) itemData.get("id");
                        int value = (int) itemData.get("value");
                        player.getInventory().addItem(new Item(id, 0, value));
                        player.sendMessage("§aYou have received " + value + " of item " + id + ".");
                    } else if (reward.containsKey("effect")) {
                        Map<String, Object> effectData = (Map<String, Object>) reward.get("effect");
                        String name = (String) effectData.get("name");
                        int tier = (int) effectData.get("tier");
                        String timeStr = (String) effectData.get("time");
                        int time = "infinite".equalsIgnoreCase(timeStr) ? Integer.MAX_VALUE : Integer.parseInt(timeStr);
                        Effect eff = Effect.getEffectByName(name);
                        if (eff != null) {
                            eff.setAmplifier(tier - 1).setDuration(time * 20);
                            player.addEffect(eff);
                            player.sendMessage("§aYou have received the " + name + " effect.");
                        }
                    }
                }
                break;
        }
    }
}
