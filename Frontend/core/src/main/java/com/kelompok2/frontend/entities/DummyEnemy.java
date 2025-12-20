package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class DummyEnemy extends GameCharacter {
    private GameCharacter target;

    // Freeze status effect (untuk Glacial Breath)
    private boolean frozen = false;
    private float freezeTimer = 0f;
    private static final float FREEZE_DURATION = 3.0f; // 3 detik freeze

    // Random movement untuk Insanity (uses parent's isInsane from GameCharacter)
    private Vector2 randomDirection = new Vector2();
    private float directionChangeTimer = 0f;

    private long lastMindFractureHitId = -1;

    public DummyEnemy(float x, float y, GameCharacter target) {
        super(x, y, 125f, 50f); // Speed 125 (balanced - not too slow), HP 50
        this.target = target;

        this.atk = 15f; // Increased from default (was too weak)

        // Bikin tekstur kotak merah
        createTexture();

        // Ukuran musuh
        this.bounds.setSize(32, 32);

        this.attackCooldown = 1.0f;
    }

    private void createTexture() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();
        this.texture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        // Apply blue tint jika frozen, purple tint jika insane
        if (frozen) {
            // Modulasi warna batch ke cyan semi-transparent
            batch.setColor(0.5f, 0.8f, 1f, 0.7f); // Light blue overlay
        } else if (isInsane) { // Use parent's isInsane
            // Modulasi warna batch ke purple untuk Insanity
            batch.setColor(0.8f, 0.3f, 0.8f, 0.8f); // Purple overlay
        }

        // Call parent render
        super.render(batch);

        // Reset warna batch ke normal
        if (frozen || isInsane) { // Use parent's isInsane
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void performInnateSkill() {
        // Dummy gak punya skill
    }

    @Override
    public float getInnateSkillTimer() {
        return 0; // No skill
    }

    @Override
    public float getInnateSkillCooldown() {
        return 0; // No skill
    }

    @Override
    public String getAttackAnimationType() {
        return "slash"; // Default
    }

    public float getXpReward() {
        return 25f;
    }

    public void reset(float x, float y, GameCharacter newTarget) {
        this.position.set(x, y);
        this.bounds.setPosition(x, y);
        this.target = newTarget;

        // Reset stats ke kondisi awal
        this.hp = this.maxHp;
        this.attackTimer = 0;

        // Reset freeze status
        this.frozen = false;
        this.freezeTimer = 0;

        // Reset insanity status (use parent's system)
        super.clearInsanity();

        this.isMarked = false;
    }

    public void freeze() {
        this.frozen = true;
        this.freezeTimer = FREEZE_DURATION;
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
        lastMindFractureHitId = activationId;
    }

    private void generateRandomDirection() {
        float angle = (float) (Math.random() * Math.PI * 2);
        randomDirection.set(
                (float) Math.cos(angle),
                (float) Math.sin(angle)).nor();
    }

    @Override
    public float getAtk() {
        if (isInsane) { // Uses GameCharacter's isInsane field
            return super.getAtk() * 1.5f; // 50% buff for consistency with Boss
        }
        return super.getAtk();
    }

    @Override
    public float getArts() {
        if (isInsane) { // Uses GameCharacter's isInsane field
            return super.getArts() * 1.5f; // 50% buff for consistency with Boss
        }
        return super.getArts();
    }

    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);

        // Unfreeze jika diserang saat frozen
        if (frozen) {
            frozen = false;
            freezeTimer = 0;
            System.out.println("Enemy unfrozen by damage!");
        }
    }

    // Logic AI: Kejar target
    public void update(float delta) {
        super.update(delta);

        // Update freeze timer
        if (frozen) {
            freezeTimer -= delta;
            if (freezeTimer <= 0) {
                frozen = false;
                freezeTimer = 0;
            }
            // Skip movement saat frozen - enemy tidak bisa bergerak
            return;
        }

        // Update insanity (use parent's insanityTimer)
        // Parent class already updates insanityTimer in super.update()
        if (isInsane) {
            // Random movement saat insane
            directionChangeTimer -= delta;
            if (directionChangeTimer <= 0) {
                generateRandomDirection();
                directionChangeTimer = 0.5f; // Change direction every 0.5 seconds
            }

            // Move in random direction
            move(randomDirection, delta);
            return;
        }

        if (target != null && !isBeingPulled) {
            // titik tengah visual player
            float targetCenterX = target.getPosition().x + target.getVisualWidth() / 2;
            float targetFeetY = target.getPosition().y;

            // titik tengah visual dummy enemy
            float myCenterX = this.position.x + getVisualWidth() / 2;
            float myFeetY = this.position.y;
            Vector2 direction = new Vector2(
                    targetCenterX - myCenterX,
                    targetFeetY - myFeetY).nor(); // Normalisasi biar speed konstan

            move(direction, delta);
        }
    }
}
