package com.kelompok2.frontend.events;

public class UltimateActivatedEvent extends GameEvent {
    private final String ultimateName;

    public UltimateActivatedEvent(String ultimateName) {
        super();
        this.ultimateName = ultimateName;
    }

    public String getUltimateName() {
        return ultimateName;
    }
}
