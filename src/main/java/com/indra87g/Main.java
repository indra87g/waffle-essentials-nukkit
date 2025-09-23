package com.indra87g;

import cn.nukkit.plugin.PluginBase;
import com.indra87g.commands.SetBlockCommand;

public class Main extends PluginBase {

    @Override
    public void onEnable() {
        getLogger().info("WaffleCoreNK has been enabled.");
        this.getServer().getCommandMap().register("setblock", new SetBlockCommand());
    }
}
