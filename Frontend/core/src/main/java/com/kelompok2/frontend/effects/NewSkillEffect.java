package com.kelompok2.frontend.effects;

import com.kelompok2.frontend.entities.GameCharacter;

/**
 * Effect untuk menambah skill baru (Q slot).
 * Saat ini hanya placeholder karena secondary skill system belum
 * diimplementasi.
 */
public class NewSkillEffect implements LevelUpEffect {

    @Override
    public void apply(GameCharacter character) {
        // TODO: Implement ketika secondary skill system (Q key) sudah ada
        System.out.println("[NewSkillEffect] Coming Soon! Secondary skill system not yet implemented.");
    }

    @Override
    public String getName() {
        return "Skill Baru";
    }

    @Override
    public String getDescription() {
        return "Skill baru (Segera!)";
    }
}
