package com.kelompok2.frontend.events;

import com.kelompok2.frontend.entities.GameCharacter;

public class XpChangedEvent extends GameEvent {
    private final GameCharacter character;
    private final float currentXp;
    private final float maxXp;
    private final int level;

    public XpChangedEvent(GameCharacter character, float currentXp, float maxXp, int level) {
        this.character = character;
        this.currentXp = currentXp;
        this.maxXp = maxXp;
        this.level = level;
    }

    public GameCharacter getCharacter() {
        return character;
    }

    public float getCurrentXp() {
        return currentXp;
    }

    public float getMaxXp() {
        return maxXp;
    }

    public int getLevel() {
        return level;
    }

    public float getXpPercent() {
        return (maxXp > 0) ? currentXp / maxXp : 0;
    }
}
