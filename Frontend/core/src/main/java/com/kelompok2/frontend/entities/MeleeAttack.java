package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;


public class MeleeAttack {
    private Rectangle bounds; // Hitbox untuk collision
    private float damage; // Damage yang akan diberikan
    private float duration; // Berapa lama hitbox aktif (dalam detik)
    private float elapsed; // Waktu yang sudah berlalu
    private GameCharacter owner; // Siapa yang membuat serangan ini (untuk mencegah self-damage)
    public boolean active; // Apakah serangan masih aktif

    // Track target yang sudah terkena, mencegah multi-hit dalam satu serangan
    private Array<GameCharacter> hitTargets;

    public MeleeAttack(float x, float y, float width, float height,
            float damage, float duration, GameCharacter owner) {
        this.bounds = new Rectangle(x, y, width, height);
        this.damage = damage;
        this.duration = duration;
        this.elapsed = 0f;
        this.owner = owner;
        this.active = true;
        this.hitTargets = new Array<>();
    }

    public void update(float delta) {
        elapsed += delta;
        if (elapsed >= duration) {
            active = false;
        }
    }

    public boolean canHit(GameCharacter target) {
        // Tidak bisa hit diri sendiri
        if (target == owner) {
            return false;
        }

        // Tidak bisa hit target yang sudah pernah terkena
        return !hitTargets.contains(target, true);
    }

    public void markAsHit(GameCharacter target) {
        hitTargets.add(target);
    }

    // Render hitbox, nanti akan diganti animasi
    public void render(ShapeRenderer shapeRenderer) {
        if (active) {
            shapeRenderer.setColor(new Color(1f, 0f, 0f, 0.3f)); // Merah semi-transparan
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    // Getters
    public Rectangle getBounds() {
        return bounds;
    }

    public float getDamage() {
        return damage;
    }

    public GameCharacter getOwner() {
        return owner;
    }

    public boolean isActive() {
        return active;
    }
}
