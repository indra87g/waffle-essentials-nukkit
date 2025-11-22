package com.indra87g.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.level.Location;
import cn.nukkit.scheduler.PluginTask;
import com.indra87g.Main;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerMoveListener implements Listener {

    private final Main plugin;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (plugin.getTeleportingPlayers().containsKey(playerUUID)) {
            Location from = plugin.getPlayerLocations().get(playerUUID);
            Location to = event.getTo();

            if (from.getFloorX() != to.getFloorX() || from.getFloorZ() != to.getFloorZ()) {
                plugin.getTeleportingPlayers().remove(playerUUID);
                plugin.getPlayerLocations().remove(playerUUID);

                PluginTask<?> task = plugin.getCountdowns().remove(playerUUID);
                if (task != null) {
                    task.cancel();
                }

                player.sendTitle("§cTeleportation cancelled.", "You moved.", 0, 20, 5);
            }
        }
    }
}
