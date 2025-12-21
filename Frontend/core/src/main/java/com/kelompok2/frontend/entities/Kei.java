package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.skills.PhantomHazeSkill;
import com.kelompok2.frontend.strategies.RangedAttackStrategy;

public class Kei extends GameCharacter {

    // Animation state system (State Pattern)
    private com.kelompok2.frontend.states.AnimationState currentState;
    private com.kelompok2.frontend.states.AnimationState idleState;
    private com.kelompok2.frontend.states.AnimationState runningState;
    private float stateTime; // Timer untuk tracking animation frame

    // Velocity tracking untuk state transitions
    private Vector2 previousPosition;
    private boolean isMoving;

    // Skill
    private PhantomHazeSkill innateSkill;

    public Kei(float x, float y) {
        super(x, y, 190f, 95f);

        // Set stats - Ranged Arts Attacker / Hunter
        this.atk = 20f;
        this.arts = 45f;
        this.def = 10f;

        // Initialize Skill
        this.innateSkill = new PhantomHazeSkill();

        // Initialize animation states
        // Using FrostPlaceholderSprite.png (10x10) as placeholder
        idleState = new com.kelompok2.frontend.states.IdleState("FrostPlaceholderSprite.png", 10, 10, 100, 0.1f);
        runningState = new com.kelompok2.frontend.states.RunningState("FrostPlaceholderSprite.png", 10, 10, 100, 0.1f);

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

        // Hitbox lebih kecil dari visual
        float hitboxWidth = visualSize / 3f;
        float hitboxHeight = visualSize * (2f / 3f);
        this.bounds.setSize(hitboxWidth, hitboxHeight);
        this.boundsOffsetX = (visualSize - hitboxWidth) / 2f;
        this.boundsOffsetY = 0;

        setPosition(x, y);

        // Ranged Attack Strategy - Hallucina Shards (Magenta)
        this.attackStrategy = new RangedAttackStrategy(0.8f, new Color(1f, 0.4f, 1f, 1f));

        this.autoAttack = true;
        this.attackCooldown = 0.6f;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;
        innateSkill.update(delta);

        checkMovementState();
        currentState.update(this, delta);
    }

    private void checkMovementState() {
        isMoving = !position.epsilonEquals(previousPosition, 0.1f);

        if (isMoving && currentState == idleState) {
            currentState.exit(this);
            currentState = runningState;
            currentState.enter(this);
            stateTime = 0f;
        } else if (!isMoving && currentState == runningState) {
            currentState.exit(this);
            currentState = idleState;
            currentState.enter(this);
            stateTime = 0f;
        }

        previousPosition.set(position);
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

        boolean needsFlip = (isFacingRight && !currentFrame.isFlipX()) || (!isFacingRight && currentFrame.isFlipX());
        if (needsFlip) {
            currentFrame.flip(true, false);
        }

        // Tint Kei Magenta/Purple to distinguish from Isolde
        batch.setColor(1f, 0.4f, 1f, 1f);
        batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);
        batch.setColor(Color.WHITE); // Reset
    }

    @Override
    public void performInnateSkill() {
        Vector2 dir = new Vector2(isFacingRight ? 1 : -1, 0);
        Vector2 targetPos = position.cpy().add(dir.scl(100));
        performInnateSkill(targetPos);
    }

    @Override
    public void performInnateSkill(Vector2 mousePos) {
        innateSkill.activate(this, mousePos, null, null);
    }

    public boolean shouldShowPhantomHazeCircle() {
        return innateSkill.shouldShowCircle();
    }

    public float getSkillRadius() {
        return innateSkill.getRadius();
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
        return "slash"; // Placeholder
    }

    public long getPhantomHazeActivationId() {
        return innateSkill.getActivationId();
    }
}
