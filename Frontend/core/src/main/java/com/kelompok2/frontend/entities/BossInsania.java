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
    private long mindFractureActivationId = 0; // ✅ FIX: Unique ID per activation

    // Scaling dengan level player
    private int playerLevel;

    // Boss attacks storage (injected by GameFacade via setAttackArrays)
    private Array<MeleeAttack> meleeAttacks;
    private Array<Projectile> projectiles;

    // Random movement and attack for insanity effect
    private Vector2 randomDirection = new Vector2();
    private float directionChangeTimer = 0f;
    private Vector2 randomAttackTarget = new Vector2();

    public BossInsania(float x, float y, GameCharacter player, int playerLevel) {
        super(x, y, 150f, 1000f + (playerLevel * 100f), // HP scales: 1000 + 100 per level, Speed: 150
                "Insania", "The Chaos Kaiser", player);
        this.playerLevel = playerLevel;

        // Stats lebih tinggi dari playable version, scale dengan level
        this.atk = 15f + (playerLevel * 1.5f); // ATK scales: 15 + 1.5 per level (reduced from 25 + 2)
        this.arts = 25f + (playerLevel * 2.5f); // Arts scales: 25 + 2.5 per level (reduced from 35 + 3)
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
        this.attackStrategy = new MeleeAttackStrategy(100f, 70f, 1.0f, 0.5f); // Range, width, damage multiplier
                                                                              // (reduced from 1.5f), duration
        this.autoAttack = true;
        this.attackCooldown = 1.0f; //

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

        // Update melee attacks (only if injected by facade)
        if (meleeAttacks != null) {
            for (int i = meleeAttacks.size - 1; i >= 0; i--) {
                MeleeAttack attack = meleeAttacks.get(i);
                attack.update(delta);
                if (!attack.isActive()) {
                    meleeAttacks.removeIndex(i);
                }
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
            if (canAttack() && meleeAttacks != null && projectiles != null) {
                attack(randomAttackTarget, projectiles, meleeAttacks);
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
            if (canAttack() && meleeAttacks != null && projectiles != null) {
                // Use player's CENTER for attack direction (not feet) for correct animation
                // rotation
                float targetCenterY = target.getPosition().y + target.getVisualHeight() / 2;
                Vector2 attackTarget = new Vector2(targetCenterX, targetCenterY);
                attack(attackTarget, projectiles, meleeAttacks);
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

        // Set Color explicitly based on status
        batch.setColor(getRenderColor());

        // Draw current frame
        batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);

        // Reset color
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
    }

    @Override
    public void performInnateSkill() {
        // Mind Fracture - AoE insanity
        skillTimer = skillCooldown;
        mindFractureJustActivated = true;
        circleDisplayTimer = CIRCLE_DISPLAY_DURATION;
        mindFractureActivationId++; // ✅ FIX: Increment for single-hit tracking

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

    public long getMindFractureActivationId() {
        return mindFractureActivationId;
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

    @Override
    public void setAttackArrays(Array<MeleeAttack> meleeAttacks, Array<Projectile> projectiles) {
        this.meleeAttacks = meleeAttacks;
        this.projectiles = projectiles;
        System.out.println("[BossInsania] Attack arrays injected from GameFacade");
    }

    public Array<MeleeAttack> getMeleeAttacks() {
        return meleeAttacks;
    }
}
