package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.HashSet;
import java.util.Set;

public class Projectile {
    private Vector2 position;
    private Vector2 velocity;
    private float speed = 400f; // Kecepatan peluru
    private Texture defaultTexture;
    private Texture customTexture;
    private Rectangle bounds;
    public boolean active; // Penanda apakah peluru masih aktif
    private float damage; // Damage yang diberikan peluru (scaling dari Arts)
    private Color color = Color.YELLOW; // Warna projectile (default kuning)
    private boolean isFireball = false; // Flag untuk fireball skill

    private float distanceTraveled = 0f;
    private float maxDistance = 1000f; // Max range of the bullet

    // Piercing Logic
    private boolean piercing = false;
    private Set<GameCharacter> hitEntities = new HashSet<>();

    // Visual Scaling
    private float visualSize = 16f;

    public Projectile(float startX, float startY, float targetX, float targetY, float damage) {
        this(startX, startY, targetX, targetY, damage, Color.YELLOW);
    }

    public Projectile(float startX, float startY, float targetX, float targetY, float damage, Color color) {
        this(startX, startY, targetX, targetY, damage, color, 400f); // Default speed
    }

    public Projectile(float startX, float startY, float targetX, float targetY, float damage, Color color,
            float speed) {
        this.position = new Vector2(startX, startY);
        this.active = true;
        this.bounds = new Rectangle(startX, startY, 10, 10); // Ukuran peluru
        this.damage = damage;
        this.color = color;
        this.speed = speed; // Use custom speed

        // Hitung arah peluru (Matematika Vektor)
        Vector2 direction = new Vector2(targetX - startX, targetY - startY).nor();
        this.velocity = direction.scl(this.speed);

        // Bikin gambar kotak dengan warna custom
        createTexture();
    }

    public Projectile(float startX, float startY, float targetX, float targetY) {
        this(startX, startY, targetX, targetY, 25f);
    }

    // Constructor untuk Object Pool (menerima Vector2 direction langsung)
    public Projectile(float startX, float startY, Vector2 direction, float damage) {
        this.position = new Vector2(startX, startY);
        this.active = true;
        this.bounds = new Rectangle(startX, startY, 10, 10);
        this.damage = damage;
        this.velocity = direction.cpy().nor().scl(speed);
        createTexture();
    }

    private void createTexture() {
        int size = isFireball ? 30 : 10;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        this.defaultTexture = new Texture(pixmap); // Store as default
        pixmap.dispose();

        if (isFireball) {
            this.bounds.setSize(size, size);
        }
    }

    public void setVisualSize(float size) {
        this.visualSize = size;
    }

    public void setTexture(Texture texture) {
        this.customTexture = texture;
    }

    public void setColor(Color newColor) {
        this.color = newColor;
        if (defaultTexture != null) {
            defaultTexture.dispose();
        }
        createTexture();
    }

    public void reset(float x, float y, Vector2 direction, float newDamage) {
        this.position.set(x, y);
        this.velocity = direction.cpy().nor().scl(speed);
        this.bounds.setPosition(x, y);
        this.damage = newDamage;
        this.active = true;
        this.distanceTraveled = 0f;
        this.isEnemyProjectile = false;
        this.hitEntities.clear();

        this.customTexture = null;
        this.bounds.setSize(10, 10);
        this.visualSize = 32f;
    }

    public void update(float delta) {

        // Calculate movement for this frame
        float moveX = velocity.x * delta;
        float moveY = velocity.y * delta;

        // Gerakkan peluru
        position.add(moveX, moveY);


        if (customTexture != null) {
            // Optional: Adjust hit box to be bigger for skills
            // this.bounds.setSize(64, 64);
        }

        bounds.setPosition(position.x, position.y);
        distanceTraveled += Math.sqrt(moveX * moveX + moveY * moveY);

        if (distanceTraveled > maxDistance) {
            active = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;

        if (customTexture != null) {
            float size = this.visualSize;

            float drawX = position.x - (size / 2) + (bounds.width / 2);
            float drawY = position.y - (size / 2) + (bounds.height / 2);

            batch.setColor(Color.WHITE);
            batch.draw(customTexture, drawX, drawY, size, size);
        } else {
            batch.draw(defaultTexture, position.x, position.y);
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setVelocity(float vx, float vy) {
        this.velocity.set(vx, vy);
    }

    public void setFireball(boolean isFireball) {
        this.isFireball = isFireball;
        if (isFireball) {
            setColor(new Color(1f, 0.5f, 0f, 1f)); // Orange for fireball
        }
    }

    public boolean isFireball() {
        return isFireball;
    }

    // Damage Type
    private boolean isArts = true; // Default to true for projectiles as most are Arts

    public boolean isArts() {
        return isArts;
    }

    public void setArts(boolean arts) {
        isArts = arts;
    }

    private boolean isEnemyProjectile = false;

    public boolean isEnemyProjectile() {
        return isEnemyProjectile;
    }

    public void setEnemyProjectile(boolean isEnemyProjectile) {
        this.isEnemyProjectile = isEnemyProjectile;
    }

    public boolean isPiercing() {
        return piercing;
    }

    public void setPiercing(boolean piercing) {
        this.piercing = piercing;
    }

    public boolean canHit(GameCharacter target) {
        return !hitEntities.contains(target);
    }

    public void addHit(GameCharacter target) {
        hitEntities.add(target);
    }

    public void dispose() {
        if (defaultTexture != null) defaultTexture.dispose();
    }
}
