package com.indra87g.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.indra87g.Main;
import java.io.File;
import java.util.Random;
import java.text.NumberFormat;
import java.util.Locale;

public class PlayerLoginListener implements Listener {

    private final Main plugin;
    private final Config bankData;

    public PlayerLoginListener(Main plugin) {
        this.plugin = plugin;
        this.bankData = new Config(new File(plugin.getDataFolder(), "bank.yml"), Config.YAML);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        if (bankData.exists(playerName)) {
            double currentMoney = bankData.getDouble(playerName + ".money", 0);
            double maxMoney = bankData.getDouble(playerName + ".max_money", 1000000);

            // Add random interest between 100 and 200
            int interest = new Random().nextInt(101) + 100;
            double newMoney = currentMoney + interest;

            if (newMoney > maxMoney) {
                newMoney = maxMoney;
            }

            bankData.set(playerName + ".money", newMoney);
            bankData.save();

            NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));
            player.sendMessage(TextFormat.GREEN + "You received Rp" + nf.format(interest) + " interest for logging in!");
        }
    }
}
