package com.indra87g.forms;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import com.indra87g.Main;
import lombok.RequiredArgsConstructor;
import me.onebone.economyapi.EconomyAPI;

@RequiredArgsConstructor
public class InfoForm {

    public static final int MAIN_FORM_ID = 6754; // Unique ID for the main form
    private final Main plugin;

    public void sendMainForm(Player player) {
        FormWindowSimple form = new FormWindowSimple("§b✧ Info Menu ✧", "Select an option below:");

        if (player.hasPermission("waffle.info.player") || player.hasPermission("waffle.info.all")) {
            form.addButton(new ElementButton("Player Info",
                    new ElementButtonImageData("path", "textures/items/name_tag")));
        }
        if (player.hasPermission("waffle.info.all")) {
            form.addButton(new ElementButton("Item Info",
                    new ElementButtonImageData("path", "textures/items/diamond_sword")));
            form.addButton(new ElementButton("Server Info",
                    new ElementButtonImageData("path", "textures/items/compass")));
        }

        player.showFormWindow(form, MAIN_FORM_ID);
    }

    public void sendPlayerInfoForm(Player player) {
        String playerName = player.getName();
        int playerLevel = player.getExperienceLevel();
        double playerHealth = player.getHealth();
        int playerFood = player.getFoodData().getLevel();
        int playerExperience = player.getExperience();
        int xpToLevelUp = player.calculateRequireExperience(player.getExperienceLevel());
        int ping = player.getPing();
        double money = plugin.isEconomyAPIAvailable() ? EconomyAPI.getInstance().myMoney(player) : -1;

        Position respawn = player.getSpawn();
        String respawnPoint = String.format("X: %.2f, Y: %.2f, Z: %.2f, World: %s",
                respawn.getX(), respawn.getY(), respawn.getZ(), respawn.getLevel().getName());

        StringBuilder myInfo = new StringBuilder();
        myInfo.append(TextFormat.AQUA).append(playerName).append(TextFormat.WHITE).append(" Level ").append(TextFormat.YELLOW).append(playerLevel).append("\n");
        myInfo.append(TextFormat.WHITE).append("HP: ").append(TextFormat.RED).append(String.format("%.2f", playerHealth)).append("\n");
        myInfo.append(TextFormat.WHITE).append("Hunger: ").append(TextFormat.GOLD).append(playerFood).append("\n");
        myInfo.append(TextFormat.WHITE).append("XP: ").append(TextFormat.GREEN).append(playerExperience).append("/").append(xpToLevelUp).append("\n");
        if (money != -1) {
            myInfo.append(TextFormat.WHITE).append("Money: ").append(TextFormat.GOLD).append(String.format("%.2f", money)).append("\n");
        }
        myInfo.append(TextFormat.WHITE).append("Ping: ").append(TextFormat.AQUA).append(ping).append("ms\n");
        myInfo.append(TextFormat.WHITE).append("Respawn Point: ").append(TextFormat.LIGHT_PURPLE).append(respawnPoint);

        FormWindowSimple form = new FormWindowSimple("§b✧ Player Info ✧", myInfo.toString());
        player.showFormWindow(form);
    }

    public void sendItemInfoForm(Player player) {
        StringBuilder itemInfo = new StringBuilder();
        itemInfo.append(TextFormat.AQUA).append("Hotbar:\n");

        for (int i = 0; i < 9; i++) {
            Item item = player.getInventory().getItem(i);
            if (item.getId() != Item.AIR) {
                itemInfo.append(TextFormat.WHITE).append(i + 1).append(". ").append(item.getName()).append(" (").append(item.getId()).append(")");
                if (item.getMaxStackSize() > 1) {
                    itemInfo.append(" x").append(item.getCount());
                }
                itemInfo.append("\n");
            }
        }

        itemInfo.append("\n").append(TextFormat.AQUA).append("Attachment:\n");
        Item[] armor = player.getInventory().getArmorContents();
        String[] armorSlotNames = {"Helmet", "Chestplate", "Leggings", "Boots"};
        for (int i = 0; i < armor.length; i++) {
            Item item = armor[i];
            if (item.getId() != Item.AIR) {
                itemInfo.append(TextFormat.WHITE).append(armorSlotNames[i]).append(": ").append(item.getName()).append(" (").append(item.getId()).append(")\n");
            }
        }
        for (Item item : player.getInventory().getContents().values()) {
            if (item instanceof cn.nukkit.item.ItemShield) {
                itemInfo.append(TextFormat.WHITE).append("Shield: ").append(item.getName()).append(" (").append(item.getId()).append(")\n");
            }
        }

        FormWindowSimple form = new FormWindowSimple("§b✧ Item Info ✧", itemInfo.toString());
        player.showFormWindow(form);
    }

    public void sendServerInfoForm(Player player) {
        Server server = player.getServer();
        Runtime runtime = Runtime.getRuntime();

        double maxMemory = runtime.maxMemory() / 1024.0 / 1024.0 / 1024.0;
        double totalMemory = runtime.totalMemory() / 1024.0 / 1024.0 / 1024.0;
        double freeMemory = runtime.freeMemory() / 1024.0 / 1024.0 / 1024.0;
        double usedMemory = totalMemory - freeMemory;

        String serverInfo = TextFormat.WHITE + "RAM Usage: " + TextFormat.GREEN + String.format("%.2fGB/%.2fGB\n", usedMemory, maxMemory) +
                TextFormat.WHITE + "CPU Usage: " + TextFormat.YELLOW + String.format("%.2f", server.getTickUsage()) + "%/100%\n" +
                TextFormat.WHITE + "Player Online: " + TextFormat.AQUA + server.getOnlinePlayers().size() + "/" + server.getMaxPlayers() + "\n" +
                TextFormat.WHITE + "TPS: " + TextFormat.GOLD + server.getTicksPerSecond() + "\n" +
                "\n" +
                TextFormat.WHITE + "IP: " + TextFormat.LIGHT_PURPLE + server.getIp() + "\n" +
                TextFormat.WHITE + "Port: " + TextFormat.LIGHT_PURPLE + server.getPort() + "\n" +
                TextFormat.WHITE + "MOTD: " + TextFormat.GRAY + server.getMotd();

        FormWindowSimple form = new FormWindowSimple("§b✧ Server Info ✧", serverInfo);
        player.showFormWindow(form);
    }
}
