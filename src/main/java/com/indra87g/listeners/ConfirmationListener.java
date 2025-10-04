package com.indra87g.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.utils.TextFormat;
import com.indra87g.util.ConfirmationManager;

public class ConfirmationListener implements Listener {
    private final ConfirmationManager confirmationManager;

    public ConfirmationListener(ConfirmationManager confirmationManager) {
        this.confirmationManager = confirmationManager;
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!confirmationManager.hasPendingConfirmation(player.getUniqueId())) {
            return;
        }

        String message = event.getMessage().toLowerCase();
        if (message.equals("confirm")) {
            confirmationManager.confirm(player);
            event.setCancelled(true);
        } else if (message.equals("cancel")) {
            confirmationManager.cancel(player);
            event.setCancelled(true);
        } else {
            player.sendMessage(TextFormat.RED + "You have a pending confirmation. Please type 'confirm' or 'cancel'.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (confirmationManager.hasPendingConfirmation(player.getUniqueId())) {
            player.sendMessage(TextFormat.RED + "You have a pending confirmation. Please type 'confirm' or 'cancel' in the chat.");
            event.setCancelled(true);
        }
    }
}