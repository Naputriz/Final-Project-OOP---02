package com.kelompok2.frontend.states;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.kelompok2.frontend.entities.GameCharacter;

public interface AnimationState {
    void enter(GameCharacter character);

    void update(GameCharacter character, float delta);

    TextureRegion getCurrentFrame(float stateTime);

    void exit(GameCharacter character);
}
