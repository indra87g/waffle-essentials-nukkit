package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;
import com.indra87g.Main;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class RedeemCommand extends BaseCommand {

    private final Main plugin;
    private final Config redeemCodesConfig;
    private final Config redeemUsersConfig;
    private final File redeemUsersFile;

    public RedeemCommand(String description, Main plugin) {
        super("redeem", description, "/redeem <code>");
        this.plugin = plugin;
        this.redeemCodesConfig = new Config(new File(plugin.getDataFolder(), "redeem_codes.yml"), Config.YAML);
        this.redeemUsersFile = new File(plugin.getDataFolder(), "redeem_users.yml");
        if (!this.redeemUsersFile.exists()) {
            plugin.saveResource("redeem_users.yml");
        }
        this.redeemUsersConfig = new Config(this.redeemUsersFile, Config.YAML);
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
        String code = args[0].toUpperCase();
        if (!redeemCodesConfig.exists(code)) {
            player.sendMessage("§cInvalid redeem code.");
            return false;
        }

        Map<String, Object> codeData = redeemCodesConfig.getSection(code).getAll();
        String permission = (String) codeData.getOrDefault("permission", "none");
        if (!"none".equalsIgnoreCase(permission) && !player.hasPermission(permission)) {
            player.sendMessage("§cYou do not have permission to redeem this code.");
            return false;
        }

        List<String> redeemedUsers = redeemUsersConfig.getStringList(player.getUniqueId().toString());
        if (redeemedUsers.contains(code)) {
            player.sendMessage("§cYou have already redeemed this code.");
            return false;
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
            return false;
        }

        String quotaStr = (String) codeData.getOrDefault("quota", "infinite");
        if (!"infinite".equalsIgnoreCase(quotaStr)) {
            int quota = Integer.parseInt(quotaStr);
            if (quota <= 0) {
                player.sendMessage("§cThis redeem code has reached its quota.");
                redeemCodesConfig.remove(code);
                redeemCodesConfig.save();
                return false;
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
        return true;
    }

    private void giveRewards(Player player, Map<String, Object> codeData) {
        int type = (int) codeData.get("type");
        switch (type) {
            case 1: // EconomyAPI money
                if (plugin.isEconomyAPIAvailable()) {
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
                        if (plugin.isEconomyAPIAvailable()) {
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