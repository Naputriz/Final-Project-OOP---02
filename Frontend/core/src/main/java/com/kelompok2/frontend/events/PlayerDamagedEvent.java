package com.kelompok2.frontend.events;

import com.kelompok2.frontend.entities.GameCharacter;

public class PlayerDamagedEvent implements GameEvent {
    private final long timestamp;
    private final GameCharacter player;
    private final float damage;
    private final float newHp;

    public PlayerDamagedEvent(GameCharacter player, float damage, float newHp) {
        this.timestamp = System.currentTimeMillis();
        this.player = player;
        this.damage = damage;
        this.newHp = newHp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public GameCharacter getPlayer() {
        return player;
    }

    public float getDamage() {
        return damage;
    }

    public float getNewHp() {
        return newHp;
    }
}
