package com.kelompok2.frontend.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kelompok2.frontend.events.GameEvent;
import com.kelompok2.frontend.events.GameEventListener;

/**
 * Event bus untuk publish dan subscribe ke game events.
 * Implements Singleton Pattern dan Observer Pattern.
 * 
 * Decouples game logic dari UI - game entities publish events,
 * UI systems subscribe dan react.
 */
public class GameEventManager {
    private static GameEventManager instance;

    // Map event type ke list of listeners
    private Map<Class<? extends GameEvent>, List<GameEventListener>> listeners;

    private GameEventManager() {
        listeners = new HashMap<>();
    }

    public static GameEventManager getInstance() {
        if (instance == null) {
            instance = new GameEventManager();
        }
        return instance;
    }

    /**
     * Subscribe ke event type tertentu
     * 
     * @param eventType Class dari event yang mau di-subscribe
     * @param listener  Listener yang akan dipanggil ketika event terjadi
     */
    public <T extends GameEvent> void subscribe(Class<T> eventType, GameEventListener<T> listener) {
        if (!listeners.containsKey(eventType)) {
            listeners.put(eventType, new ArrayList<>());
        }
        listeners.get(eventType).add(listener);
    }

    /**
     * Unsubscribe dari event type
     * 
     * @param eventType Class dari event yang mau di-unsubscribe
     * @param listener  Listener yang mau di-remove
     */
    public <T extends GameEvent> void unsubscribe(Class<T> eventType, GameEventListener<T> listener) {
        if (listeners.containsKey(eventType)) {
            listeners.get(eventType).remove(listener);
        }
    }

    /**
     * Publish event ke semua subscribers
     * 
     * @param event Event yang mau di-publish
     */
    @SuppressWarnings("unchecked")
    public <T extends GameEvent> void publish(T event) {
        Class<?> eventType = event.getClass();
        if (listeners.containsKey(eventType)) {
            // Create copy to avoid ConcurrentModificationException if listener unsubscribes
            List<GameEventListener> eventListeners = new ArrayList<>(listeners.get(eventType));
            for (GameEventListener listener : eventListeners) {
                listener.onEvent(event);
            }
        }
    }

    /**
     * Clear all listeners (untuk cleanup/testing)
     */
    public void clearAll() {
        listeners.clear();
    }

    /**
     * Reset instance (untuk testing)
     */
    public static void resetInstance() {
        instance = null;
    }
}
