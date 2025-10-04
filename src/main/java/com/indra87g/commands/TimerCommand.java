package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.indra87g.data.Timer;
import com.indra87g.util.ConfirmationManager;
import com.indra87g.util.TimerManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class TimerCommand extends BaseCommand {
    private final TimerManager timerManager;
    private final ConfirmationManager confirmationManager;

    public TimerCommand(String description, TimerManager timerManager, ConfirmationManager confirmationManager) {
        super("timer", description, "/timer <addmsg|addcmd|agenda>", "waffle.timer");
        this.timerManager = timerManager;
        this.confirmationManager = confirmationManager;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(TextFormat.RED + "Usage: " + this.getUsage());
            return false;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "addmsg":
                handleAddMsg(player, args);
                break;
            case "addcmd":
                handleAddCmd(player, args);
                break;
            case "agenda":
                handleAgenda(player);
                break;
            default:
                player.sendMessage(TextFormat.RED + "Usage: " + this.getUsage());
                break;
        }
        return true;
    }


    private void handleAddMsg(Player player, String[] args) {
        if (!player.hasPermission("waffle.timer.addmsg")) {
            player.sendMessage(TextFormat.RED + "You do not have permission to use this command.");
            return;
        }
        if (args.length < 4) {
            player.sendMessage(TextFormat.RED + "Usage: /timer addmsg <seconds> <type> <message>");
            return;
        }
        try {
            long seconds = Long.parseLong(args[1]);
            String type = args[2].toLowerCase();
            if (!type.equals("broadcast") && !type.equals("private")) {
                player.sendMessage(TextFormat.RED + "Invalid message type. Use 'broadcast' or 'private'.");
                return;
            }
            StringJoiner message = new StringJoiner(" ");
            for (int i = 3; i < args.length; i++) {
                message.add(args[i]);
            }
            String fullMessage = message.toString();

            String confirmationMessage = String.format(
                    "Are you sure you want to schedule a %s message in %d seconds with the message: \"%s\"? Type 'confirm' to proceed or 'cancel' to abort.",
                    type, seconds, fullMessage
            );

            Runnable onConfirm = () -> {
                long executionTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds);
                String action = type + " " + fullMessage;
                Timer timer = new Timer("msg", action, player.getName(), executionTime);
                timerManager.addTimer(timer);
                player.sendMessage(TextFormat.GREEN + "Message timer scheduled successfully!");
            };

            confirmationManager.requestConfirmation(player, confirmationMessage, onConfirm);

        } catch (NumberFormatException e) {
            player.sendMessage(TextFormat.RED + "The seconds must be a valid number.");
        }
    }

    private void handleAddCmd(Player player, String[] args) {
        if (!player.hasPermission("waffle.timer.addcmd")) {
            player.sendMessage(TextFormat.RED + "You do not have permission to use this command.");
            return;
        }
        if (args.length < 3) {
            player.sendMessage(TextFormat.RED + "Usage: /timer addcmd <seconds> <command>");
            return;
        }
        try {
            long seconds = Long.parseLong(args[1]);
            StringJoiner command = new StringJoiner(" ");
            for (int i = 2; i < args.length; i++) {
                command.add(args[i]);
            }
            String fullCommand = command.toString();

            String confirmationMessage = String.format(
                    "Are you sure you want to schedule the command \"/%s\" in %d seconds? Type 'confirm' to proceed or 'cancel' to abort.",
                    fullCommand, seconds
            );

            Runnable onConfirm = () -> {
                long executionTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds);
                Timer timer = new Timer("cmd", fullCommand, player.getName(), executionTime);
                timerManager.addTimer(timer);
                player.sendMessage(TextFormat.GREEN + "Command timer scheduled successfully!");
            };

            confirmationManager.requestConfirmation(player, confirmationMessage, onConfirm);

        } catch (NumberFormatException e) {
            player.sendMessage(TextFormat.RED + "The seconds must be a valid number.");
        }
    }

    private void handleAgenda(Player player) {
        if (!player.hasPermission("waffle.timer.agenda")) {
            player.sendMessage(TextFormat.RED + "You do not have permission to use this command.");
            return;
        }
        List<Timer> timers = timerManager.getTimers();
        if (timers.isEmpty()) {
            player.sendMessage(TextFormat.YELLOW + "There are no scheduled timers.");
            return;
        }

        player.sendMessage(TextFormat.GOLD + "--- Timer Agenda ---");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Timer timer : timers) {
            LocalDateTime triggerTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timer.getExecutionTime()), ZoneId.systemDefault());
            String info = String.format(
                    "By: %s | Type: %s | At: %s | Action: %s",
                    timer.getCreator(), timer.getType(), triggerTime.format(formatter), timer.getAction()
            );
            player.sendMessage(TextFormat.AQUA + "- " + info);
        }
        player.sendMessage(TextFormat.GOLD + "--------------------");
    }

}