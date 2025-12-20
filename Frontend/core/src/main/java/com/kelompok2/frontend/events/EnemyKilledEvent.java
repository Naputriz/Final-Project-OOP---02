package com.kelompok2.frontend.events;

import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.GameCharacter;

public class EnemyKilledEvent extends GameEvent {
    private final DummyEnemy enemy;
    private final GameCharacter player;
    private final float xpGained;

    public EnemyKilledEvent(DummyEnemy enemy, GameCharacter player, float xpGained) {
        super();
        this.enemy = enemy;
        this.player = player;
        this.xpGained = xpGained;
    }

    public DummyEnemy getEnemy() {
        return enemy;
    }

    public GameCharacter getPlayer() {
        return player;
    }

    public float getXpGained() {
        return xpGained;
    }
}
