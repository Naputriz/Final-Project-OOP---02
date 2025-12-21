package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.strategies.RangedAttackStrategy;
import com.kelompok2.frontend.skills.Skill;
import com.kelompok2.frontend.skills.FrozenApocalypseSkill;
import com.badlogic.gdx.graphics.Color;

public class BossIsolde extends Boss {

    // Animation system
    private Animation<TextureRegion> idleAnimation;
    private float stateTime;

    // Glacial Breath skill cooldown
    private float skillCooldown = 12f; // 12 seconds untuk boss
    private float skillTimer = 0f;

    // Active Glacial Breath attacks
    private Array<GlacialBreath> glacialBreaths;

    // Scaling dengan level player
    private int playerLevel;

    // Kiting AI variables
    private float preferredDistance = 350f; // Maintain 350px distance dari player
    private float postAttackSlowTimer = 0f; // Slow after attacking
    private static final float POST_ATTACK_SLOW_DURATION = 0.5f;

    // Boss projectiles storage (injected by GameFacade via setAttackArrays)
    private Array<Projectile> projectiles;
    private Array<MeleeAttack> meleeAttacks; // Not used but required by interface

    // Random movement and attack for insanity effect
    private Vector2 randomDirection = new Vector2();
    private float directionChangeTimer = 0f;
    private Vector2 randomAttackTarget = new Vector2();

    public BossIsolde(float x, float y, GameCharacter player, int playerLevel) {
        super(x, y, 120f, 800f + (playerLevel * 80f), // HP scales: 800 + 80 per level, Base speed: 120
                "Isolde", "The Frost Kaiser", player);
        this.playerLevel = playerLevel;

        // Stats scaling (after super call)
        this.atk = 6f + (playerLevel * 0.6f); // ATK: 6 + 0.6 per level (reduced from 8 + 0.8)
        this.def = 25f + (playerLevel * 2.5f); // DEF: 25 + 2.5 per level
        this.arts = 12f + (playerLevel * 1.2f); // ARTS: 12 + 1.2 per level (reduced from 15 + 1.5)

        // Load spritesheet (sama dengan playable version)
        Texture spritesheet = AssetManager.getInstance().loadTexture("FrostPlaceholderSprite.png");

        // Split spritesheet (10x10)
        int FRAME_COLS = 10;
        int FRAME_ROWS = 10;
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
        idleAnimation = new Animation<>(0.1f, idleFrames);
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        stateTime = 0f;
        this.texture = spritesheet;

        // Ukuran visual dan hitbox
        float visualSize = 128f;
        this.renderWidth = visualSize;
        this.renderHeight = visualSize;

        float hitboxWidth = visualSize / 3f;
        float hitboxHeight = visualSize * (2f / 3f);
        this.bounds.setSize(hitboxWidth, hitboxHeight);

        this.boundsOffsetX = (visualSize - hitboxWidth) / 2f;
        this.boundsOffsetY = 0;

        setPosition(x, y);

        this.attackStrategy = new RangedAttackStrategy(0.7f, new Color(0.5f, 0.8f, 1f, 1f), 400f);
        this.autoAttack = true;
        this.attackCooldown = 1.0f;

        glacialBreaths = new Array<>();

        System.out.println("[BossIsolde] Created with level scaling: Level " + playerLevel +
                ", HP: " + this.maxHp + ", ATK: " + this.atk + ", Arts: " + this.arts);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;

        // Update skill cooldown
        if (skillTimer > 0) {
            skillTimer -= delta;
        }

        // Update post-attack slow timer
        if (postAttackSlowTimer > 0) {
            postAttackSlowTimer -= delta;
        }

        // Update active glacial breaths
        for (int i = glacialBreaths.size - 1; i >= 0; i--) {
            GlacialBreath gb = glacialBreaths.get(i);
            gb.update(delta);
            if (!gb.isActive()) {
                glacialBreaths.removeIndex(i);
            }
        }

        // Projectiles are updated by GameFacade's main loop, tidak perlu update di sini
        // Removing duplicate update to fix 2x speed bug
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
                float randomOffsetX = (float) (Math.random() * 600 - 300); // wider range for ranged
                float randomOffsetY = (float) (Math.random() * 600 - 300);
                randomAttackTarget.set(
                        position.x + randomOffsetX,
                        position.y + randomOffsetY);

                directionChangeTimer = 0.5f;
            }

            // Move randomly
            move(randomDirection, delta);

            // Shoot icicles randomly
            if (canAttack() && projectiles != null && meleeAttacks != null) {
                attack(randomAttackTarget, projectiles, meleeAttacks);
                resetAttackTimer();
            }

