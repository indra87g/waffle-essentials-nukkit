package com.indra87g.commands;

import cn.nukkit.Player;
import com.indra87g.Main;
import com.indra87g.forms.RedeemAdminForm;
import com.indra87g.forms.RedeemForm;

public class RedeemCommand extends BaseCommand {

    private final Main plugin;

    public RedeemCommand(String description, Main plugin) {
        super("redeem", description, "/redeem [admin]");
        this.plugin = plugin;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        if (args.length > 0 && "admin".equalsIgnoreCase(args[0])) {
            if (player.hasPermission("waffle.redeem.admin")) {
                RedeemAdminForm adminForm = new RedeemAdminForm();
                adminForm.sendAdminMenu(player);
            } else {
                player.sendMessage("§cYou do not have permission to use this command.");
            }
        } else {
            RedeemForm redeemForm = new RedeemForm();
            redeemForm.sendRedemptionForm(player);
        }
        return true;
    }
}
