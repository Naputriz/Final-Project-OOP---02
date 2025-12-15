package com.kelompok2.frontend.effects;

import com.kelompok2.frontend.entities.GameCharacter;

/**
 * Command Pattern interface untuk level-up effects.
 * Setiap effect mengimplementasikan interface ini untuk menerapkan efek ke
 * character.
 */
public interface LevelUpEffect {
    /**
     * Menerapkan effect ke character yang dipilih.
     * 
     * @param character Character yang akan menerima effect
     */
    void apply(GameCharacter character);

    /**
     * Mendapatkan nama effect untuk ditampilkan di UI.
     * 
     * @return Nama effect
     */
    String getName();

    /**
     * Mendapatkan deskripsi lengkap effect untuk ditampilkan di UI.
     * 
     * @return Deskripsi effect
     */
    String getDescription();
}
