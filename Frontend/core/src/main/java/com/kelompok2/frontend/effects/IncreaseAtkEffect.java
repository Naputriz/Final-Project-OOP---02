package com.kelompok2.frontend.effects;

import com.kelompok2.frontend.entities.GameCharacter;

public class IncreaseAtkEffect implements LevelUpEffect {

    @Override
    public void apply(GameCharacter character) {
        float newAtk = character.getAtk() * 1.15f;
        character.setAtk(newAtk);
        System.out.println("[IncreaseAtkEffect] ATK increased to " + newAtk);
    }

    @Override
    public String getName() {
        return "Tingkatkan ATK";
    }

    @Override
    public String getDescription() {
        return "+15% ATK";
    }
}
