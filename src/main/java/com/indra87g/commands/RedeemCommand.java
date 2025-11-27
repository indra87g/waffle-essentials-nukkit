package com.indra87g.commands;

import cn.nukkit.Player;
import com.indra87g.Main;
import com.indra87g.forms.RedeemForm;

public class RedeemCommand extends BaseCommand {

    private final Main plugin;

    public RedeemCommand(String description, Main plugin) {
        super("redeem", description, "/redeem");
        this.plugin = plugin;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        RedeemForm redeemForm = new RedeemForm();
        redeemForm.sendRedemptionForm(player);
        return true;
    }
}