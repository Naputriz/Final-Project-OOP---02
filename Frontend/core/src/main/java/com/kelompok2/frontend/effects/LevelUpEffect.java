package com.kelompok2.frontend.effects;

import com.kelompok2.frontend.entities.GameCharacter;

public interface LevelUpEffect {
    void apply(GameCharacter character);

    String getName();

    String getDescription();
}
