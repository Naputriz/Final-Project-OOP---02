package com.kelompok2.frontend.events;

import com.kelompok2.frontend.entities.GameCharacter;

public class EnemyDamagedEvent extends GameEvent {
    private GameCharacter enemy;
    private float damage;
    private boolean isArts;

    public EnemyDamagedEvent(GameCharacter enemy, float damage, boolean isArts) {
        this.enemy = enemy;
        this.damage = damage;
        this.isArts = isArts;
    }

    public GameCharacter getEnemy() {
        return enemy;
    }

    public float getDamage() {
        return damage;
    }

    public boolean isArts() {
        return isArts;
    }
}
