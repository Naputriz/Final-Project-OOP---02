package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kelompok2.frontend.strategies.MeleeAttackStrategy;
import com.kelompok2.frontend.managers.AssetManager;

public class Ryze extends GameCharacter {

    // Spectral Body skill tracking
    private float skillCooldown = 15f; // 15 seconds cooldown
    private float skillTimer = 0f;
    private boolean spectralBodyActive = false;
    private float spectralDuration = 3f; // 3 seconds invulnerability
    private float spectralTimer = 0f;

    public Ryze(float x, float y) {
        super(x, y, 200f, 100f); // Stats sementara, HP ama Speed dulu buat testing
        this.atk = 30f; // High ATK untuk melee
        this.arts = 10f; // Low Arts
        this.def = 5f; // Low Defence

        // Load texture melalui AssetManager
        this.texture = AssetManager.getInstance().loadTexture("ryze_placeholder.png");
        float visualSize = 256f;
        this.renderWidth = visualSize;
        this.renderHeight = visualSize;
        // Ukuran hitbox 1/3 dari visualnya (hanya badan yang dianggap hitbox)
        float hitboxWidth = visualSize / 3f;
        float hitboxHeight = visualSize * (2f / 3f);

        this.bounds.setSize(hitboxWidth, hitboxHeight);

        this.boundsOffsetX = (visualSize - hitboxWidth) / 2f;
        this.boundsOffsetY = 0;

        // Update posisi bounds awal
        setPosition(x, y);

        this.attackStrategy = new MeleeAttackStrategy(150f, 120f, 1.0f, 0.2f);

        this.autoAttack = false;
        this.attackCooldown = 0.4f; // Cooldown agak lama untuk scythe swing
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update skill cooldown
        if (skillTimer > 0) {
            skillTimer -= delta;
        }

        // Update Spectral Body duration
        if (spectralBodyActive) {
            spectralTimer -= delta;
            if (spectralTimer <= 0) {
                spectralBodyActive = false;
                spectralTimer = 0;
                System.out.println("[Ryze] Spectral Body ended");
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Apply transparency saat Spectral Body aktif
        if (spectralBodyActive) {
            batch.setColor(1f, 1f, 1f, 0.5f);
        }

        super.render(batch);

        // Reset color
        if (spectralBodyActive) {
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void takeDamage(float damage) {
        if (spectralBodyActive) {
            System.out.println("[Ryze] Attack passed through! (Spectral Body)");
            return; // Ignore damage completely
        }
        super.takeDamage(damage);
    }

    public boolean isInvulnerable() {
        return spectralBodyActive;
    }

    @Override
    public void performInnateSkill() {
        // Check cooldown
        if (skillTimer > 0) {
            System.out.println("[Ryze] Spectral Body on cooldown: " +
                    String.format("%.1f", skillTimer) + "s remaining");
            return;
        }

        // Activate Spectral Body
        spectralBodyActive = true;
        spectralTimer = spectralDuration;
        skillTimer = skillCooldown;

        System.out.println("[Ryze] Spectral Body activated! Invulnerable for 3 seconds!");
    }
}
