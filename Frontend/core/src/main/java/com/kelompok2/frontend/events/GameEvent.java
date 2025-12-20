package com.kelompok2.frontend.events;

public abstract class GameEvent {
    private final long timestamp;

    public GameEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
