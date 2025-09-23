package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.network.protocol.TransferPacket;
import cn.nukkit.scheduler.PluginTask;
import com.github.mefrreex.formconstructor.form.SimpleForm;
import com.indra87g.Main;

import java.util.List;
import java.util.Map;

public class ServersCommand extends Command {

    private final Main plugin;

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

        List<Map<?, ?>> servers = plugin.getServers();
        if (servers == null || servers.isEmpty()) {
            player.sendMessage("There are no servers available at the moment. Please try again later.");
            return true;
        }

        SimpleForm form = new SimpleForm.Builder("Server Selector", "Choose a server to connect to.").build();

        for (Map<?, ?> serverData : servers) {
            String name = (String) serverData.get("name");
            String address = (String) serverData.get("address");
            int port = (int) serverData.get("port");

            form.addButton(name, (p, b) -> {
                if (plugin.getTeleportingPlayers().containsKey(p.getUniqueId())) {
                    p.sendMessage("You are already being transferred to a server.");
                    return;
                }

                plugin.getPlayerLocations().put(p.getUniqueId(), p.getLocation());
                plugin.getTeleportingPlayers().put(p.getUniqueId(), p);
                CountdownTask task = new CountdownTask(plugin, p, address, port);
                plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, task, 20);
                plugin.getCountdowns().put(p.getUniqueId(), task);
            });
        }

        form.send(player);

        return true;
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
                this.cancel();
            }
        }
    }
}
