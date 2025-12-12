package com.kelompok2.frontend.entities;

import com.kelompok2.frontend.strategies.MeleeAttackStrategy;
import com.kelompok2.frontend.managers.AssetManager;

public class Ryze extends GameCharacter {

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
    public void performInnateSkill() {
        // Implementasi skill Spectral Body nanti di sini
        System.out.println("Ryze pake skill");
    }
}
