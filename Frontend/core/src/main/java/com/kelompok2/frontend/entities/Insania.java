package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.strategies.MeleeAttackStrategy;
import com.kelompok2.frontend.managers.AssetManager;

public class Insania extends GameCharacter {

    // Animation system untuk spritesheet
    private Animation<TextureRegion> idleAnimation;
    private float stateTime; // Timer untuk tracking animation frame

    // Mind Fracture skill tracking
    private float skillCooldown = 10f; // 10 seconds cooldown
    private float skillTimer = 0f;
    private float skillRadius = 300f; // Radius untuk Mind Fracture AoE
    private boolean mindFractureJustActivated = false; // Flag untuk GameScreen (single-use)
    private float circleDisplayTimer = 0f; // Timer untuk display circle
    private static final float CIRCLE_DISPLAY_DURATION = 0.5f; // Show circle for 0.5s

    public Insania(float x, float y) {
        super(x, y, 180f, 110f);

        // Set stats sesuai role Hybrid Attacker
        this.atk = 35f; // High ATK - primary damage
        this.arts = 25f; // Moderate Arts - untuk skill
        this.def = 5f; // Low Defence - glass cannon style

        // Load spritesheet baru (4 frames, 2x2) melalui AssetManager
        Texture spritesheet = AssetManager.getInstance().loadTexture("Insania/pcgp-insania-idle.png");

        // Split spritesheet menjadi individual frames (2 kolom x 2 baris = 4 frames)
        int FRAME_COLS = 2;
        int FRAME_ROWS = 2;
        TextureRegion[][] tmp = TextureRegion.split(
                spritesheet,
                spritesheet.getWidth() / FRAME_COLS,
                spritesheet.getHeight() / FRAME_ROWS);

        // Convert 2D array ke 1D array untuk animation
        TextureRegion[] idleFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                idleFrames[index++] = tmp[i][j];
            }
        }

        // Buat idle animation (0.1 detik per frame = 10 FPS)
        idleAnimation = new Animation<>(0.15f, idleFrames); // Slightly slower for 4 frames
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Initialize state time
        stateTime = 0f;

        // Set texture ke spritesheet untuk bounds calculation
        this.texture = spritesheet;

        // Ukuran visual dan hitbox
        float visualSize = 128f;
        this.renderWidth = visualSize;
        this.renderHeight = visualSize;

        // Hitbox lebih kecil dari visual (1/3 lebar, 2/3 tinggi)
        float hitboxWidth = visualSize / 3f;
        float hitboxHeight = visualSize * (2f / 3f);
        this.bounds.setSize(hitboxWidth, hitboxHeight);

        // Offset hitbox ke tengah visual
        this.boundsOffsetX = (visualSize - hitboxWidth) / 2f;
        this.boundsOffsetY = 0;

        // Update posisi bounds awal
        setPosition(x, y);

        // Melee Attack Strategy - Mad Claw (Physical Scaling)
        // MeleeAttackStrategy(range, width, damageMultiplier, cooldown)
        // Damage scales from ATK (physical melee)
        this.attackStrategy = new MeleeAttackStrategy(120f, 100f, 1.0f, 0.3f);

        this.autoAttack = true;
        this.attackCooldown = 0.5f; // Medium cooldown untuk Mad Claw
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;

        // Update skill cooldown
        if (skillTimer > 0) {
            skillTimer -= delta;
        }

        // Update circle display timer
        if (circleDisplayTimer > 0) {
            circleDisplayTimer -= delta;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Get frame saat ini dari animation
        TextureRegion currentFrame = idleAnimation.getKeyFrame(stateTime);

        // Hitung posisi render
        float renderX = position.x;
        float renderY = position.y;

        // Flip sprite based on facing direction
        // Sprite awalnya menghadap KIRI, jadi:
        // - Jika isFacingRight = true dan sprite belum flip -> FLIP
        // - Jika isFacingRight = false dan sprite sudah flip -> FLIP BACK
        boolean needsFlip = (isFacingRight && !currentFrame.isFlipX()) || (!isFacingRight && currentFrame.isFlipX());
        if (needsFlip) {
            currentFrame.flip(true, false);
        }

        // Draw current animation frame
        batch.draw(currentFrame, renderX, renderY, renderWidth, renderHeight);
    }

    @Override
    public void performInnateSkill() {
        // Call overload dengan facing direction sebagai fallback
        Vector2 dir = new Vector2(isFacingRight ? 1 : -1, 0);
        Vector2 targetPos = position.cpy().add(dir.scl(100));
        performInnateSkill(targetPos);
    }

    @Override
    public void performInnateSkill(com.badlogic.gdx.math.Vector2 mousePos) {
        // Check cooldown
        if (skillTimer > 0) {
            System.out.println("[Insania] Mind Fracture on cooldown: " +
                    String.format("%.1f", skillTimer) + "s remaining");
            return;
        }

        // Activate Mind Fracture - AoE centered on Insania
        skillTimer = skillCooldown;
        mindFractureJustActivated = true; // Set flag untuk GameScreen
        circleDisplayTimer = CIRCLE_DISPLAY_DURATION; // Show circle for 0.5s

        System.out.println("[Insania] Mind Fracture activated! Applying Insanity in 300px radius!");
    }

    public boolean shouldShowMindFractureCircle() {
        return circleDisplayTimer > 0;
    }

    public boolean hasJustUsedMindFracture() {
        // Return true only once, then reset flag (consumed after use)
        if (mindFractureJustActivated) {
            mindFractureJustActivated = false; // Consume flag
            return true;
        }
        return false;
    }

    public float getSkillRadius() {
        return skillRadius;
    }

    // Getter untuk skill cooldown bar
    public float getSkillTimer() {
        return skillTimer;
    }

    public float getSkillCooldown() {
        return skillCooldown;
    }
}
