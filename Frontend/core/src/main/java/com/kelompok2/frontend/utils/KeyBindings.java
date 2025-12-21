package com.kelompok2.frontend.utils;

import com.badlogic.gdx.Input;

public class KeyBindings {
    // Default Keys
    public int moveUp = Input.Keys.W;
    public int moveDown = Input.Keys.S;
    public int moveLeft = Input.Keys.A;
    public int moveRight = Input.Keys.D;

    public int innateSkill = Input.Keys.E;
    public int secondarySkill = Input.Keys.Q;
    public int ultimateSkill = Input.Keys.R;

    public int pause = Input.Keys.ESCAPE;

    public KeyBindings() {}

    // Reset ke default
    public void resetToDefault() {
        moveUp = Input.Keys.W;
        moveDown = Input.Keys.S;
        moveLeft = Input.Keys.A;
        moveRight = Input.Keys.D;
        innateSkill = Input.Keys.E;
        secondarySkill = Input.Keys.Q;
        ultimateSkill = Input.Keys.R;
        pause = Input.Keys.ESCAPE;
    }
}
