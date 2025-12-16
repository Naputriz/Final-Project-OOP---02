package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.strategies.MeleeAttackStrategy;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.states.AnimationState;
import com.kelompok2.frontend.states.IdleState;
import com.kelompok2.frontend.states.RunningState;

public class Blaze extends GameCharacter {

    // Hellfire Pillar skill tracking
    private float skillCooldown = 5f; // 5 seconds cooldown
    private float skillTimer = 0f;
    private Vector2 lastPillarPosition; // Track last pillar position
    private float pillarDuration = 2f; // Pillar active for 2 seconds
    private float pillarTimer = 0f;
    private boolean pillarActive = false;

    // State Pattern for animations
    private AnimationState currentState;
    private AnimationState idleState;
    private AnimationState runningState;
    private float stateTime = 0f;

    // Movement tracking for state transitions
    private Vector2 lastPosition;

    public Blaze(float x, float y) {
        super(x, y, 180f, 110f); // Moderate speed, Moderate HP

        // Set stats sesuai role Arts Attacker
        this.atk = 25f; // Moderate ATK
        this.arts = 40f; // High Arts - primary damage source
        this.def = 5f; // Low Defence

        // Initialize animation states
        // 4 columns × 23 rows = 92 frames total
        idleState = new IdleState("BlazeCharacterPlaceholder.png", 4, 23, 92, 0.1f);
        runningState = new RunningState("BlazeCharacterPlaceholder.png", 4, 23, 92, 0.08f); // Slightly faster for run

        currentState = idleState;
        currentState.enter(this);

        // Placeholder texture (will use animation frames instead)
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

        // Flame Punch - Hybrid damage melee attack
        this.attackStrategy = new MeleeAttackStrategy(100f, 90f, 1.0f, 0.25f);
        this.autoAttack = true;
        this.attackCooldown = 0.4f;

        lastPillarPosition = new Vector2();
        lastPosition = new Vector2(x, y);
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

        // Check for movement to transition states
        boolean isMoving = !position.epsilonEquals(lastPosition, 0.1f);

        if (isMoving && currentState != runningState) {
            // Transition to running
            currentState.exit(this);
            currentState = runningState;
            currentState.enter(this);
            stateTime = 0;
        } else if (!isMoving && currentState != idleState) {
            // Transition to idle
            currentState.exit(this);
            currentState = idleState;
            currentState.enter(this);
            stateTime = 0;
        }

        lastPosition.set(position);
    }

    @Override
    public void render(SpriteBatch batch) {
        // Get current frame from state
        TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

        // Flip sprite based on facing direction
        // Sprites default to facing LEFT, flip when facing RIGHT
        boolean needsFlip = (isFacingRight && !currentFrame.isFlipX()) || (!isFacingRight && currentFrame.isFlipX());
        if (needsFlip) {
            currentFrame.flip(true, false);
        }

        // Draw character sprite
        batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);

        // Render Hellfire Pillar if active (visual placeholder)
        if (pillarActive) {
            // TODO: Add actual pillar sprite/animation
            // For now, pillar damage is handled in GameScreen
        }
    }

    @Override
    public void performInnateSkill() {
        // Default behavior jika tidak ada target position
        Vector2 dir = new Vector2(isFacingRight ? 1 : -1, 0);
        Vector2 targetPos = position.cpy().add(dir.scl(150));
        performInnateSkill(targetPos);
    }

    @Override
    public void performInnateSkill(Vector2 mousePos) {
        // Check cooldown
        if (skillTimer > 0) {
            System.out.println("[Blaze] Hellfire Pillar on cooldown: " +
                    String.format("%.1f", skillTimer) + "s remaining");
            return;
        }

        // Activate Hellfire Pillar at cursor position
        skillTimer = skillCooldown;
        pillarActive = true;
        pillarTimer = pillarDuration;
        lastPillarPosition.set(mousePos);

        System.out.println("[Blaze] Hellfire Pillar summoned at: " + mousePos);
    }

    public boolean isPillarActive() {
        return pillarActive;
    }

    public Vector2 getPillarPosition() {
        return lastPillarPosition;
    }

    public float getPillarRadius() {
        return 40f; // 80px diameter = 40px radius
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
        return "slash"; // Blaze uses slash animations
    }

    @Override
    public float getAtk() {
        // For Flame Punch, return hybrid damage
        return (this.atk * 0.7f) + (this.arts * 0.3f);
    }
}
