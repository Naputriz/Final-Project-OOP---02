package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Projectile {
    private Vector2 position;
    private Vector2 velocity;
    private float speed = 400f; // Kecepatan peluru
    private Texture texture;
    private Rectangle bounds;
    public boolean active; // Penanda apakah peluru masih aktif
    private float damage; // Damage yang diberikan peluru (scaling dari Arts)
    private Color color = Color.YELLOW; // Warna projectile (default kuning)
    private boolean isFireball = false; // Flag untuk fireball skill

    private float distanceTraveled = 0f;
    private float maxDistance = 1000f; // Max range of the bullet

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
        // Make fireballs bigger for visual impact
        int size = isFireball ? 30 : 10;

        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(color); // Gunakan warna custom
        pixmap.fill();
        this.texture = new Texture(pixmap);
        pixmap.dispose();

        // Update bounds size jika fireball
        if (isFireball) {
            this.bounds.setSize(size, size);
        }
    }

    public void setColor(Color newColor) {
        this.color = newColor;
        // Recreate texture dengan warna baru
        if (texture != null) {
            texture.dispose();
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
    }

    public void update(float delta) {

        // Calculate movement for this frame
        float moveX = velocity.x * delta;
        float moveY = velocity.y * delta;

        // Gerakkan peluru
        position.add(moveX, moveY);
        bounds.setPosition(position.x, position.y);

        // Track distance
        distanceTraveled += Math.sqrt(moveX * moveX + moveY * moveY);

        // Hapus peluru kalau sudah jalan terlalu jauh
        // Nanti diganti logika collision tembok (maybe)
        if (distanceTraveled > maxDistance) {
            active = false;
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y);
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

    public void dispose() {
        texture.dispose();
    }
}
