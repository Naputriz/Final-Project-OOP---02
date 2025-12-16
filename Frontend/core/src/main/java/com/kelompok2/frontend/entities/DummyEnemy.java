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

    // Insanity status effect (untuk Mind Fracture)
    private boolean insane = false;
    private float insanityTimer = 0f;
    private static final float INSANITY_DURATION = 5.0f; // 5 detik insanity
    private static final float INSANITY_ATK_BUFF = 10f; // +10 ATK saat insane
    private static final float INSANITY_ARTS_BUFF = 10f; // +10 Arts saat insane

    // Random movement untuk Insanity
    private Vector2 randomDirection = new Vector2();
    private float directionChangeTimer = 0f;

    public DummyEnemy(float x, float y, GameCharacter target) {
        super(x, y, 150f, 50f); // Speed 150 (lebih lambat dari player), HP 50
        this.target = target;

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
        } else if (insane) {
            // Modulasi warna batch ke purple untuk Insanity
            batch.setColor(0.8f, 0.3f, 0.8f, 0.8f); // Purple overlay
        }

        // Call parent render
        super.render(batch);

        // Reset warna batch ke normal
        if (frozen || insane) {
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

        // Reset insanity status
        this.insane = false;
        this.insanityTimer = 0;
    }

    public void freeze() {
        this.frozen = true;
        this.freezeTimer = FREEZE_DURATION;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void applyInsanity() {
        this.insane = true;
        this.insanityTimer = INSANITY_DURATION;
        // Generate initial random direction
        generateRandomDirection();
        System.out.println("[Enemy] Insanity applied! Moving randomly for 5 seconds");
    }

    public boolean isInsane() {
        return insane;
    }

    private void generateRandomDirection() {
        float angle = (float) (Math.random() * Math.PI * 2);
        randomDirection.set(
                (float) Math.cos(angle),
                (float) Math.sin(angle)).nor();
    }

    @Override
    public float getAtk() {
        if (insane) {
            return super.getAtk() + INSANITY_ATK_BUFF;
        }
        return super.getAtk();
    }

    @Override
    public float getArts() {
        if (insane) {
            return super.getArts() + INSANITY_ARTS_BUFF;
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

        // Update insanity timer
        if (insane) {
            insanityTimer -= delta;
            if (insanityTimer <= 0) {
                insane = false;
                insanityTimer = 0;
                System.out.println("[Enemy] Insanity ended");
            }

            // Random movement saat insane
            directionChangeTimer -= delta;
            if (directionChangeTimer <= 0) {
                generateRandomDirection();
                directionChangeTimer = 0.5f; // Change direction every 0.5 seconds
            }

            // Move in random direction
            move(randomDirection.x * delta, randomDirection.y * delta);
            return;
        }

        // Normal AI: chase player (jika tidak frozen dan tidak insane)
        if (target != null) {
            // titik tengah visual player
            float targetCenterX = target.getPosition().x + target.getVisualWidth() / 2;
            float targetFeetY = target.getPosition().y;

            // titik tengah visual dummy enemy
            float myCenterX = this.position.x + getVisualWidth() / 2;
            float myFeetY = this.position.y;
            Vector2 direction = new Vector2(
                    targetCenterX - myCenterX,
                    targetFeetY - myFeetY).nor(); // Normalisasi biar speed konstan

            move(direction.x * delta, direction.y * delta);
        }
    }
}