            // Use Glacial Breath randomly
            if (skillTimer <= 0) {
                performInnateSkill(randomAttackTarget);
            }

            return; // Skip normal AI
        }

        // Normal AI: Kiting AI - maintain distance, shoot projectiles, use Glacial
        // Breath
        if (target != null && !target.isDead()) {
            // Calculate distance to target
            float targetCenterX = target.getPosition().x + target.getVisualWidth() / 2;
            float targetFeetY = target.getPosition().y;

            float myCenterX = this.position.x + getVisualWidth() / 2;
            float myFeetY = this.position.y;

            float dx = targetCenterX - myCenterX;
            float dy = targetFeetY - myFeetY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            Vector2 direction = new Vector2(dx, dy).nor();

            // Kiting behavior: move away if too close, move closer if too far
            if (distance < preferredDistance) {
                // Too close - retreat (move away from player) at reduced speed
                Vector2 retreatDir = new Vector2(-direction.x, -direction.y);
                // Temporarily reduce speed for retreat
                float originalSpeed = this.speed;
                this.speed = 80f; // Reduced from 120f for balance
                move(retreatDir, delta);
                this.speed = originalSpeed; // Restore speed
            } else if (distance > preferredDistance + 100f) {
                // Too far - approach (move towards player)
                Vector2 approachDir = new Vector2(direction.x * 0.5f, direction.y * 0.5f); // Slower approach
                move(approachDir, delta);
            }
            // If within preferred range, stay still and attack

            // Shoot icicles at player when in range
            if (distance <= preferredDistance + 200f && canAttack() && projectiles != null && meleeAttacks != null) {
                Vector2 attackTarget = new Vector2(targetCenterX, targetFeetY);
                attack(attackTarget, projectiles, meleeAttacks);
                resetAttackTimer();
                // Apply post-attack slow
                postAttackSlowTimer = POST_ATTACK_SLOW_DURATION;
            }

            // Use Glacial Breath when player gets close
            if (skillTimer <= 0 && distance < 400f) {
                Vector2 targetPos = new Vector2(targetCenterX, targetFeetY);
                performInnateSkill(targetPos);
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Get current animation frame
        TextureRegion currentFrame = idleAnimation.getKeyFrame(stateTime);

        // Flip sprite based on facing direction
        if (!isFacingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (isFacingRight && currentFrame.isFlipX()) {
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
        // Default - shoot towards target
        if (target != null) {
            Vector2 targetPos = new Vector2(
                    target.getPosition().x + target.getVisualWidth() / 2,
                    target.getPosition().y + target.getVisualHeight() / 2);
            performInnateSkill(targetPos);
        }
    }

    @Override
    public void performInnateSkill(Vector2 targetPos) {
        // Check cooldown
        if (skillTimer > 0) {
            return;
        }

        // Calculate direction dari boss ke target
        float myCenterX = position.x + getVisualWidth() / 2;
        float myCenterY = position.y + getVisualHeight() / 2;
        Vector2 direction = new Vector2(
                targetPos.x - myCenterX,
                targetPos.y - myCenterY).nor();

        // Calculate damage (Arts scaling × 0.5 untuk boss)
        float damage = this.arts * 0.5f;

        // Create Glacial Breath cone attack
        GlacialBreath glacialBreath = new GlacialBreath(this, direction, damage, 0.5f);
        glacialBreaths.add(glacialBreath);

        // Reset cooldown
        skillTimer = skillCooldown;

        System.out.println("[BossIsolde] Glacial Breath activated! Damage: " + damage);
    }

    public Array<GlacialBreath> getGlacialBreaths() {
        return glacialBreaths;
    }

    @Override
    public float getAtk() {
        // Icicle Shard uses ARTS scaling (heavily reduced to prevent one-shots)
        return this.arts * 0.3f; // Reduced from 0.6f
    }

    @Override
    public float getArts() {
        // BossIsolde is ARTS-based, so insanity buffs ARTS instead of ATK
        if (isInsane) {
            return super.getArts() * 1.5f; // 50% ARTS increase when insane
        }
        return super.getArts();
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
        return "slash";
    }

    @Override
    public String getUltimateSkillName() {
        return "Frozen Apocalypse";
    }

    @Override
    public Skill createUltimateSkill() {
        // Return new instance of Frozen Apocalypse ultimate skill
        return new FrozenApocalypseSkill();
    }

    @Override
    public void setAttackArrays(Array<MeleeAttack> meleeAttacks, Array<Projectile> projectiles) {
        this.meleeAttacks = meleeAttacks;
        this.projectiles = projectiles;
        System.out.println("[BossIsolde] Attack arrays injected from GameFacade");
    }

    public Array<Projectile> getProjectiles() {
        return projectiles;
    }
}
