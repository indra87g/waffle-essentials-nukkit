package com.indra87g.util;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.indra87g.Main;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class TimerManager {
    private final Main plugin;
    private final Config timerConfig;
    private final Map<String, Timer> activeTimers = new ConcurrentHashMap<>();

    public TimerManager(Main plugin) {
        this.plugin = plugin;
        File timerFile = new File(plugin.getDataFolder(), "timers.yml");
        if (!timerFile.exists()) {
            plugin.saveResource("timers.yml", false);
        }
        this.timerConfig = new Config(timerFile, Config.YAML);
        loadTimers();
        startScheduler();
    }

    private void loadTimers() {
        if (timerConfig.get("timers") instanceof Map) {
            Map<String, Map<String, Object>> timersMap = (Map<String, Map<String, Object>>) timerConfig.get("timers");
            for (Map.Entry<String, Map<String, Object>> entry : timersMap.entrySet()) {
                String id = entry.getKey();
                Map<String, Object> timerData = entry.getValue();
                String type = (String) timerData.get("type");
                int duration = (int) timerData.get("duration");
                long createdAt = ((Number) timerData.get("createdAt")).longValue();
                String messageOrCommand = (String) timerData.get("messageOrCommand");
                UUID playerUUID = UUID.fromString((String) timerData.get("playerUUID"));
                Timer timer = new Timer(id, type, duration, createdAt, messageOrCommand, playerUUID);
                if (!timer.isExpired()) {
                    activeTimers.put(id, timer);
                }
            }
        }
    }

    private void saveTimers() {
        Map<String, Object> timersToSave = new LinkedHashMap<>();
        for (Timer timer : activeTimers.values()) {
            Map<String, Object> timerData = new LinkedHashMap<>();
            timerData.put("type", timer.getType());
            timerData.put("duration", timer.getDuration());
            timerData.put("createdAt", timer.getCreatedAt());
            timerData.put("messageOrCommand", timer.getMessageOrCommand());
            timerData.put("playerUUID", timer.getPlayerUUID().toString());
            timersToSave.put(timer.getId(), timerData);
        }
        timerConfig.set("timers", timersToSave);
        timerConfig.save();
    }

    public String addTimer(String type, int duration, String messageOrCommand, Player player) {
        String id = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
        Timer timer = new Timer(id, type, duration, messageOrCommand, player.getUniqueId());
        activeTimers.put(id, timer);
        saveTimers();
        player.sendMessage(TextFormat.GREEN + "Timer " + type + " with id " + id + " has been created! Please wait for " + duration + " seconds.");
        return id;
    }

    public boolean removeTimer(String id) {
        if (activeTimers.containsKey(id)) {
            activeTimers.remove(id);
            saveTimers();
            return true;
        }
        return false;
    }

    public List<Timer> getUpcomingTimers() {
        return new ArrayList<>(activeTimers.values());
    }

    private void startScheduler() {
        new NukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<String, Timer>> iterator = activeTimers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Timer> entry = iterator.next();
                    Timer timer = entry.getValue();
                    if (timer.isExpired()) {
                        executeTimer(timer);
                        iterator.remove();
                        saveTimers();
                    }
                }
            }
        }.runTaskTimer(plugin, 20, 20); // Run every second
    }

    private void executeTimer(Timer timer) {
        Server server = plugin.getServer();
        switch (timer.getType()) {
            case "msg":
                server.broadcastMessage(TextFormat.colorize('&', timer.getMessageOrCommand()));
                break;
            case "cmd":
                server.dispatchCommand(server.getConsoleSender(), timer.getMessageOrCommand());
                break;
        }
    }
}