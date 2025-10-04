package com.indra87g.util;

import cn.nukkit.utils.Config;
import com.indra87g.Main;
import com.indra87g.data.Timer;
import com.indra87g.tasks.TimerTask;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TimerManager {
    private final Main plugin;
    private final Config timerConfig;
    private final List<Timer> timers;

    public TimerManager(Main plugin) {
        this.plugin = plugin;
        this.timerConfig = new Config(new File(plugin.getDataFolder(), "timers.yml"), Config.YAML);
        this.timers = new ArrayList<>();
        loadTimers();
    }

    private void loadTimers() {
        List<LinkedHashMap<String, Object>> timerMaps = (List<LinkedHashMap<String, Object>>) timerConfig.getList("timers");
        if (timerMaps != null) {
            for (LinkedHashMap<String, Object> timerMap : timerMaps) {
                Timer timer = Timer.fromMap(timerMap);
                if (timer.getExecutionTime() > System.currentTimeMillis()) {
                    timers.add(timer);
                    scheduleTimer(timer);
                }
            }
            saveConfig();
        }
    }

    public void addTimer(Timer timer) {
        timers.add(timer);
        scheduleTimer(timer);
        saveConfig();
    }

    private void scheduleTimer(Timer timer) {
        long delay = (timer.getExecutionTime() - System.currentTimeMillis()) / 50; // Nukkit scheduler uses ticks (50ms)
        if (delay > 0) {
            plugin.getServer().getScheduler().scheduleDelayedTask(new TimerTask(plugin, this, timer), (int) delay);
        } else {
            // If for some reason the time is already past, execute immediately
            new TimerTask(plugin, this, timer).run();
        }
    }

    public void removeTimer(Timer timer) {
        timers.remove(timer);
        saveConfig();
    }

    private void saveConfig() {
        List<LinkedHashMap<String, Object>> timerMaps = timers.stream().map(Timer::toMap).collect(Collectors.toList());
        timerConfig.set("timers", timerMaps);
        timerConfig.save();
    }

    public List<Timer> getTimers() {
        return new ArrayList<>(timers);
    }
}