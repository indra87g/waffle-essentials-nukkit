package com.indra87g.util;

import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Config;
import java.io.File;
import lombok.Getter;

@Getter
public class ConfigManager {

    private final Plugin plugin;
    private final Config config;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        this.config = new Config(new File(plugin.getDataFolder(), "config.yml"), Config.YAML);
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
}
