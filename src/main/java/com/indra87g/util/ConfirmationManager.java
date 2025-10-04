package com.indra87g.util;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConfirmationManager {
    private final Map<UUID, Runnable> pendingConfirmations = new ConcurrentHashMap<>();

    public void requestConfirmation(Player player, Runnable onConfirm) {
        pendingConfirmations.put(player.getUniqueId(), onConfirm);
        player.sendMessage(TextFormat.YELLOW + "Are you sure? Type 'yes' to confirm or 'no' to cancel.");
    }

    public boolean hasPendingConfirmation(Player player) {
        return pendingConfirmations.containsKey(player.getUniqueId());
    }

    public void confirm(Player player) {
        Runnable onConfirm = pendingConfirmations.remove(player.getUniqueId());
        if (onConfirm != null) {
            onConfirm.run();
            player.sendMessage(TextFormat.GREEN + "Action confirmed.");
        }
    }

    public void cancel(Player player) {
        if (pendingConfirmations.remove(player.getUniqueId()) != null) {
            player.sendMessage(TextFormat.RED + "Action cancelled.");
        }
    }
}