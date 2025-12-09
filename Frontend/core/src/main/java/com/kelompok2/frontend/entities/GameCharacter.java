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
    protected boolean isFacingRight;
    protected Texture texture; // Sprite karakter
    protected Rectangle bounds; // Hitbox untuk collision
    protected int level;
    protected float currentXp;
    protected float xpToNextLevel;

    public GameCharacter(float x, float y, float speed, float maxHp) {
        this.position = new Vector2(x, y);
        this.speed = speed;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.isFacingRight = true; // Default menghadap kanan
        this.bounds = new Rectangle(x, y, 32, 32); // Default size 32x32
        this.level = 1;
        this.currentXp = 0;
        this.xpToNextLevel = 100;
    }

    // Method abstract buat innate skill
    public abstract void performInnateSkill();

    public void move(float deltaX, float deltaY) {
        position.x += deltaX * speed;
        position.y += deltaY * speed;

        // Update posisi hitbox mengikuti gambar
        bounds.setPosition(position.x, position.y);
    }

    public void takeDamage(float amount) {
        hp -= amount;
        if (hp < 0) hp = 0;
    }

    public void heal(float amount) {
        hp += amount;
        if (hp > maxHp) hp = maxHp;
    }

    public void gainXp(float xpAmount){
        this.currentXp += xpAmount;
        if(this.currentXp >= this.xpToNextLevel){
            levelUp();
        }
    }

    protected void levelUp() {
        this.currentXp -= this.xpToNextLevel;
        this.level++;
        this.xpToNextLevel = (float) Math.ceil(this.xpToNextLevel * 1.2f);
        this.maxHp += 20;
        System.out.println(this.getClass().getSimpleName() + " Level Up! lv: " + level);
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public void setFacingRight(boolean isFacingRight) {
        this.isFacingRight = isFacingRight;
    }

    public void render(SpriteBatch batch) {
        // Gambar karakter di posisi X, Y
        if (texture != null) {
            // Cek apakah texture perlu di-flip
            // Gambar asli (aset png) karakter menghadap ke KIRI
            // Jika isFacingRight true (mau hadap kanan), flipX harus true (dibalik)
            // Jika isFacingRight false (mau hadap kiri), flipX false (jangan dibalik, pakai asli)
            boolean flipX = isFacingRight;
            // Gambar dirender seukuran hitbox
            batch.draw(
                texture,
                position.x,
                position.y,
                bounds.width,
                bounds.height,
                0,
                0,
                texture.getWidth(),
                texture.getHeight(),
                flipX,
                false // flipY (tidak perlu dibalik secara vertikal)
            );
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
    public Vector2 getPosition() { return position; } // Helper buat kamera
    public Rectangle getBounds() { return bounds; }
    public float getHp() { return hp; }
    public float getMaxHp() { return maxHp; }
    public float getWidth() { return bounds.width; }
    public float getHeight() { return bounds.height; }
    public int getLevel(){
        return level;
    }
    public float getCurrentXp(){
        return currentXp;
    }
    public float getXpToNextLevel(){
        return xpToNextLevel;
    }
}
