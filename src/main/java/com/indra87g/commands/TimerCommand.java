package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.indra87g.util.ConfirmationManager;
import com.indra87g.util.Timer;
import com.indra87g.util.TimerManager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimerCommand extends BaseCommand {
    private final TimerManager timerManager;
    private final ConfirmationManager confirmationManager;

    public TimerCommand(String description, TimerManager timerManager, ConfirmationManager confirmationManager) {
        super("timer", description, "/timer <addmsg|addcmd|remove|agenda>", "waffle.timer");
        this.timerManager = timerManager;
        this.confirmationManager = confirmationManager;
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        if (args.length == 0) {
            player.sendMessage(TextFormat.YELLOW + "Usage: " + getUsage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "addmsg":
                return handleAddTimer(player, args, "msg");
            case "addcmd":
                if (!player.hasPermission("waffle.timer.admin")) {
                    player.sendMessage(TextFormat.RED + "You do not have permission to use this subcommand.");
                    return false;
                }
                return handleAddTimer(player, args, "cmd");
            case "remove":
                return handleRemoveTimer(player, args);
            case "agenda":
                return handleAgenda(player);
            default:
                player.sendMessage(TextFormat.RED + "Unknown subcommand. Usage: " + getUsage());
                return false;
        }
    }

    private boolean handleAddTimer(Player player, String[] args, String type) {
        if (args.length < 3) {
            player.sendMessage(TextFormat.RED + "Usage: /timer " + args[0] + " <seconds> <" + (type.equals("msg") ? "message" : "command") + ">");
            return false;
        }

        int duration;
        try {
            duration = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(TextFormat.RED + "Invalid duration specified.");
            return false;
        }

        String messageOrCommand = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        confirmationManager.requestConfirmation(player, () -> {
            timerManager.addTimer(type, duration, messageOrCommand, player);
        });

        return true;
    }

    private boolean handleRemoveTimer(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(TextFormat.RED + "Usage: /timer remove <timer_id>");
            return false;
        }
        String timerId = args[1];
        if (timerManager.removeTimer(timerId)) {
            player.sendMessage(TextFormat.GREEN + "Timer with ID " + timerId + " has been removed.");
        } else {
            player.sendMessage(TextFormat.RED + "Timer with ID " + timerId + " not found.");
        }
        return true;
    }

    private boolean handleAgenda(Player player) {
        List<Timer> timers = timerManager.getUpcomingTimers();
        if (timers.isEmpty()) {
            player.sendMessage(TextFormat.YELLOW + "No upcoming timers.");
            return true;
        }

        player.sendMessage(TextFormat.AQUA + "--- Upcoming Timers ---");
        for (Timer timer : timers) {
            long remainingSeconds = timer.getDuration() - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - timer.getCreatedAt());
            player.sendMessage(TextFormat.GRAY + "- ID: " + timer.getId() + " | Type: " + timer.getType() + " | Remaining: " + remainingSeconds + "s");
        }
        return true;
    }
}