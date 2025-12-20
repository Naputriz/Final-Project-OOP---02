package com.kelompok2.frontend.effects;

import com.kelompok2.frontend.entities.GameCharacter;

public class IncreaseSpeedEffect implements LevelUpEffect {

    @Override
    public void apply(GameCharacter character) {
        float oldSpeed = character.getSpeed();
        // Increase speed by 10%
        float newSpeed = oldSpeed * 1.10f;
        character.setSpeed(newSpeed);
        System.out.println("Applied IncreaseSpeedEffect: " + oldSpeed + " -> " + newSpeed);
    }

    @Override
    public String getName() {
        return "Increase Speed";
    }

    @Override
    public String getDescription() {
        return "Increases movement speed by 10%.";
    }
}
