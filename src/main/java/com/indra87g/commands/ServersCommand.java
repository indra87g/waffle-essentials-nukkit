package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.network.protocol.TransferPacket;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.form.window.FormWindowSimple;
import com.indra87g.Main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServersCommand extends Command implements Listener {

    private final Main plugin;
    private final Map<UUID, Integer> formIDs = new HashMap<>();

    public ServersCommand(Main plugin) {
        super("servers", "Display a list of servers to connect to.", "/servers");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return false;
        }

        Player player = (Player) sender;

        List<Map> servers = plugin.getServers();
        if (servers == null || servers.isEmpty()) {
            player.sendMessage("There are no servers available at the moment. Please try again later.");
            return true;
        }

        FormWindowSimple form = new FormWindowSimple("Server Selector", "Choose a server to connect to.");
        for (Map serverData : servers) {
            form.addButton(new ElementButton(String.valueOf(serverData.get("name"))));
        }

        int formId = player.showFormWindow(form);
        formIDs.put(player.getUniqueId(), formId);

        return true;
    }

    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (formIDs.containsKey(playerUUID) && formIDs.get(playerUUID) == event.getFormID()) {
            formIDs.remove(playerUUID); // Prevent re-handling

            if (event.getResponse() == null) {
                return; // Form was closed
            }

            if (event.getWindow() instanceof FormWindowSimple) {
                FormWindowSimple form = (FormWindowSimple) event.getWindow();
                int buttonIndex = form.getResponse().getClickedButtonId();

                List<Map> servers = plugin.getServers();
                if (servers == null || buttonIndex >= servers.size()) {
                    return; // Should not happen
                }

                Map serverData = servers.get(buttonIndex);
                String address = String.valueOf(serverData.get("address"));
                int port = ((Number) serverData.get("port")).intValue();

                if (plugin.getTeleportingPlayers().containsKey(playerUUID)) {
                    player.sendMessage("You are already being transferred to a server.");
                    return;
                }

                plugin.getPlayerLocations().put(playerUUID, player.getLocation());
                plugin.getTeleportingPlayers().put(playerUUID, player);
                CountdownTask task = new CountdownTask(plugin, player, address, port);
                plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, task, 20);
                plugin.getCountdowns().put(playerUUID, task);
            }
        }
    }

    private static class CountdownTask extends PluginTask<Main> {

        private final Player player;
        private final String address;
        private final int port;
        private int countdown = 3;

        public CountdownTask(Main owner, Player player, String address, int port) {
            super(owner);
            this.player = player;
            this.address = address;
            this.port = port;
        }

        @Override
        public void onRun(int currentTick) {
            if (!player.isOnline() || !this.getOwner().getTeleportingPlayers().containsKey(player.getUniqueId())) {
                this.cancel();
                this.getOwner().getTeleportingPlayers().remove(player.getUniqueId());
                this.getOwner().getCountdowns().remove(player.getUniqueId());
                this.getOwner().getPlayerLocations().remove(player.getUniqueId());
                return;
            }

            if (countdown > 0) {
                player.sendTitle("§eTeleporting in " + countdown + "...", "", 0, 25, 5);
                countdown--;
            } else {
                player.sendTitle("§aTransferring...", "", 0, 20, 0);
                TransferPacket pk = new TransferPacket();
                pk.address = address;
                pk.port = port;
                player.dataPacket(pk);
                this.getOwner().getTeleportingPlayers().remove(player.getUniqueId());
                this.getOwner().getCountdowns().remove(player.getUniqueId());
                this.getOwner().getPlayerLocations().remove(player.getUniqueId());
                this.cancel();
            }
        }
    }
}
