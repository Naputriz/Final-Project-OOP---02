package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.strategies.RangedAttackStrategy;

public class Isolde extends GameCharacter {

    // Animation system untuk spritesheet
    private Animation<TextureRegion> idleAnimation;
    private float stateTime; // Timer untuk tracking animation frame

    // Glacial Breath skill cooldown
    private float skillCooldown = 10f; // 10 seconds cooldown
    private float skillTimer = 0f;

    // Active Glacial Breath attacks
    private Array<GlacialBreath> glacialBreaths;

    public Isolde(float x, float y) {
        super(x, y, 180f, 120f);

        // Set stats sesuai role Arts Attacker
        this.atk = 20f; // Ranged ATK (balanced with Blaze)
        this.arts = 55f; // High Arts (main damage stat, similar to Insania)
        this.def = 15f; // Moderate Defence (ranged glass cannon)

        // Placeholder (Ini karakter Arknight cok mati kita kalo pake ini di versi
        // akhir)
        Texture spritesheet = AssetManager.getInstance().loadTexture("FrostPlaceholderSprite.png");

        // Split spritesheet menjadi individual frames (10 kolom x 10 baris)
        int FRAME_COLS = 10;
        int FRAME_ROWS = 10;
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
        idleAnimation = new Animation<>(0.1f, idleFrames);
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Initialize state time
        stateTime = 0f;

        // Set texture ke frame pertama untuk bounds calculation
        this.texture = spritesheet;

        // Ukuran visual dan hitbox
        float visualSize = 128f; // Reduced size for better gameplay visibility
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

        // Ranged Attack Strategy - Shoot icicles (projectile dengan Arts scaling)
        // RangedAttackStrategy(damageMultiplier, color) - Light blue untuk frost theme
        // Damage lowered to 0.5x untuk testing freeze mechanics
        this.attackStrategy = new RangedAttackStrategy(0.5f, new Color(0.5f, 0.8f, 1f, 1f)); // Light blue/cyan

        // Auto attack (hold to shoot continuously)
        this.autoAttack = true;
        this.attackCooldown = 0.8f; // Cooldown 0.8 detik untuk ranged

        // Initialize glacial breaths array
        glacialBreaths = new Array<>();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;

        // Update skill cooldown
        if (skillTimer > 0) {
            skillTimer -= delta;
        }

        // Update active glacial breaths
        for (int i = glacialBreaths.size - 1; i >= 0; i--) {
            GlacialBreath gb = glacialBreaths.get(i);
            gb.update(delta);
            if (!gb.isActive()) {
                glacialBreaths.removeIndex(i);
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Get frame saat ini dari animation
        TextureRegion currentFrame = idleAnimation.getKeyFrame(stateTime);

        // Hitung posisi render (position sudah correct, tidak perlu offset lagi)
        float renderX = position.x;
        float renderY = position.y;

        // Flip sprite jika menghadap kiri
        if (!isFacingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (isFacingRight && currentFrame.isFlipX()) {
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
    public void performInnateSkill(Vector2 mousePos) {
        // Check cooldown
        if (skillTimer > 0) {
            System.out.println("[Isolde] Glacial Breath on cooldown: " +
                    String.format("%.1f", skillTimer) + "s remaining");
            return;
        }

        // Calculate direction dari posisi player ke mouse cursor
        float playerCenterX = position.x + getVisualWidth() / 2;
        float playerCenterY = position.y + getVisualHeight() / 2;
        Vector2 direction = new Vector2(
                mousePos.x - playerCenterX,
                mousePos.y - playerCenterY).nor();

        // Calculate damage (Arts scaling × 0.3 - heavily reduced for freeze testing)
        // Damage: 40 Arts × 0.3 = 12 damage (won't one-shot with basic attack combo)
        // Bakal dinaikin di versi akhir
        float damage = this.arts * 0.3f;

        // Create Glacial Breath cone attack (0.5s duration) aiming toward mouse
        GlacialBreath glacialBreath = new GlacialBreath(this, direction, damage, 0.5f);
        glacialBreaths.add(glacialBreath);

        // Reset cooldown
        skillTimer = skillCooldown;

        System.out.println("[Isolde] Glacial Breath activated! Damage: " + damage +
                " (aimed at mouse)");
    }

    public Array<GlacialBreath> getGlacialBreaths() {
        return glacialBreaths;
    }

    // Getter untuk skill cooldown bar
    public float getSkillTimer() {
        return skillTimer;
    }

    public float getSkillCooldown() {
        return skillCooldown;
    }

    @Override
    public float getInnateSkillTimer() {
        return skillTimer;
    }

    @Override
    public float getInnateSkillCooldown() {
        return skillCooldown;
    }

    @Override
    public String getAttackAnimationType() {
        return "slash"; // Isolde uses slash animations
    }
}
