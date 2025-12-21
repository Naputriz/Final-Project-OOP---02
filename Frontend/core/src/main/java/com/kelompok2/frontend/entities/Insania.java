package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.skills.MindFractureSkill;
import com.kelompok2.frontend.strategies.MeleeAttackStrategy;

public class Insania extends GameCharacter {

    // Animation state system (State Pattern)
    private com.kelompok2.frontend.states.AnimationState currentState;
    private com.kelompok2.frontend.states.AnimationState idleState;
    private com.kelompok2.frontend.states.AnimationState runningState;
    private float stateTime; // Timer untuk tracking animation frame

    // Velocity tracking untuk state transitions
    private Vector2 previousPosition;
    private boolean isMoving;

    // Skill
    private MindFractureSkill innateSkill;

    public Insania(float x, float y) {
        super(x, y, 180f, 110f);

        // Set stats sesuai role Hybrid Attacker
        this.atk = 35f; // High ATK - primary damage
        this.arts = 25f; // Moderate Arts - untuk skill
        this.def = 5f; // Low Defence - glass cannon style

        // Initialize Skill
        this.innateSkill = new MindFractureSkill();

        // Initialize animation states dengan State Pattern
        // Idle: 4 frames in 2x2 grid
        idleState = new com.kelompok2.frontend.states.IdleState("Insania/pcgp-insania-idle.png", 2, 2, 4, 0.15f);
        // Running: 10 frames in 3x4 grid
        runningState = new com.kelompok2.frontend.states.RunningState("Insania/pcgp-insania-run_1.png", 3, 4, 10, 0.1f);

        // Start dengan idle state
        currentState = idleState;
        currentState.enter(this);
        stateTime = 0f;

        // Initialize movement tracking
        previousPosition = new Vector2(x, y);
        isMoving = false;

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
        // MeleeAttackStrategy - Mad Claw (Physical Scaling)
        // Increased range to 140f for better boss kiting
        this.attackStrategy = new MeleeAttackStrategy(140f, 100f, 1.0f, 0.3f);

        this.autoAttack = true;
        this.attackCooldown = 0.5f; // Medium cooldown untuk Mad Claw
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;
        innateSkill.update(delta);

        // Check movement untuk state transition
        checkMovementState();

        // Update current state
        currentState.update(this, delta);
    }

    private void checkMovementState() {
        // Bandingkan posisi sekarang dengan posisi sebelumnya
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
        // Get frame saat ini dari animation
        TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

        // Hitung posisi render
        float renderX = position.x;
        float renderY = position.y;

        // Set Color explicitly based on status
        batch.setColor(getRenderColor());

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

        // Reset color
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
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
        // Delegate to Skill
        innateSkill.activate(this, mousePos, null, null);
    }

    public boolean shouldShowMindFractureCircle() {
        return innateSkill.shouldShowCircle();
    }

    public float getSkillRadius() {
        return innateSkill.getRadius();
    }

    // Getter untuk skill cooldown bar
    public float getSkillTimer() {
        return innateSkill.getRemainingCooldown();
    }

    public float getSkillCooldown() {
        return innateSkill.getCooldown();
    }

    @Override
    public float getInnateSkillTimer() {
        return innateSkill.getRemainingCooldown();
    }

    @Override
    public float getInnateSkillCooldown() {
        return innateSkill.getCooldown();
    }

    @Override
    public String getAttackAnimationType() {
        return "scratch"; // Insania uses scratch animations
    }

    public long getMindFractureActivationId() {
        return innateSkill.getActivationId();
    }
}
