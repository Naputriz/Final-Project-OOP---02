package com.kelompok2.frontend.states;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.kelompok2.frontend.entities.GameCharacter;

/**
 * State Pattern interface untuk animation states.
 * Digunakan untuk mengelola state animasi karakter (Idle, Running, dll).
 */
public interface AnimationState {
    /**
     * Dipanggil saat state baru dimasuki.
     * 
     * @param character Character yang memasuki state ini
     */
    void enter(GameCharacter character);

    /**
     * Update state setiap frame.
     * 
     * @param character Character yang sedang dalam state ini
     * @param delta     Delta time
     */
    void update(GameCharacter character, float delta);

    /**
     * Mendapatkan frame animasi saat ini.
     * 
     * @param stateTime Waktu total dalam state ini
     * @return TextureRegion frame saat ini
     */
    TextureRegion getCurrentFrame(float stateTime);

    /**
     * Dipanggil saat state akan ditinggalkan.
     * 
     * @param character Character yang meninggalkan state ini
     */
    void exit(GameCharacter character);
}
