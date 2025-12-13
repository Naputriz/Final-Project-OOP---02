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
        // Apply blue tint jika frozen
        if (frozen) {
            // Modulasi warna batch ke cyan semi-transparent
            batch.setColor(0.5f, 0.8f, 1f, 0.7f); // Light blue overlay
        }

        // Call parent render
        super.render(batch);

        // Reset warna batch ke normal
        if (frozen) {
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void performInnateSkill() {
        // Dummy gak punya skill
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
    }

    public void freeze() {
        this.frozen = true;
        this.freezeTimer = FREEZE_DURATION;
    }

    public boolean isFrozen() {
        return frozen;
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
