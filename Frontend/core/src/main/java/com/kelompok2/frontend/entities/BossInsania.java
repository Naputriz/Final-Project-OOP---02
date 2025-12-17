package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.strategies.MeleeAttackStrategy;
import com.kelompok2.frontend.skills.Skill;
import com.kelompok2.frontend.skills.InsanityBurstSkill;

/**
 * Boss variant dari Insania - The Chaos Kaiser.
 * Stats lebih tinggi dari playable version, AI aggressive chase.
 */
public class BossInsania extends Boss {

    // Animation system untuk spritesheet
    private Animation<TextureRegion> idleAnimation;
    private float stateTime;

    // Mind Fracture skill tracking
    private float skillCooldown = 15f; // 15 seconds cooldown untuk boss
    private float skillTimer = 0f;
    private float skillRadius = 300f;
    private boolean mindFractureJustActivated = false;
    private float circleDisplayTimer = 0f;
    private static final float CIRCLE_DISPLAY_DURATION = 0.5f;

    // Scaling dengan level player
    private int playerLevel;

    // Boss attacks storage
    private Array<MeleeAttack> meleeAttacks = new Array<>();

    // Random movement and attack for insanity effect
    private Vector2 randomDirection = new Vector2();
    private float directionChangeTimer = 0f;
    private Vector2 randomAttackTarget = new Vector2();

