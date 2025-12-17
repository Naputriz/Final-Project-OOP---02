package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.strategies.MeleeAttackStrategy;
import com.kelompok2.frontend.skills.Skill;
import com.kelompok2.frontend.skills.InfernoNovaSkill;
import com.kelompok2.frontend.states.AnimationState;
import com.kelompok2.frontend.states.IdleState;
import com.kelompok2.frontend.states.RunningState;

/**
 * Boss variant dari Blaze - The Flame Kaiser.
 * Stats lebih tinggi dari playable version, AI aggressive chase (seperti
 * Insania).
 */
public class BossBlaze extends Boss {

    // Hellfire Pillar skill tracking
    private float skillCooldown = 8f; // 8 seconds untuk boss
    private float skillTimer = 0f;
    private Vector2 lastPillarPosition;
    private float pillarDuration = 2f;
    private float pillarTimer = 0f;
    private boolean pillarActive = false;

    // State Pattern for animations
    private AnimationState currentState;
    private AnimationState idleState;
    private AnimationState runningState;
    private float stateTime = 0f;

    // Movement tracking for state transitions
    private Vector2 lastPosition;

    // Scaling dengan level player
    private int playerLevel;

    // Boss melee attacks storage
    private Array<MeleeAttack> meleeAttacks = new Array<>();

    // Random movement and attack for insanity effect
    private Vector2 randomDirection = new Vector2();
    private float directionChangeTimer = 0f;
    private Vector2 randomAttackTarget = new Vector2();

    public BossBlaze(float x, float y, GameCharacter player, int playerLevel) {
        super(x, y, 150f, 450f + (playerLevel * 45f), // HP scales: 450 + 45 per level, Speed: 150
                "Blaze", "The Flame Kaiser", player);

        this.playerLevel = playerLevel;

        // Stats lebih tinggi, // Stats scaling
        this.atk = 20f + (playerLevel * 2f); // ATK scales: 20 + 2 per level (reduced from 35 + 4)
        this.arts = 60f + (playerLevel * 5f); // Arts scales: 60 + 5 per level (primary damage)
        this.def = 5f + (playerLevel * 1f); // DEF scales: 5 + 1 per level

        // Initialize animation states (sama dengan playable version)
        idleState = new IdleState("BlazeCharacterPlaceholder.png", 4, 23, 92, 0.1f);
        runningState = new RunningState("BlazeCharacterPlaceholder.png", 4, 23, 92, 0.08f);

        currentState = idleState;
        currentState.enter(this);

        // Placeholder texture
        Texture placeholder = AssetManager.getInstance().loadTexture("BlazeCharacterPlaceholder.png");
        this.texture = placeholder;

        // Setup visual dan hitbox
        float visualSize = 128f;
        this.renderWidth = visualSize;
        this.renderHeight = visualSize;

        float hitboxWidth = visualSize / 3f;
        float hitboxHeight = visualSize * (2f / 3f);
        this.bounds.setSize(hitboxWidth, hitboxHeight);
        this.boundsOffsetX = (visualSize - hitboxWidth) / 2f;
        this.boundsOffsetY = 0;

        setPosition(x, y);

        // Attack strategy - Melee aggressive (Flame Punch)
        this.attackStrategy = new MeleeAttackStrategy(100f, 70f, 1.5f, 0.5f); // Range, width, damage mult, duration
        this.autoAttack = true;
        this.attackCooldown = 1.5f; // Attack every 1.5 seconds between attacks

        lastPillarPosition = new Vector2();
        lastPosition = new Vector2(x, y);

        System.out.println("[BossBlaze] Created with level scaling: Level " + playerLevel +
                ", HP: " + this.maxHp + ", ATK: " + this.atk + ", Arts: " + this.arts);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update state time
        stateTime += delta;
        currentState.update(this, delta);

        // Update skill cooldown
        if (skillTimer > 0) {
            skillTimer -= delta;
        }

        // Update pillar duration
        if (pillarActive) {
            pillarTimer -= delta;
            if (pillarTimer <= 0) {
                pillarActive = false;
                pillarTimer = 0;
            }
        }

        // Update melee attacks
        for (int i = meleeAttacks.size - 1; i >= 0; i--) {
            MeleeAttack attack = meleeAttacks.get(i);
            attack.update(delta);
            if (!attack.isActive()) {
                meleeAttacks.removeIndex(i);
            }
        }

        // Check for movement to transition states
        boolean isMoving = !position.epsilonEquals(lastPosition, 0.1f);

        if (isMoving && currentState != runningState) {
            currentState.exit(this);
            currentState = runningState;
            currentState.enter(this);
            stateTime = 0;
        } else if (!isMoving && currentState != idleState) {
            currentState.exit(this);
            currentState = idleState;
            currentState.enter(this);
            stateTime = 0;
        }

        lastPosition.set(position);
    }

    @Override
    public void updateAnimationsOnly(float delta) {
        // Only update animation time and state during cinematics
        stateTime += delta;
        currentState.update(this, delta);
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
                float randomOffsetX = (float) (Math.random() * 400 - 200);
                float randomOffsetY = (float) (Math.random() * 400 - 200);
                randomAttackTarget.set(
                        position.x + randomOffsetX,
                        position.y + randomOffsetY);

                directionChangeTimer = 0.5f;
            }

            // Move randomly
            move(randomDirection, delta);

            // Attack randomly
            if (canAttack()) {
                attack(randomAttackTarget, new Array<Projectile>(), meleeAttacks);
                resetAttackTimer();
            }

            // Use skill randomly
            if (skillTimer <= 0) {
                performInnateSkill(randomAttackTarget);
            }

            return; // Skip normal AI
        }

        // Normal AI: Aggressive chase AI - chase player seperti Insania (user request)
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

            // Use Hellfire Pillar periodically at player's position
            if (skillTimer <= 0) {
                Vector2 targetPos = new Vector2(targetCenterX, targetFeetY);
                performInnateSkill(targetPos);
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Get current frame from state
        TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

        // Flip sprite based on facing direction
        boolean needsFlip = (isFacingRight && !currentFrame.isFlipX()) || (!isFacingRight && currentFrame.isFlipX());
        if (needsFlip) {
            currentFrame.flip(true, false);
        }

        // Draw character sprite
        batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);
    }

    @Override
    public void performInnateSkill() {
        // Default - summon at own position
        performInnateSkill(new Vector2(position.x, position.y));
    }

    @Override
    public void performInnateSkill(Vector2 targetPos) {
        // Activate Hellfire Pillar at target position
        skillTimer = skillCooldown;
        pillarActive = true;
        pillarTimer = pillarDuration;
        lastPillarPosition.set(targetPos);

        System.out.println("[BossBlaze] Hellfire Pillar summoned at: " + targetPos);
    }

    public boolean isPillarActive() {
        return pillarActive;
    }

    public Vector2 getPillarPosition() {
        return lastPillarPosition;
    }

    public float getPillarRadius() {
        return 40f; // 80px diameter
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
        return "Inferno Nova";
    }

    @Override
    public Skill createUltimateSkill() {
        // Return new instance of Inferno Nova ultimate skill
        return new InfernoNovaSkill();
    }

    @Override
    public float getAtk() {
        // Flame Punch hybrid damage
        return (this.atk * 0.7f) + (this.arts * 0.3f);
    }

    public Array<MeleeAttack> getMeleeAttacks() {
        return meleeAttacks;
    }
}
