package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class GameCharacter {
    protected Vector2 position;
    protected float speed;
    protected float hp;
    protected float maxHp;
    protected Texture texture; // Sprite karakter
    protected Rectangle bounds; // Hitbox untuk collision

    public GameCharacter(float x, float y, float speed, float maxHp) {
        this.position = new Vector2(x, y);
        this.speed = speed;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.bounds = new Rectangle(x, y, 32, 32); // Default size 32x32
    }

    // Method abstract buat innate skill
    public abstract void performInnateSkill();

    public void move(float deltaX, float deltaY) {
        position.x += deltaX * speed;
        position.y += deltaY * speed;

        // Update posisi hitbox mengikuti gambar
        bounds.setPosition(position.x, position.y);
    }

    public void render(SpriteBatch batch) {
        // Gambar karakter di posisi X, Y
        if (texture != null) {
            // Gambar dirender seukuran hitbox
            batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
        }
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.bounds.setPosition(x, y);
    }

    // Getter Setter standar
    public Rectangle getBounds() { return bounds; }
    public float getHp() { return hp; }
    public float getWidth() { return bounds.width; }
    public float getHeight() { return bounds.height; }
}
