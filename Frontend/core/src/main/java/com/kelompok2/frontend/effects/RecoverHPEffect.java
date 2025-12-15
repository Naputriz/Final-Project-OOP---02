package com.kelompok2.frontend.effects;

import com.kelompok2.frontend.entities.GameCharacter;

/**
 * Effect yang memulihkan HP sebesar 50% dari Max HP.
 */
public class RecoverHPEffect implements LevelUpEffect {

    @Override
    public void apply(GameCharacter character) {
        float healAmount = character.getMaxHp() * 0.5f;
        character.heal(healAmount);
        System.out.println("[RecoverHPEffect] Healed " + healAmount + " HP!");
    }

    @Override
    public String getName() {
        return "Pulihkan HP";
    }

    @Override
    public String getDescription() {
        return "Pulihkan 50% Max HP";
    }
}
