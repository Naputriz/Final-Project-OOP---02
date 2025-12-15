package com.kelompok2.frontend.effects;

import com.kelompok2.frontend.entities.GameCharacter;

/**
 * Effect yang meningkatkan DEF sebesar 15%.
 */
public class IncreaseDefenseEffect implements LevelUpEffect {

    @Override
    public void apply(GameCharacter character) {
        float newDef = character.getDef() * 1.15f;
        character.setDef(newDef);
        System.out.println("[IncreaseDefenseEffect] DEF increased to " + newDef);
    }

    @Override
    public String getName() {
        return "Tingkatkan DEF";
    }

    @Override
    public String getDescription() {
        return "+15% DEF";
    }
}
