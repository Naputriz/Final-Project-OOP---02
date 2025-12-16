package com.kelompok2.frontend.effects;

import com.kelompok2.frontend.entities.GameCharacter;

public class IncreaseArtsEffect implements LevelUpEffect {

    @Override
    public void apply(GameCharacter character) {
        float newArts = character.getArts() * 1.15f;
        character.setArts(newArts);
        System.out.println("[IncreaseArtsEffect] Arts increased to " + newArts);
    }

    @Override
    public String getName() {
        return "Tingkatkan Arts";
    }

    @Override
    public String getDescription() {
        return "+15% Arts";
    }
}
