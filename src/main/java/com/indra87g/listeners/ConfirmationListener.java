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
        if (confirmationManager.hasPendingConfirmation(player)) {
            event.setCancelled(true);
            String message = event.getMessage().toLowerCase();
            if ("yes".equals(message)) {
                confirmationManager.confirm(player);
            } else if ("no".equals(message)) {
                confirmationManager.cancel(player);
            } else {
                player.sendMessage(TextFormat.YELLOW + "Please type 'yes' or 'no'.");
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (confirmationManager.hasPendingConfirmation(player)) {
            event.setCancelled(true);
            player.sendMessage(TextFormat.RED + "You must respond to the pending confirmation before running another command.");
        }
    }
}