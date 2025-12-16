package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.strategies.MeleeAttackStrategy;
import com.kelompok2.frontend.states.AnimationState;
import com.kelompok2.frontend.states.IdleState;
import com.kelompok2.frontend.states.RunningState;

public class Ryze extends GameCharacter {

    // Animation state system (State Pattern)
    private AnimationState currentState;
    private AnimationState idleState;
    private AnimationState runningState;
    private float stateTime; // Timer untuk tracking animation

    // Velocity tracking untuk state transitions
    private Vector2 previousPosition;
    private boolean isMoving;

    // Spectral Body skill tracking
    private float skillCooldown = 15f; // 15 seconds cooldown
    private float skillTimer = 0f;
    private boolean spectralBodyActive = false;
    private float spectralDuration = 3f; // 3 seconds invulnerability
    private float spectralTimer = 0f;

    public Ryze(float x, float y) {
        super(x, y, 200f, 100f);
        this.atk = 30f; // High ATK untuk melee
        this.arts = 10f; // Low Arts
        this.def = 5f; // Low Defence

        // Initialize animation states dengan State Pattern
        // Idle: 8 frames in 3x3 grid (bottom-right is empty)
        idleState = new IdleState("Ryze/pcgp-ryze-idle.png", 3, 3, 8, 0.1f);
        // Running: 10 frames in 3x4 grid (last 2 cells are empty)
        runningState = new RunningState("Ryze/pcgp-ryze-run.png", 3, 4, 10, 0.1f);

        // Start dengan idle state
        currentState = idleState;
        currentState.enter(this);
        stateTime = 0f;

        // Initialize movement tracking
        previousPosition = new Vector2(x, y);
        isMoving = false;

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

        this.attackStrategy = new MeleeAttackStrategy(150f, 120f, 1.0f, 0.2f);
        this.autoAttack = false;
        this.attackCooldown = 0.4f;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;

        // Update skill cooldown
        if (skillTimer > 0) {
            skillTimer -= delta;
        }

        // Update Spectral Body duration
        if (spectralBodyActive) {
            spectralTimer -= delta;
            if (spectralTimer <= 0) {
                spectralBodyActive = false;
                spectralTimer = 0;
                System.out.println("[Ryze] Spectral Body ended");
            }
        }

        // Check movement untuk state transition
        checkMovementState();

        // Update current state
        currentState.update(this, delta);
    }

    private void checkMovementState() {
        // Bandingkan posisi sekarang dengan posisi sebelumnya
        boolean wasMoving = isMoving;
        isMoving = !position.epsilonEquals(previousPosition, 0.1f);

        // Transition states
        if (isMoving && currentState == idleState) {
            // Idle -> Running
            currentState.exit(this);
            currentState = runningState;
            currentState.enter(this);
            stateTime = 0f; // Reset animation time
        } else if (!isMoving && currentState == runningState) {
            // Running -> Idle
            currentState.exit(this);
            currentState = idleState;
            currentState.enter(this);
            stateTime = 0f; // Reset animation time
        }

        // Update previous position
        previousPosition.set(position);
    }

    @Override
    public void render(SpriteBatch batch) {
        // Get current animation frame from state
        TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

        // Apply transparency saat Spectral Body aktif
        if (spectralBodyActive) {
            batch.setColor(1f, 1f, 1f, 0.5f);
        }

        // Flip sprite based on facing direction
        // Sprite awalnya menghadap KIRI, jadi:
        // - Jika isFacingRight = true dan sprite belum flip -> FLIP
        // - Jika isFacingRight = false dan sprite sudah flip -> FLIP BACK
        boolean needsFlip = (isFacingRight && !currentFrame.isFlipX()) || (!isFacingRight && currentFrame.isFlipX());
        if (needsFlip) {
            currentFrame.flip(true, false);
        }

        // Draw current frame
        batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);

        // Reset color
        if (spectralBodyActive) {
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void takeDamage(float damage) {
        if (spectralBodyActive) {
            System.out.println("[Ryze] Attack passed through! (Spectral Body)");
            return; // Ignore damage completely
        }
        super.takeDamage(damage);
    }

    public boolean isInvulnerable() {
        return spectralBodyActive;
    }

    @Override
    public void performInnateSkill() {
        // Check cooldown
        if (skillTimer > 0) {
            System.out.println("[Ryze] Spectral Body on cooldown: " +
                    String.format("%.1f", skillTimer) + "s remaining");
            return;
        }

        // Activate Spectral Body
        spectralBodyActive = true;
        spectralTimer = spectralDuration;
        skillTimer = skillCooldown;

        System.out.println("[Ryze] Spectral Body activated! Invulnerable for 3 seconds!");
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
        return "slash"; // Ryze uses slash animations
    }
}
