package com.kelompok2.frontend.events;

public class UltimateActivatedEvent implements GameEvent {
    private final long timestamp;
    private final String ultimateName;

    public UltimateActivatedEvent(String ultimateName) {
        this.timestamp = System.currentTimeMillis();
        this.ultimateName = ultimateName;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public String getUltimateName() {
        return ultimateName;
    }
}
