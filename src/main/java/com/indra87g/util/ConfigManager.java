package com.indra87g.util;

import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Config;
import java.io.File;
import java.util.Map;

public class ConfigManager {

    private final Plugin plugin;
    private Config config;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        config = new Config(new File(plugin.getDataFolder(), "config.yml"), Config.YAML);
    }

    public boolean isCommandEnabled(String commandName) {
        return config.getBoolean("commands." + commandName + ".enabled", true);
    }

    public String getCommandDescription(String commandName, String defaultDescription) {
        return config.getString("commands." + commandName + ".description", defaultDescription);
    }

    public String getFastMessage(String messageKey) {
        return config.getString("fast_messages." + messageKey, null);
    }

    public boolean isChatToCoinEnabled() {
        return config.getBoolean("chat_to_coin.enabled", true);
    }

    public int getChatCount() {
        return config.getInt("chat_to_coin.chat_count", 10);
    }

    public int getMinMessageLength() {
        return config.getInt("chat_to_coin.min_message_length", 5);
    }

    public int getMinCoins() {
        return config.getInt("chat_to_coin.min_coins", 1);
    }

    public int getMaxCoins() {
        return config.getInt("chat_to_coin.max_coins", 3);
    }

    public boolean isCoinConversionEnabled() {
        return config.getBoolean("coin_conversion.enabled", true);
    }

    public int getConversionRateCoins() {
        return config.getInt("coin_conversion.rate.coins", 10);
    }

    public int getConversionRateMoney() {
        return config.getInt("coin_conversion.rate.money", 500);
    }
}