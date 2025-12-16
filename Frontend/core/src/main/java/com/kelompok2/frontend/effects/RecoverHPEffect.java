package com.kelompok2.frontend.effects;

import com.kelompok2.frontend.entities.GameCharacter;

public class RecoverHPEffect implements LevelUpEffect {

    @Override
    public void apply(GameCharacter character) {
        float healAmount = character.getMaxHp() * 0.2f;
        character.heal(healAmount);
        System.out.println("[RecoverHPEffect] Healed " + healAmount + " HP!");
    }

    @Override
    public String getName() {
        return "Pulihkan HP";
    }

    @Override
    public String getDescription() {
        return "Pulihkan 20% Max HP";
    }
}
