package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kelompok2.frontend.strategies.RangedAttackStrategy;
import com.kelompok2.frontend.skills.VerdantDomainSkill;

public class Aelita extends GameCharacter {

    private VerdantDomainSkill verdantDomain;

    float atkBuffMultiplier = 1.0f;
    float artsBuffMultiplier = 1.0f;

    // Animation state system
    private com.kelompok2.frontend.states.AnimationState currentState;
    private com.kelompok2.frontend.states.AnimationState idleState;
    private com.kelompok2.frontend.states.AnimationState runState;
    private float stateTime;

    // Pulse fields for tracking movement
    private Vector2 previousPosition;
    private boolean isMoving;

    public Aelita(float x, float y) {
        super(x, y, 170f, 140f); // Speed: 170, HP: 140
        this.atk = 15f;
        this.arts = 30f;
        this.def = 20f;

        // Initialize Animation States
        // Idle: 2x2 grid, 4 frames
        idleState = new com.kelompok2.frontend.states.IdleState("Aelita/pcgp-aelita.png", 2, 2, 4, 0.15f);

        // Run: 2x3 grid, 6 frames
        runState = new com.kelompok2.frontend.states.RunningState("Aelita/pcgp-aelita-run.png", 2, 3, 6, 0.1f);

        // Start with idle state
        currentState = idleState;
        currentState.enter(this);
        stateTime = 0f;

        // Initialize movement tracking
        previousPosition = new Vector2(x, y);
        isMoving = false;

        // Note: this.texture is no longer used for rendering with AnimationState system
        // keeping it null or placeholder if needed for other systems, but usually safe
        // to ignore

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

        // Ranged attack strategy with GREEN projectiles
        this.attackStrategy = new RangedAttackStrategy(
                1.0f, // damage multiplier (Arts × 1.0)
                Color.GREEN // ✨ Green projectiles for healer aesthetic
        );
        this.attackCooldown = 0.7f;
        this.autoAttack = true; // Click-to-attack

        // Create innate skill
        this.verdantDomain = new VerdantDomainSkill();

        System.out.println("[Aelita] Healer initialized - HP: " + this.maxHp +
                ", ATK: " + this.atk + ", Arts: " + this.arts);
    }

    @Override
    public void performInnateSkill() {
        // Verdant Domain activates at player's center position
        Vector2 playerCenter = new Vector2(
                position.x + getVisualWidth() / 2,
                position.y + getVisualHeight() / 2);
        performInnateSkill(playerCenter);
    }

    @Override
    public void performInnateSkill(Vector2 targetPos) {
        verdantDomain.activate(this, targetPos, new Array<>(), new Array<>());
    }

    @Override
    public float getInnateSkillTimer() {
        return verdantDomain.getRemainingCooldown();
    }

    @Override
    public float getInnateSkillCooldown() {
        return verdantDomain.getCooldown();
    }

    @Override
    public String getAttackAnimationType() {
        return "slash"; // Default animation type
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;
        verdantDomain.update(delta, this); // Pass player reference for healing

        // Check movement for state transition
        checkMovementState();

        // Update current state (animation)
        currentState.update(this, delta);
    }

    private void checkMovementState() {
        // Bandingkan posisi sekarang dengan posisi sebelumnya
        isMoving = !position.epsilonEquals(previousPosition, 0.1f);

        // Transition states
        if (isMoving && currentState == idleState) {
            // Idle -> Run
            currentState.exit(this);
            currentState = runState;
            currentState.enter(this);
            stateTime = 0f;
        } else if (!isMoving && currentState == runState) {
            // Run -> Idle
            currentState.exit(this);
            currentState = idleState;
            currentState.enter(this);
            stateTime = 0f;
        }

        // Update previous position
        previousPosition.set(position);
    }

    @Override
    public void render(SpriteBatch batch) {
        // Get current animation frame from state
        com.badlogic.gdx.graphics.g2d.TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

        // Create a copy or use logic to determine flip without modifying original if
        // shared (usually distinct instances though)
        // TextureRegion frameToDraw = new TextureRegion(currentFrame);
        // Actually, AnimationState returns frames from array, so modifying them (flip)
        // modifies the cached frame.
        // We should handle flip carefully.
        // Ryze implementation flips the frame object itself back and forth.

        // Flip logic (standard for all characters)
        // Asset faces LEFT by default.
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

    // Override getAtk to apply Verdant Domain buff
    @Override
    public float getAtk() {
        return super.getAtk() * atkBuffMultiplier;
    }

    // Override getArts to apply Verdant Domain buff
    @Override
    public float getArts() {
        return super.getArts() * artsBuffMultiplier;
    }

    // Apply Verdant Domain buff (called by GameScreen when player is in zone)
    public void applyVerdantBuff(float atkMult, float artsMult) {
        this.atkBuffMultiplier = atkMult;
        this.artsBuffMultiplier = artsMult;
    }

    // Clear Verdant Domain buff (called when player leaves zone or zone expires)
    public void clearVerdantBuff() {
        this.atkBuffMultiplier = 1.0f;
        this.artsBuffMultiplier = 1.0f;
    }

    // Getter for Verdant Domain skill (used by GameScreen for zone rendering and
    // logic)
    public VerdantDomainSkill getVerdantDomain() {
        return verdantDomain;
    }

    // Getters for buff state (used for debug logging)
    public float getAtkBuffMultiplier() {
        return atkBuffMultiplier;
    }

    public float getArtsBuffMultiplier() {
        return artsBuffMultiplier;
    }
}
