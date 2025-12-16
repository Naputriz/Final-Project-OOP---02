package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.managers.AssetManager;

import java.util.HashSet;

public class MeleeAttack {
    private Rectangle bounds; // Hitbox untuk collision
    private Vector2 position;
    private float damage;
    private float duration; // Berapa lama attack aktif
    private float timer; // Timer untuk track duration
    private boolean active;

    // Animation support
    private Animation<TextureRegion> attackAnimation;
    private float animationTime;
    private String animationType; // "slash" or "scratch"
    private float rotationAngle; // Rotation angle in degrees (0 = right, 90 = up, 180 = left, 270 = down)

    // Damage tracking -mencegah hit multiple kali
    private HashSet<GameCharacter> hitEnemies;

    public MeleeAttack(float x, float y, float width, float height, float damage, float duration, String animationType,
            float rotationAngle) {
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, width, height);
        this.damage = damage;
        this.duration = duration;
        this.timer = 0;
        this.active = true;
        this.animationType = animationType;
        this.rotationAngle = rotationAngle;
        this.animationTime = 0;
        this.hitEnemies = new HashSet<>();

        loadAnimation();
    }

    private void loadAnimation() {
        String spritePath;

        // Pilih spritesheet berdasarkan type
        switch (animationType.toLowerCase()) {
            case "slash":
                spritePath = "AttackAnimations/Slash Animation.png";
                break;
            case "scratch":
                spritePath = "AttackAnimations/Scratch Animation.png";
                break;
            default:
                spritePath = "AttackAnimations/Slash Animation.png";
                break;
        }

        // Load spritesheet
        Texture spritesheet = AssetManager.getInstance().loadTexture(spritePath);

        // Split spritesheet (7 frames in 3x3 grid)
        int cols = 3;
        int rows = 3;
        int actualFrames = 7; // Only 7 frames, not all 9 cells

        TextureRegion[][] tmp = TextureRegion.split(
                spritesheet,
                spritesheet.getWidth() / cols,
                spritesheet.getHeight() / rows);

        // Extract only first 7 frames
        TextureRegion[] frames = new TextureRegion[actualFrames];
        int index = 0;
        for (int i = 0; i < rows && index < actualFrames; i++) {
            for (int j = 0; j < cols && index < actualFrames; j++) {
                frames[index++] = tmp[i][j];
            }
        }

        // Create animation (play once, not loop)
        float frameDuration = duration / actualFrames; // Spread across attack duration
        attackAnimation = new Animation<>(frameDuration, frames);
        attackAnimation.setPlayMode(Animation.PlayMode.NORMAL); // Play once
    }

    public void update(float delta) {
        if (!active)
            return;

        timer += delta;
        animationTime += delta;

        if (timer >= duration) {
            active = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!active)
            return;

        // Get current frame from animation
        TextureRegion currentFrame = attackAnimation.getKeyFrame(animationTime, false);

        // Calculate sprite size and position
        float spriteSize = Math.max(bounds.width, bounds.height) * 1.5f; // Slightly larger than hitbox
        float offsetX = (spriteSize - bounds.width) / 2;
        float offsetY = (spriteSize - bounds.height) / 2;

        // Draw position (top-left corner of sprite)
        float drawX = position.x - offsetX;
        float drawY = position.y - offsetY;

        // Origin point for rotation (center of sprite)
        float originX = spriteSize / 2;
        float originY = spriteSize / 2;

        // Determine if we need to flip the sprite vertically
        // When facing left (angles roughly 90° to 270°), flip Y to maintain slash
        // direction
        float scaleY = 1f;
        if (rotationAngle > 90 && rotationAngle < 270) {
            scaleY = -1f; // Flip vertically
        }

        // Draw with rotation and optional vertical flip
        // Parameters: texture, x, y, originX, originY, width, height, scaleX, scaleY,
        // rotation
        batch.draw(currentFrame,
                drawX, drawY, // Position
                originX, originY, // Origin (rotation point)
                spriteSize, spriteSize, // Size
                1f, scaleY, // Scale (flip Y when facing left)
                rotationAngle); // Rotation in degrees
    }

    public boolean canHit(GameCharacter enemy) {
        if (!active)
            return false;
        if (hitEnemies.contains(enemy))
            return false;
        return bounds.overlaps(enemy.getBounds());
    }

    public void markAsHit(GameCharacter enemy) {
        hitEnemies.add(enemy);
    }

    public float getDamage() {
        return damage;
    }

    public boolean isActive() {
        return active;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
