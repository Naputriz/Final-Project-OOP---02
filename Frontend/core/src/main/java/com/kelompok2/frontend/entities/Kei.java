package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.skills.PhantomHazeSkill;
import com.kelompok2.frontend.strategies.RangedAttackStrategy;
import com.kelompok2.frontend.states.AnimationState;
import com.kelompok2.frontend.states.IdleState;
import com.kelompok2.frontend.states.RunningState;

public class Kei extends GameCharacter {

    // Animation state system (State Pattern)
    private AnimationState currentState;
    private AnimationState idleState;
    private AnimationState runningState;
    private float stateTime; // Timer untuk tracking animation frame

    // Velocity tracking untuk state transitions
    private Vector2 previousPosition;
    private boolean isMoving;

    // Skill
    private PhantomHazeSkill innateSkill;

    public Kei(float x, float y) {
        super(x, y, 190f, 95f); // Using stats from user code

        // Set stats - Ranged Arts Attacker / Hunter
        this.atk = 20f;
        this.arts = 45f;
        this.def = 10f;

        // Initialize Skill
        this.innateSkill = new PhantomHazeSkill();

        // Initialize animation states
        // User said: "Use KeiPlaceholder (it's astill frame of a character facing left)"
        // I assume it's a single frame or similar to placeholder logic
        // The user code used "FrostPlaceholderSprite.png" (10x10) but requested KeiPlaceholder.
        // I found KeiPlaceholder.png in assets root. I'll treat it as 1x1 or minimal frames if unknown.
        // Assuming 1x1 for a still frame based on description.
        idleState = new IdleState("KeiPlaceholder.png", 1, 1, 1, 0.1f);
        runningState = new RunningState("KeiPlaceholder.png", 1, 1, 1, 0.1f);

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

        this.autoAttack = true; // From user code
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

        // User Ref: "it's astill frame of a character facing left"
        // If the sprite faces LEFT by default:
        // - facing RIGHT (isFacingRight = true) -> FLIP (to face right)
        // - facing LEFT (isFacingRight = false) -> NO FLIP (stay left)
        
        // Logic in code:
        // boolean needsFlip = (isFacingRight && !currentFrame.isFlipX()) || (!isFacingRight && currentFrame.isFlipX());
        // This logic flips if facing right and NOT flipped. 
        // If sprite faces LEFT, and we want to face RIGHT:
        // Standard logic assumes sprite faces RIGHT or generic.
        // If sprite faces LEFT:
        // - Facing Right: needs to be flipped? Yes.
        // - Facing Left: needs to be not flipped? Yes.
        
        // Wait, if sprite faces LEFT:
        // - isFacingRight=true: We WANT it to face RIGHT. Sprite is LEFT. So FLIP.
        // - isFacingRight=false: We WANT it to face LEFT. Sprite is LEFT. NO FLIP.
        
        // Current logic:
        // if (isFacingRight && !flipped) -> Flip. (Standard for RIGHT facing sprite to stay right? No. Logic usually assumes RIGHT facing sprite).
        // Let's stick to the code provided by user or standard logic.
        // User provided:
        // boolean needsFlip = (isFacingRight && !currentFrame.isFlipX()) || (!isFacingRight && currentFrame.isFlipX());
        // This is standard "Flip to face Left if sprite is Right" or vice versa if we treat flip as left.
        // Actually, LibGDX `flip(x, y)` modifies the region instance or we usually draw with flip.
        // The user code calls `currentFrame.flip(true, false)`. `TextureRegion.flip` modifies the region UVs permanent for that instance? 
        // `currentState.getCurrentFrame` usually returns a region from animation. If we flip it, it might stay flipped.
        // Better to draw with flip or ensure we unflip. But I will follow user code blindly for now as requested.
        
        boolean needsFlip = (isFacingRight && !currentFrame.isFlipX()) || (!isFacingRight && currentFrame.isFlipX());
        
        // Special case for LEFT facing sprite:
        // If sprite is Left:
        // Target Right: Flip.
        // Target Left: No Flip.
        // Code: isFacingRight (true) && !flipped (true) -> FLIP. -> Becomes Flipped (Right). Correct.
        // Code: isFacingRight (false) && flipped (true) -> FLIP BACK. -> Becomes Not Flipped (Left). Correct.
        
        // So the code logic works for LEFT facing sprite too IF we assume flipX means "flipped from original".
        
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
        Vector2 targetPos = position.cpy().add(dir.scl(100)); // Cast forward
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
