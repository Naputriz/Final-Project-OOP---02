package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public abstract class BaseEnemy extends GameCharacter {
    protected GameCharacter target;

    // Status effects
    protected boolean frozen = false;
    protected float freezeTimer = 0f;
    protected static final float FREEZE_DURATION = 3.0f;
    protected com.badlogic.gdx.utils.Array<Projectile> projectileList;

    public void setProjectileList(com.badlogic.gdx.utils.Array<Projectile> projectileList) {
        this.projectileList = projectileList;
    }

    // Insanity behavior
    protected Vector2 randomDirection = new Vector2();
    protected float directionChangeTimer = 0f;

    protected long lastMindFractureHitId = -1;
    protected long lastPhantomHazeHitId = -1;

    // Base stats for scaling
    protected float baseMaxHp;
    protected float baseAtk;
    protected float baseArts;

    public BaseEnemy(float x, float y, float speed, float maxHp, GameCharacter target) {
        super(x, y, speed, maxHp);
        this.target = target;
        this.attackCooldown = 1.0f; // Default cooldown
        this.isPlayerCharacter = false; // Mark as enemy

        // Initialize base stats
        this.baseMaxHp = maxHp;
        this.baseAtk = this.atk; // Initial atk from super (GameCharacter default is 10f)
        this.baseArts = this.arts; // Initial arts from super
    }

    public abstract void updateBehavior(float delta);

    public abstract float getXpReward();

    public void scaleStats(int level) {
        if (level <= 1)
            return;

        // Basic linear scaling: 5% per level (Reduced from 10%)
        float scaleFactor = 1.0f + 0.05f * (level - 1);

        // Always scale from BASE values to prevent infinite compounding
        this.maxHp = this.baseMaxHp * scaleFactor;
        this.hp = this.maxHp; // Heal to full new max HP
        this.atk = this.baseAtk * scaleFactor;
        this.arts = this.baseArts * scaleFactor;

        // Cap scaling to prevent absurdity at high levels if needed
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update freeze
        if (frozen) {
            freezeTimer -= delta;
            if (freezeTimer <= 0) {
                frozen = false;
                freezeTimer = 0;
            }
            return; // Skip movement/behavior if frozen
        }

        // Update hallucination
        if (isHallucinating) {
            if (target != null) {
                // Move AWAY from target
                Vector2 awayDir = position.cpy().sub(target.getPosition()).nor();
                move(awayDir, delta);
            }
            return;
        }

        // Update insanity
        if (isInsane) {
            directionChangeTimer -= delta;
            if (directionChangeTimer <= 0) {
                generateRandomDirection();
                directionChangeTimer = 0.5f;
            }
            move(randomDirection, delta);
            return;
        }

        // If not disabled, run specific behavior
        if (!isBeingPulled) {
            updateBehavior(delta);
        }
    }

    @Override
    protected Color getRenderColor() {
        if (frozen)
            return new Color(0.5f, 0.8f, 1f, 0.7f); // Override for BaseEnemy specific frozen field
        return super.getRenderColor();
    }

    protected void generateRandomDirection() {
        float angle = (float) (Math.random() * Math.PI * 2);
        randomDirection.set((float) Math.cos(angle), (float) Math.sin(angle)).nor();
    }

    public void reset(float x, float y, GameCharacter newTarget) {
        this.position.set(x, y);
        this.bounds.setPosition(x, y);
        this.target = newTarget;

        // Restore stats to BASE before scaling is re-applied
        this.maxHp = this.baseMaxHp;
        this.hp = this.maxHp;
        this.atk = this.baseAtk;
        this.arts = this.baseArts;

        this.frozen = false;
        this.freezeTimer = 0;
        super.clearInsanity();
        super.clearStun();
        super.clearSlow();
        super.clearFreeze();
        super.clearHallucination();
        this.isMarked = false;
    }

    // --- Getters / Setters ---

    public void freeze() {
        freeze(FREEZE_DURATION);
    }

    public void freeze(float duration) {
        this.frozen = true;
        this.freezeTimer = duration;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
        if (frozen) {
            this.freezeTimer = FREEZE_DURATION;
        } else {
            this.freezeTimer = 0;
        }
    }

    public boolean isFrozen() {
        return frozen;
    }

    public boolean wasHitByMindFracture(long activationId) {
        return lastMindFractureHitId == activationId;
    }

    public void markMindFractureHit(long activationId) {
        this.lastMindFractureHitId = activationId;
    }

    public boolean wasHitByPhantomHaze(long activationId) {
        return lastPhantomHazeHitId == activationId;
    }

    public void markPhantomHazeHit(long activationId) {
        this.lastPhantomHazeHitId = activationId;
    }

    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        if (frozen) {
            frozen = false;
            freezeTimer = 0;
        }
    }

    @Override
    public float getAtk() {
        return isInsane ? super.getAtk() * 1.5f : super.getAtk();
    }

    @Override
    public float getArts() {
        return isInsane ? super.getArts() * 1.5f : super.getArts();
    }

    // --- Default Implementations ---
    @Override
    public void performInnateSkill() {
    }

    @Override
    public float getInnateSkillTimer() {
        return 0;
    }

    @Override
    public float getInnateSkillCooldown() {
        return 0;
    }

    @Override
    public String getAttackAnimationType() {
        return "slash";
    }
}
