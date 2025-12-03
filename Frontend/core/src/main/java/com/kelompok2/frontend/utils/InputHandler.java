package com.kelompok2.frontend.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.kelompok2.frontend.entities.GameCharacter;

public class InputHandler {
    private GameCharacter character;

    public InputHandler(GameCharacter character) {
        this.character = character;
    }

    public void update(float delta) {
        float moveX = 0;
        float moveY = 0;

        // Logika Input (WASD)
        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX = 1;

        // Skill Input
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            character.performInnateSkill();
        }

        // Apply movement (normalize biar diagonal gak ngebut)
        if (moveX != 0 || moveY != 0) {
            // Normalisasi vektor diagonal (Pythagoras)
            double length = Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= length;
            moveY /= length;

            character.move(moveX * delta, moveY * delta);
        }
    }
}
