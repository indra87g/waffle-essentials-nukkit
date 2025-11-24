package com.indra87g.forms;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.window.FormWindowCustom;

public class RedeemForm {

    public static final int REDEMPTION_FORM_ID = 6755;

    public void sendRedemptionForm(Player player) {
        FormWindowCustom form = new FormWindowCustom("§b✧ Redeem Code ✧");
        form.addElement(new ElementInput("Enter your redeem code below:", "Example: HAPPYHOLIDAYS"));
        player.showFormWindow(form, REDEMPTION_FORM_ID);
    }
}