    public BossInsania(float x, float y, GameCharacter player, int playerLevel) {
        super(x, y, 150f, 500f + (playerLevel * 50f), // HP scales: 500 + 50 per level, Speed: 150
                "Insania", "The Chaos Kaiser", player);
        this.playerLevel = playerLevel;

        // Stats lebih tinggi dari playable version, scale dengan level
        this.atk = 25f + (playerLevel * 2f); // ATK scales: 25 + 2 per level (reduced from 50 + 5)
        this.arts = 35f + (playerLevel * 3f); // Arts scales: 35 + 3 per level
        this.def = 10f + (playerLevel * 2f); // DEF scales: 10 + 2 per level

        // Load spritesheet yang sama dengan playable Insania
        Texture spritesheet = AssetManager.getInstance().loadTexture("Insania/pcgp-insania-idle.png");

        // Split spritesheet (2x2 = 4 frames)
        int FRAME_COLS = 2;
        int FRAME_ROWS = 2;
        TextureRegion[][] tmp = TextureRegion.split(
                spritesheet,
                spritesheet.getWidth() / FRAME_COLS,
                spritesheet.getHeight() / FRAME_ROWS);

        // Convert 2D array ke 1D
        TextureRegion[] idleFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                idleFrames[index++] = tmp[i][j];
            }
        }

        // Create idle animation
        idleAnimation = new Animation<>(0.15f, idleFrames);
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        stateTime = 0f;
        this.texture = spritesheet;

        // Ukuran visual dan hitbox (sama dengan playable version)
        float visualSize = 128f;
        this.renderWidth = visualSize;
        this.renderHeight = visualSize;

        float hitboxWidth = visualSize / 3f;
        float hitboxHeight = visualSize * (2f / 3f);
        this.bounds.setSize(hitboxWidth, hitboxHeight);

        this.boundsOffsetX = (visualSize - hitboxWidth) / 2f;
        this.boundsOffsetY = 0;

        setPosition(x, y);

        // Set attack strategy - melee with scratch animation (boss is aggressive)
        this.attackStrategy = new MeleeAttackStrategy(100f, 70f, 1.5f, 0.5f); // Range, width, damage multiplier,
                                                                              // duration
        this.autoAttack = true;
        this.attackCooldown = 1.5f; // Attack every 1.5 seconds between attacks

        System.out.println("[BossInsania] Created with level scaling: Level " + playerLevel +
                ", HP: " + this.maxHp + ", ATK: " + this.atk);
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

        // Update melee attacks
        for (int i = meleeAttacks.size - 1; i >= 0; i--) {
            MeleeAttack attack = meleeAttacks.get(i);
            attack.update(delta);
            if (!attack.isActive()) {
                meleeAttacks.removeIndex(i);
            }
        }
    }

    @Override
    public void updateAnimationsOnly(float delta) {
        // Only update animation time during cinematics
        stateTime += delta;
    }

    @Override
    public void updateAI(float delta) {
        // Insanity behavior - random movement and attacks
        if (isInsane) {
            directionChangeTimer -= delta;
            if (directionChangeTimer <= 0) {
                // Generate new random direction
                float angle = (float) (Math.random() * Math.PI * 2);
                randomDirection.set(
                        (float) Math.cos(angle),
                        (float) Math.sin(angle)).nor();

                // Generate random attack target near boss
                float randomOffsetX = (float) (Math.random() * 400 - 200); // -200 to +200
                float randomOffsetY = (float) (Math.random() * 400 - 200);
                randomAttackTarget.set(
                        position.x + randomOffsetX,
                        position.y + randomOffsetY);

                directionChangeTimer = 0.5f; // Change every 0.5 seconds
            }

            // Move randomly
            move(randomDirection, delta);

            // Attack randomly
            if (canAttack()) {
                attack(randomAttackTarget, new Array<Projectile>(), meleeAttacks);
                resetAttackTimer();
            }

            return; // Skip normal AI
        }

        // Normal AI: Aggressive chase AI - always chase player
        if (target != null && !target.isDead()) {
            // Calculate direction to target
            float targetCenterX = target.getPosition().x + target.getVisualWidth() / 2;
            float targetFeetY = target.getPosition().y;

            float myCenterX = this.position.x + getVisualWidth() / 2;
            float myFeetY = this.position.y;

            Vector2 direction = new Vector2(
                    targetCenterX - myCenterX,
                    targetFeetY - myFeetY).nor();

            move(direction, delta);

            // Update facing direction
            setFacingRight(direction.x > 0);

            // Attack player continuously (no range check - like Isolde)
            if (canAttack()) {
                // Use player's CENTER for attack direction (not feet) for correct animation
                // rotation
                float targetCenterY = target.getPosition().y + target.getVisualHeight() / 2;
                Vector2 attackTarget = new Vector2(targetCenterX, targetCenterY);
                attack(attackTarget, new Array<Projectile>(), meleeAttacks);
                resetAttackTimer();
            }

            // Use Mind Fracture periodically
            if (skillTimer <= 0) {
                performInnateSkill();
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Get current animation frame
        TextureRegion currentFrame = idleAnimation.getKeyFrame(stateTime);

        // Flip sprite based on facing direction
        boolean needsFlip = (isFacingRight && !currentFrame.isFlipX()) || (!isFacingRight && currentFrame.isFlipX());
        if (needsFlip) {
            currentFrame.flip(true, false);
        }

        // Draw current frame
        batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);
    }

    @Override
    public void performInnateSkill() {
        // Mind Fracture - AoE insanity
        skillTimer = skillCooldown;
        mindFractureJustActivated = true;
        circleDisplayTimer = CIRCLE_DISPLAY_DURATION;

        System.out.println("[BossInsania] Mind Fracture activated!");
    }

    @Override
    public void performInnateSkill(Vector2 mousePos) {
        // Boss doesn't use mouse targeting
        performInnateSkill();
    }

    public boolean shouldShowMindFractureCircle() {
        return circleDisplayTimer > 0;
    }

    public boolean hasJustUsedMindFracture() {
        if (mindFractureJustActivated) {
            mindFractureJustActivated = false;
            return true;
        }
        return false;
    }

    public float getSkillRadius() {
        return skillRadius;
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
        return "scratch";
    }

    @Override
    public String getUltimateSkillName() {
        return "Insanity Burst";
    }

    @Override
    public Skill createUltimateSkill() {
        // Return new instance of Insanity Burst ultimate skill
        return new InsanityBurstSkill();
    }

    public Array<MeleeAttack> getMeleeAttacks() {
        return meleeAttacks;
    }
}
