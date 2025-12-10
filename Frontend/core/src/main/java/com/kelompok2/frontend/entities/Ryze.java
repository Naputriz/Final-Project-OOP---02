package com.kelompok2.frontend.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Ryze extends GameCharacter {

    public Ryze(float x, float y) {
        super(x, y, 200f, 100f); // Stats sementara, HP ama Speed dulu buat testing

        // Sementara, nanti diganti
        this.texture = new Texture(Gdx.files.internal("ryze_placeholder.png"));
        float visualSize = 256f;
        this.renderWidth = visualSize;
        this.renderHeight = visualSize;
        // Ukuran hitbox 1/3 dari visualnya (hanya badan yang dianggap hitbox)
        float hitboxWidth = visualSize / 3f;
        float hitboxHeight = visualSize * (2f/3f);

        this.bounds.setSize(hitboxWidth, hitboxHeight);

        this.boundsOffsetX = (visualSize - hitboxWidth) / 2f;
        this.boundsOffsetY = 0;

        // Update posisi bounds awal
        setPosition(x, y);

        this.autoAttack = false;
        this.attackCooldown = 0;
    }

    @Override
    public void attack(Vector2 targetPos, Array<Projectile> projectiles) {
        // Bullet spawn point
        float startX = this.position.x + this.getVisualWidth() / 2;
        float startY = this.position.y + this.getVisualHeight() / 2;

        // Create projectile logic
        Projectile p = new Projectile(startX, startY, targetPos.x, targetPos.y);
        projectiles.add(p);
    }

    @Override
    public void performInnateSkill() {
        // Implementasi skill Spectral Body nanti di sini
        System.out.println("Ryze pake skill");
    }
}
