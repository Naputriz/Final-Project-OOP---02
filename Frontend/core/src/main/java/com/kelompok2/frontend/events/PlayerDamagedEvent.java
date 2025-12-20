package com.kelompok2.frontend.events;

import com.kelompok2.frontend.entities.GameCharacter;

public class PlayerDamagedEvent extends GameEvent {
    private final GameCharacter player;
    private final float damage;
    private final float newHp;

    public PlayerDamagedEvent(GameCharacter player, float damage, float newHp) {
        super();
        this.player = player;
        this.damage = damage;
        this.newHp = newHp;
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
