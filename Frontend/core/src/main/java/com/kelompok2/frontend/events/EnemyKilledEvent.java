package com.kelompok2.frontend.events;

import com.kelompok2.frontend.entities.BaseEnemy;
import com.kelompok2.frontend.entities.GameCharacter;

public class EnemyKilledEvent extends GameEvent {
    private final BaseEnemy enemy;
    private final GameCharacter player;
    private final float xpGained;

    public EnemyKilledEvent(BaseEnemy enemy, GameCharacter player, float xpGained) {
        super();
        this.enemy = enemy;
        this.player = player;
        this.xpGained = xpGained;
    }

    public BaseEnemy getEnemy() {
        return enemy;
    }

    public GameCharacter getPlayer() {
        return player;
    }

    public float getXpGained() {
        return xpGained;
    }
}
