package com.kelompok2.frontend.entities;

import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.skills.Skill;

public abstract class Boss extends GameCharacter {

    protected String bossName; // Nama boss (e.g., "Insania")
    protected String bossTitle; // Gelar boss (e.g., "The Chaos Kaiser")
    protected float attackRange;

    // Victory state
    protected boolean defeated = false; // This replaces the original 'isDefeated'

    private long lastMindFractureHitId = -1;
    private long lastPhantomHazeHitId = -1;

    // Target untuk AI (player)
    protected GameCharacter target;

    public Boss(float x, float y, float speed, float hp, String bossName, String bossTitle, GameCharacter target) {
        super(x, y, speed, hp);
        this.bossName = bossName;
        this.bossTitle = bossTitle;
        this.target = target;
        this.isPlayerCharacter = false; // Mark as boss (enemy)
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
        // Commented out to make Frozen Apocalypse useful (freeze persists)
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
            if (isFrozen)
                return; // Skip update if frozen
            if (isStunned)
                return; // Skip update if stunned

            if (isHallucinating) {
                if (target != null) {
                    com.badlogic.gdx.math.Vector2 dir = position.cpy().sub(target.getPosition()).nor();
                    move(dir, delta);
                }
            } else {
                updateAI(delta);
            }
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
        super.makeInsane(1.5f); // Reduced from 5.0f for better balance
        System.out.println("[Boss] " + bossName + " is now insane! ATK/Arts increased by 50%!");
    }

    public boolean wasHitByMindFracture(long activationId) {
        return lastMindFractureHitId == activationId;
    }

    public void markMindFractureHit(long activationId) {
        lastMindFractureHitId = activationId;
    }

    public boolean wasHitByPhantomHaze(long activationId) {
        return lastPhantomHazeHitId == activationId;
    }

    public void markPhantomHazeHit(long activationId) {
        lastPhantomHazeHitId = activationId;
    }
}
