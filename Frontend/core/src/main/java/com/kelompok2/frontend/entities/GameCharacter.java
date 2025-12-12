package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.strategies.AttackStrategy;
import com.kelompok2.frontend.managers.GameManager;

public abstract class GameCharacter {
    protected Vector2 position;
    protected float speed;
    protected float hp;
    protected float maxHp;
    protected float atk;
    protected float arts;
    protected float def;
    protected boolean isFacingRight;
    protected Texture texture; // Sprite karakter
    protected Rectangle bounds; // Hitbox untuk collision
    protected float renderWidth = -1; // -1 artinya belum diset (default ikut bounds)
    protected float renderHeight = -1;
    protected float boundsOffsetX = 0;
    protected float boundsOffsetY = 0;
    protected int level;
    protected float currentXp;
    protected float xpToNextLevel;
    protected float attackCooldown; // Cooldown value
    protected float attackTimer; // Cooldown timer
    protected boolean autoAttack; // True = Hold, False = Click

    // Strategy Pattern untuk attack behavior
    protected AttackStrategy attackStrategy;

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

        // Default stats (akan di-override oleh subclass)
        this.atk = 10f;
        this.arts = 10f;
        this.def = 5f;

        this.attackCooldown = 0.5f;
        this.attackTimer = 0;
        this.autoAttack = false;
    }

    public void attack(Vector2 targetPos, Array<Projectile> projectiles, Array<MeleeAttack> meleeAttacks) {
        if (attackStrategy != null) {
            attackStrategy.execute(this, targetPos, projectiles, meleeAttacks);
        }
    }

    // Method abstract buat innate skill
    public abstract void performInnateSkill();

    // Update untuk mengurangi timer
    public void update(float delta) {
        if (attackTimer > 0) {
            attackTimer -= delta;
        }
    }

    // Check bisa attack jika timer habis
    public boolean canAttack() {
        return attackTimer <= 0;
    }

    // Reset timer
    public void resetAttackTimer() {
        this.attackTimer = attackCooldown;
    }

    // Cek tipe attack
    public boolean isAutoAttack() {
        return autoAttack;
    }

    public void move(float deltaX, float deltaY) {
        position.x += deltaX * speed;
        position.y += deltaY * speed;

        // Position sekarang merepresentasikan posisi pojok kiri-bawah GAMBAR (Visual),
        // bukan Hitbox
        bounds.setPosition(position.x + boundsOffsetX, position.y + boundsOffsetY);
    }

    public void takeDamage(float amount) {
        hp -= amount;
        if (hp < 0)
            hp = 0;
    }

    public void heal(float amount) {
        hp += amount;
        if (hp > maxHp)
            hp = maxHp;
    }

    public void gainXp(float xpAmount) {
        this.currentXp += xpAmount;
        if (this.currentXp >= this.xpToNextLevel) {
            levelUp();
        }
    }

    protected void levelUp() {
        this.currentXp -= this.xpToNextLevel;
        this.level++;
        this.xpToNextLevel = (float) Math.ceil(this.xpToNextLevel * 1.2f);
        this.maxHp += 20;

        // Sync dengan GameManager (Singleton Pattern)
        GameManager.getInstance().incrementLevel();

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
            // Jika isFacingRight false (mau hadap kiri), flipX false (jangan dibalik, pakai
            // asli)
            boolean flipX = isFacingRight;
            // [LOGIKA BARU] Gunakan renderWidth/Height jika ada. Jika tidak, pakai bounds.
            float drawWidth = (renderWidth > 0) ? renderWidth : bounds.width;
            float drawHeight = (renderHeight > 0) ? renderHeight : bounds.height;
            // Gambar dirender seukuran hitbox
            batch.draw(
                    texture,
                    position.x,
                    position.y,
                    drawWidth,
                    drawHeight,
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
        if (texture != null)
            texture.dispose();
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.bounds.setPosition(x + boundsOffsetX, y + boundsOffsetY);
    }

    // Getter Setter standar
    public Vector2 getPosition() {
        return position;
    } // Helper buat kamera

    public Rectangle getBounds() {
        return bounds;
    }

    public float getHp() {
        return hp;
    }

    public float getMaxHp() {
        return maxHp;
    }

    public float getWidth() {
        return bounds.width;
    }

    public float getHeight() {
        return bounds.height;
    }

    public int getLevel() {
        return level;
    }

    public float getCurrentXp() {
        return currentXp;
    }

    public float getXpToNextLevel() {
        return xpToNextLevel;
    }

    public float getVisualWidth() {
        return (renderWidth > 0) ? renderWidth : bounds.width;
    }

    public float getVisualHeight() {
        return (renderHeight > 0) ? renderHeight : bounds.height;
    }

    // Getters untuk stats (GDD stats)
    public float getAtk() {
        return atk;
    }

    public float getArts() {
        return arts;
    }

    public float getDef() {
        return def;
    }

    // Setter untuk attack strategy (Strategy Pattern)
    public void setAttackStrategy(AttackStrategy strategy) {
        this.attackStrategy = strategy;
    }

    public AttackStrategy getAttackStrategy() {
        return attackStrategy;
    }
}
