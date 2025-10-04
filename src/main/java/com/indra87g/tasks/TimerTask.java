package com.indra87g.tasks;

import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.TextFormat;
import com.indra87g.Main;
import com.indra87g.data.Timer;
import com.indra87g.util.TimerManager;

public class TimerTask extends PluginTask<Main> {
    private final TimerManager timerManager;
    private final Timer timer;

    public TimerTask(Main owner, TimerManager timerManager, Timer timer) {
        super(owner);
        this.timerManager = timerManager;
        this.timer = timer;
    }

    @Override
    public void onRun(int currentTick) {
        switch (timer.getType()) {
            case "msg":
                handleMessage();
                break;
            case "cmd":
                handleCommand();
                break;
        }
        timerManager.removeTimer(timer);
    }

    private void handleMessage() {
        String[] parts = timer.getAction().split(" ", 2);
        String messageType = parts[0];
        String message = parts[1];
        String formattedMessage = TextFormat.colorize('&', message);

        switch (messageType.toLowerCase()) {
            case "broadcast":
                getOwner().getServer().broadcastMessage(formattedMessage);
                break;
            case "private":
                Player player = getOwner().getServer().getPlayer(timer.getCreator());
                if (player != null && player.isOnline()) {
                    player.sendMessage(formattedMessage);
                }
                break;
        }
    }

    private void handleCommand() {
        getOwner().getServer().dispatchCommand(getOwner().getServer().getConsoleSender(), timer.getAction());
    }
}