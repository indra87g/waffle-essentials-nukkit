package com.indra87g.commands;

import cn.nukkit.Player;
import com.indra87g.Main;

public class InfoCommand extends BaseCommand {

    private final Main plugin;

    public InfoCommand(String description, Main plugin) {
        super("info", description, "/info");
        this.plugin = plugin;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        plugin.getInfoForm().sendMainForm(player);
        return true;
    }
}