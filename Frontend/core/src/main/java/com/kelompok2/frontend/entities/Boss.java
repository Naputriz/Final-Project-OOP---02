package com.kelompok2.frontend.entities;

import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.skills.Skill;

public abstract class Boss extends GameCharacter {

    protected String bossName; // Nama boss (e.g., "Insania")
    protected String bossTitle; // Gelar boss (e.g., "The Chaos Kaiser")
    protected float attackRange;

    // Victory state
    protected boolean defeated = false; // This replaces the original 'isDefeated'

    // ✅ FIX: Track Mind Fracture hits to prevent spam
    private long lastMindFractureHitId = -1;

    // Target untuk AI (player)
    protected GameCharacter target;

    public Boss(float x, float y, float speed, float hp, String bossName, String bossTitle, GameCharacter target) {
        super(x, y, speed, hp);
        this.bossName = bossName;
        this.bossTitle = bossTitle;
        this.target = target;
    }

    public abstract String getUltimateSkillName();
    public abstract Skill createUltimateSkill();
    public abstract void updateAI(float delta);
    public String getBossName() {
        return bossName;
    }
    public String getBossTitle() {
        return bossTitle;
    }
    public boolean isDefeated() {
        return defeated;
    }
    void markDefeated() {
        this.defeated = true;
        System.out.println("[Boss] " + bossName + " has been defeated!");
    }
    public GameCharacter getTarget() {
        return target;
    }
    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);

        // ✅ FIX: Break freeze when hit
        if (isFrozen) {
            isFrozen = false;
            System.out.println("[Boss] " + bossName + " broke free from freeze!");
        }
    }

    @Override
    public float getAtk() {
        if (isInsane) {
            return super.getAtk() * 1.5f; // 50% damage increase when insane
        }
        return super.getAtk();
    }

    @Override
    public float getArts() {
        if (isInsane) {
            return super.getArts() * 1.5f; // 50% arts increase when insane
        }
        return super.getArts();
    }

    public abstract void updateAnimationsOnly(float delta);

    public abstract void setAttackArrays(Array<MeleeAttack> meleeAttacks, Array<Projectile> projectiles);

    @Override
    public void update(float delta) {
        super.update(delta);

        // Call AI behavior
        if (!isDead()) {
            updateAI(delta);
        } else if (!defeated) {
            // Mark as defeated on death
            markDefeated();
        }
    }

    public void freeze() {
        // Use GameCharacter's freeze method with duration
        super.freeze(5.0f); // 5 seconds freeze
        System.out.println("[Boss] " + bossName + " has been frozen for 5 seconds!");
    }

    public void applyInsanity() {
        // Use GameCharacter's makeInsane method with duration
        super.makeInsane(5.0f); // 5 seconds insanity
        System.out.println("[Boss] " + bossName + " is now insane! ATK/Arts increased by 50%!");
    }

    public boolean wasHitByMindFracture(long activationId) {
        return lastMindFractureHitId == activationId;
    }
    public void markMindFractureHit(long activationId) {
        lastMindFractureHitId = activationId;
    }
}
