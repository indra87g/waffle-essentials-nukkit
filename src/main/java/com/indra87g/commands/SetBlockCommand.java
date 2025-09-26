package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;

public class SetBlockCommand extends BaseCommand {

    public SetBlockCommand() {
        super("setblock", "Sets a block at a specific location", "/setblock <x> <y> <z> <block_id>", "waffle.setblock");
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        if (args.length != 4) {
            player.sendMessage("§cUsage: " + this.getUsage());
            return false;
        }
        return true;
    }

    @Override
    public boolean handleCommand(Player player, String[] args) {
        Level level = player.getLevel();

        try {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);
            int blockId = Integer.parseInt(args[3]);

            Vector3 pos = new Vector3(x, y, z);
            Block block = Block.get(blockId);

            level.setBlock(pos, block);
            player.sendMessage("§aBlock " + block.getName() + " successfully placed at (" + x + ", " + y + ", " + z + ")");
        } catch (NumberFormatException e) {
            player.sendMessage("§cPlease provide valid numbers for coordinates and block ID.");
        }

        return true;
    }
}
