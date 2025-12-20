package com.kelompok2.frontend.events;

import com.kelompok2.frontend.entities.GameCharacter;

public class CooldownChangedEvent extends GameEvent {
    public enum SkillType {
        INNATE,
        SECONDARY,
        ULTIMATE
    }

    private final GameCharacter character;
    private final SkillType skillType;
    private final float remainingTime;
    private final float totalCooldown;

    public CooldownChangedEvent(GameCharacter character, SkillType skillType, float remainingTime,
            float totalCooldown) {
        this.character = character;
        this.skillType = skillType;
        this.remainingTime = remainingTime;
        this.totalCooldown = totalCooldown;
    }

    public GameCharacter getCharacter() {
        return character;
    }

    public SkillType getSkillType() {
        return skillType;
    }

    public float getRemainingTime() {
        return remainingTime;
    }

    public float getTotalCooldown() {
        return totalCooldown;
    }

    public float getCooldownPercent() {
        // Return percentage REMAINING
        if (totalCooldown <= 0)
            return 0;
        return remainingTime / totalCooldown;
    }
}
