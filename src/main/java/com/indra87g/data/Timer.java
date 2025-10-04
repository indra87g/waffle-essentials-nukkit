package com.indra87g.data;

import java.util.LinkedHashMap;

public class Timer {
    private final String type;
    private final String action;
    private final String creator;
    private final long executionTime;

    public Timer(String type, String action, String creator, long executionTime) {
        this.type = type;
        this.action = action;
        this.creator = creator;
        this.executionTime = executionTime;
    }

    public String getType() {
        return type;
    }

    public String getAction() {
        return action;
    }

    public String getCreator() {
        return creator;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public LinkedHashMap<String, Object> toMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("type", type);
        map.put("action", action);
        map.put("creator", creator);
        map.put("executionTime", executionTime);
        return map;
    }

    public static Timer fromMap(LinkedHashMap<String, Object> map) {
        String type = (String) map.get("type");
        String action = (String) map.get("action");
        String creator = (String) map.get("creator");
        long executionTime = ((Number) map.get("executionTime")).longValue();
        return new Timer(type, action, creator, executionTime);
    }
}