package com.kelompok2.frontend.events;

import com.kelompok2.frontend.entities.GameCharacter;

public class HealthChangedEvent extends GameEvent {
    private final GameCharacter character;
    private final float currentHp;
    private final float maxHp;

    public HealthChangedEvent(GameCharacter character, float currentHp, float maxHp) {
        this.character = character;
        this.currentHp = currentHp;
        this.maxHp = maxHp;
    }

    public GameCharacter getCharacter() {
        return character;
    }

    public float getCurrentHp() {
        return currentHp;
    }

    public float getMaxHp() {
        return maxHp;
    }

    public float getHpPercent() {
        return (maxHp > 0) ? currentHp / maxHp : 0;
    }
}
