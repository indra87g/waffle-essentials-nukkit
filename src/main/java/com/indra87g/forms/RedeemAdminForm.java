package com.indra87g.forms;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

public class RedeemAdminForm {

    public static final int REDEEM_ADMIN_FORM_ID = 6756;
    public static final int REDEEM_ADMIN_CREATE_FORM_ID = 6757;
    public static final int REDEEM_ADMIN_READ_LIST_FORM_ID = 6758;
    public static final int REDEEM_ADMIN_UPDATE_LIST_FORM_ID = 6759;
    public static final int REDEEM_ADMIN_UPDATE_FORM_ID = 6760;
    public static final int REDEEM_ADMIN_DELETE_LIST_FORM_ID = 6761;
    public static final int REDEEM_ADMIN_DELETE_CONFIRM_FORM_ID = 6762;

    public void sendAdminMenu(Player player) {
        FormWindowSimple form = new FormWindowSimple("§b✧ Redeem Admin ✧", "Select an option below:");
        form.addButton(new ElementButton("Create Code"));
        form.addButton(new ElementButton("Read Codes"));
        form.addButton(new ElementButton("Update Code"));
        form.addButton(new ElementButton("Delete Code"));
        player.showFormWindow(form, REDEEM_ADMIN_FORM_ID);
    }

    public void sendCodeList(Player player, Config redeemCodesConfig, int formId) {
        FormWindowSimple form = new FormWindowSimple("§b✧ Redeem Codes ✧", "Select a code:");
        for (String code : redeemCodesConfig.getKeys(false)) {
            form.addButton(new ElementButton(code));
        }
        player.showFormWindow(form, formId);
    }

    public void sendCodeDetails(Player player, String code, Map<String, Object> codeData) {
        StringBuilder details = new StringBuilder();
        details.append("§eQuota: §f").append(codeData.get("quota")).append("\n");
        details.append("§ePermission: §f").append(codeData.get("permission")).append("\n");
        details.append("§eReward Type: §f").append(codeData.get("type")).append("\n\n");

        // Add more details based on reward type
        switch ((int) codeData.get("type")) {
            case 1:
                details.append("§eMoney Value: §f").append(codeData.get("money_value"));
                break;
            case 2:
                details.append("§eItem ID: §f").append(codeData.get("item_id")).append("\n");
                details.append("§eItem Amount: §f").append(codeData.get("item_value"));
                break;
            case 3:
                details.append("§eEffect Name: §f").append(codeData.get("effect_name")).append("\n");
                details.append("§eEffect Tier: §f").append(codeData.get("effect_tier")).append("\n");
                details.append("§eEffect Duration: §f").append(codeData.get("effect_time"));
                break;
            case 4:
                details.append("§eRewards: §f").append(codeData.get("rewards").toString());
                break;
        }

        FormWindowSimple form = new FormWindowSimple("§b✧ Details: " + code + " ✧", details.toString());
        player.showFormWindow(form); // No action needed on close
    }

    public void sendUpdateForm(Player player, String code, Map<String, Object> codeData) {
        FormWindowCustom form = new FormWindowCustom("§b✧ Update: " + code + " ✧");
        form.addElement(new ElementInput("Quota", "e.g., 100 or 'infinite'", (String) codeData.get("quota")));
        form.addElement(new ElementInput("Required Permission", "e.g., waffle.vip or 'none'", (String) codeData.get("permission")));

        player.showFormWindow(form, REDEEM_ADMIN_UPDATE_FORM_ID);
    }

    public void sendDeleteConfirmation(Player player, String code) {
        FormWindowSimple form = new FormWindowSimple("§cConfirm Deletion", "Are you sure you want to delete the redeem code '" + code + "'? This action cannot be undone.");
        form.addButton(new ElementButton("Yes, delete it"));
        form.addButton(new ElementButton("No, cancel"));
        player.showFormWindow(form, REDEEM_ADMIN_DELETE_CONFIRM_FORM_ID);
    }

    public void sendCreateForm(Player player) {
        FormWindowCustom form = new FormWindowCustom("§b✧ Create Redeem Code ✧");
        form.addElement(new ElementInput("Redeem Code", "e.g., WELCOMEGIFT"));
        form.addElement(new ElementInput("Quota", "e.g., 100 or 'infinite'"));
        form.addElement(new ElementInput("Required Permission", "e.g., waffle.vip or 'none'"));
        form.addElement(new ElementDropdown("Reward Type", Arrays.asList("Economy", "Item", "Effect", "Multiple Rewards")));

        // Fields for all reward types
        form.addElement(new ElementInput("Money Value (for Economy)", "e.g., 5000"));
        form.addElement(new ElementInput("Item ID (for Item)", "e.g., 264"));
        form.addElement(new ElementInput("Item Amount (for Item)", "e.g., 10"));
        form.addElement(new ElementInput("Effect Name (for Effect)", "e.g., speed"));
        form.addElement(new ElementInput("Effect Tier (for Effect)", "e.g., 2"));
        form.addElement(new ElementInput("Effect Duration (seconds, for Effect)", "e.g., 60 or 'infinite'"));
        form.addElement(new ElementInput("Multiple Rewards (Advanced)", "See console for format"));

        player.showFormWindow(form, REDEEM_ADMIN_CREATE_FORM_ID);

        // Instructions for the complex "Multiple Rewards" format
        player.sendMessage("§e--- Multiple Rewards Format ---");
        player.sendMessage("§eTo create multiple rewards, enter a list in YAML format in the 'Multiple Rewards' field.");
        player.sendMessage("§eExample:");
        player.sendMessage("§f- money:\n    value: 1000\n- item:\n    id: 264\n    value: 5");
        player.sendMessage("§eYou must use spaces, not tabs. Leave the other reward fields (Money, Item, Effect) blank if using this.");
    }
}
