package com.kelompok2.frontend.events;

@FunctionalInterface
public interface GameEventListener<T extends GameEvent> {
    void onEvent(T event);
}
