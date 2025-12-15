package com.kelompok2.frontend.effects;

import com.kelompok2.frontend.entities.GameCharacter;

/**
 * Effect yang meningkatkan Max HP sebesar 15%.
 */
public class IncreaseMaxHPEffect implements LevelUpEffect {

    @Override
    public void apply(GameCharacter character) {
        float newMaxHp = character.getMaxHp() * 1.15f;
        character.setMaxHp(newMaxHp);
        System.out.println("[IncreaseMaxHPEffect] Max HP increased to " + newMaxHp);
    }

    @Override
    public String getName() {
        return "Tingkatkan Max HP";
    }

    @Override
    public String getDescription() {
        return "+15% Max HP";
    }
}
