package com.indra87g.util;

import java.util.UUID;

public class Timer {
    private final String id;
    private final String type;
    private final int duration;
    private final long createdAt;
    private final String messageOrCommand;
    private final UUID playerUUID;

    public Timer(String id, String type, int duration, String messageOrCommand, UUID playerUUID) {
        this.id = id;
        this.type = type;
        this.duration = duration;
        this.createdAt = System.currentTimeMillis();
        this.messageOrCommand = messageOrCommand;
        this.playerUUID = playerUUID;
    }

    public Timer(String id, String type, int duration, long createdAt, String messageOrCommand, UUID playerUUID) {
        this.id = id;
        this.type = type;
        this.duration = duration;
        this.createdAt = createdAt;
        this.messageOrCommand = messageOrCommand;
        this.playerUUID = playerUUID;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getMessageOrCommand() {
        return messageOrCommand;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - createdAt) / 1000 >= duration;
    }
}