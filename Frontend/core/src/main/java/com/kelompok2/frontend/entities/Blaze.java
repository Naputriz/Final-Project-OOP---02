package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.skills.HellfirePillarSkill;
import com.kelompok2.frontend.strategies.MeleeAttackStrategy;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.states.AnimationState;
import com.kelompok2.frontend.states.IdleState;
import com.kelompok2.frontend.states.RunningState;

public class Blaze extends GameCharacter {

    // State Pattern for animations
    private AnimationState currentState;
    private AnimationState idleState;
    private AnimationState runningState;
    private float stateTime = 0f;

    // Movement tracking for state transitions
    private Vector2 lastPosition;

    // Skill
    private HellfirePillarSkill innateSkill;

    public Blaze(float x, float y) {
        super(x, y, 180f, 110f); // Moderate speed, Moderate HP

        // Set stats sesuai role Arts Attacker
        this.atk = 25f; // Moderate ATK
        this.arts = 40f; // High Arts - primary damage source
        this.def = 5f; // Low Defence

        // Initialize Skill
        this.innateSkill = new HellfirePillarSkill();

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
        // Flame Punch - Hybrid damage melee attack
        // Increased range to 120f
        this.attackStrategy = new MeleeAttackStrategy(120f, 90f, 1.0f, 0.25f);
        this.autoAttack = true;
        this.attackCooldown = 0.4f;

        lastPosition = new Vector2(x, y);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update state time
        stateTime += delta;
        currentState.update(this, delta);
        innateSkill.update(delta);

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
        if (innateSkill.isPillarActive()) {
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
        // Delegate to Skill
        innateSkill.activate(this, mousePos, null, null);
    }

    public boolean isPillarActive() {
        return innateSkill.isPillarActive();
    }

    public Vector2 getPillarPosition() {
        return innateSkill.getPillarPosition();
    }

    public float getPillarRadius() {
        return innateSkill.getPillarRadius();
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
        return "slash"; // Blaze uses slash animations
    }

    @Override
    public float getAtk() {
        // For Flame Punch, return hybrid damage
        return (this.atk * 0.7f) + (this.arts * 0.3f);
    }
}
