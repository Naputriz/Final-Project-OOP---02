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

    private float distanceTraveled = 0f;
    private float maxDistance = 1000f; // Max range of the bullet

    public Projectile(float startX, float startY, float targetX, float targetY) {
        this.position = new Vector2(startX, startY);
        this.active = true;
        this.bounds = new Rectangle(startX, startY, 10, 10); // Ukuran peluru

        // Hitung arah peluru (Matematika Vektor)
        Vector2 direction = new Vector2(targetX - startX, targetY - startY).nor();
        this.velocity = direction.scl(speed);

        // Bikin gambar kotak kuning sederhana
        createTexture();
    }

    private void createTexture() {
        Pixmap pixmap = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.YELLOW);
        pixmap.fill();
        this.texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float delta) {

        // Calculate movement for this frame
        float moveX = velocity.x * delta;
        float moveY = velocity.y * delta;

        // Gerakkan peluru
        position.add(moveX, moveY);
        bounds.setPosition(position.x, position.y);

        // Track distance
        distanceTraveled += Math.sqrt(moveX*moveX + moveY*moveY);

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

    public void dispose() {
        texture.dispose();
    }
}
