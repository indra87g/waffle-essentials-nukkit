package com.indra87g.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import com.indra87g.Main;
import com.indra87g.util.ConfigManager;
import angga7togk.coins.Coins;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerChatListener implements Listener {

    private final Main plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Integer> chatCount = new HashMap<>();

    public PlayerChatListener(Main plugin) {
        this.plugin = plugin;
        this.configManager = new ConfigManager(plugin);
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (!configManager.isChatToCoinEnabled()) {
            return;
        }

        if (!plugin.isCoinsAPIAvailable()) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.startsWith("/")) {
            return;
        }

        if (message.length() < configManager.getMinMessageLength()) {
            return;
        }

        UUID playerUUID = player.getUniqueId();
        int count = chatCount.getOrDefault(playerUUID, 0) + 1;
        chatCount.put(playerUUID, count);

        if (count >= configManager.getChatCount()) {
            chatCount.remove(playerUUID);
            int minCoins = configManager.getMinCoins();
            int maxCoins = configManager.getMaxCoins();
            int coinsToAdd = ThreadLocalRandom.current().nextInt(minCoins, maxCoins + 1);
            Coins.getInstance().getCoinsManager().addCoins(player, coinsToAdd);
            player.sendMessage("You have received " + coinsToAdd + " coins for being active!");
        }
    }
}