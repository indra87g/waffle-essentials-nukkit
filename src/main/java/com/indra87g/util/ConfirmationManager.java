package com.indra87g.util;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfirmationManager {
    private final Map<UUID, Runnable> pendingConfirmations = new HashMap<>();

    public void requestConfirmation(Player player, String message, Runnable onConfirm) {
        if (pendingConfirmations.containsKey(player.getUniqueId())) {
            player.sendMessage(TextFormat.RED + "You already have a pending confirmation request.");
            return;
        }
        pendingConfirmations.put(player.getUniqueId(), onConfirm);
        player.sendMessage(TextFormat.YELLOW + message);
    }

    public boolean hasPendingConfirmation(UUID playerId) {
        return pendingConfirmations.containsKey(playerId);
    }

    public void confirm(Player player) {
        Runnable onConfirm = pendingConfirmations.remove(player.getUniqueId());
        if (onConfirm != null) {
            onConfirm.run();
        }
    }

    public void cancel(Player player) {
        if (pendingConfirmations.remove(player.getUniqueId()) != null) {
            player.sendMessage(TextFormat.YELLOW + "Confirmation cancelled.");
        }
    }
}