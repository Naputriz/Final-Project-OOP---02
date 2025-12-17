package com.kelompok2.frontend.entities;

import com.kelompok2.frontend.skills.Skill;

/**
 * Abstract base class untuk semua boss dalam game.
 * Boss adalah enemy yang lebih kuat dengan stats lebih tinggi,
 * drops ultimate skill saat defeated, dan bisa unlock sebagai playable
 * character.
 */
public abstract class Boss extends GameCharacter {

    protected String bossName; // Nama boss (e.g., "Insania")
    protected String bossTitle; // Gelar boss (e.g., "The Chaos Kaiser")
    protected boolean isDefeated = false;

    // Target untuk AI (player)
    protected GameCharacter target;

    /**
     * Konstruktor untuk Boss.
     * 
     * @param x         Posisi x awal
     * @param y         Posisi y awal
     * @param speed     Kecepatan boss
     * @param hp        HP maksimum boss
     * @param bossName  Nama boss
     * @param bossTitle Gelar/title boss
     * @param target    Target untuk AI (player character)
     */
    public Boss(float x, float y, float speed, float hp, String bossName, String bossTitle, GameCharacter target) {
        super(x, y, speed, hp);
        this.bossName = bossName;
        this.bossTitle = bossTitle;
        this.target = target;
    }

    /**
     * Method abstract untuk mendapatkan nama ultimate skill yang di-drop boss ini
     * 
     * @return Nama ultimate skill (e.g., "Insanity Burst")
     */
    public abstract String getUltimateSkillName();

    /**
     * Method abstract untuk membuat instance ultimate skill saat boss defeated
     * 
     * @return Instance dari Skill yang merupakan ultimate boss ini
     */
    public abstract Skill createUltimateSkill();

    /**
     * Update AI boss. Override di masing-masing boss subclass untuk behavior
     * spesifik.
     * 
     * @param delta Delta time
     */
    public abstract void updateAI(float delta);

    /**
     * Getter untuk nama boss
     * 
     * @return Nama boss
     */
    public String getBossName() {
        return bossName;
    }

    /**
     * Getter untuk title boss (untuk ditampilkan di health bar)
     * 
     * @return Title boss (e.g., "The Chaos Kaiser")
     */
    public String getBossTitle() {
        return bossTitle;
    }

    /**
     * Check apakah boss sudah defeated
     * 
     * @return true jika boss sudah defeated
     */
    public boolean isDefeated() {
        return isDefeated;
    }

    /**
     * Tandai boss sebagai defeated (dipanggil saat HP \u003c= 0)
     */
    public void markDefeated() {
        this.isDefeated = true;
        System.out.println("[Boss] " + bossName + " has been defeated!");
    }

    /**
     * Getter untuk target (player)
     * 
     * @return Target character yang diburu boss
     */
    public GameCharacter getTarget() {
        return target;
    }

    /**
     * Override getAtk() to reduce damage when insane (Mind Fracture debuff)
     * 
     * @return Attack damage (reduced by 50% when insane)
     */
    @Override
    public float getAtk() {
        if (isInsane) {
            return super.getAtk() * 1.5f; // 50% damage reduction when insane
        }
        return super.getAtk();
    }

    /**
     * Update animations only (untuk cinematic sequences).
     * Hanya update stateTime dan animasi, tidak update AI atau combat logic.
     * 
     * @param delta Delta time
     */
    public abstract void updateAnimationsOnly(float delta);

    /**
     * Override update untuk memanggil AI logic
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        // Call AI behavior
        if (!isDead()) {
            updateAI(delta);
        } else if (!isDefeated) {
            // Mark as defeated on death
            markDefeated();
        }
    }
}
