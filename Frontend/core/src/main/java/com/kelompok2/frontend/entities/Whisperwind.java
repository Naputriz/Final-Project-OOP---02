package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.skills.BaseSkill;
import com.kelompok2.frontend.states.AnimationState;
import com.kelompok2.frontend.strategies.RangedAttackStrategy;
import com.kelompok2.frontend.skills.HurricaneBindSkill;

public class Whisperwind extends GameCharacter {
    // Animation state system
    private AnimationState currentState;
    private AnimationState idleState;
    private AnimationState runState;
    private float stateTime;

    // Pulse fields for tracking movement
    private Vector2 previousPosition;
    private boolean isMoving;

    public Whisperwind(float x, float y) {
        super(x, y, 190f, 110f); // Moderate speed, Moderate HP

        // Set stats sesuai GDD
        this.atk = 15f; // Low ATK
        this.arts = 38f; // High Arts
        this.def = 12f; // Moderate Defence
        this.title = "The Silent Caster";
        this.description = "Death usually knocks, but Whisperwind does not even breathe. Known as 'The Silent Caster,' she is the master of the silent kill. Her spells travel faster than sound and strike with the weight of a cannonball, yet they make no noise upon launch or impact. Legends say that if the battlefield suddenly goes quiet, it means Whisperwind has taken a position, and you are already in her sights.";

        this.setInnateSkill(new HurricaneBindSkill());
        this.skillName = innateSkill.getName();
        this.skillDescription = innateSkill.getDescription();

        Texture airSlashTexture = AssetManager.getInstance().loadTexture(AssetManager.AIR_SLASH);

        this.attackStrategy = new RangedAttackStrategy(1.0f, 600f, airSlashTexture);
        this.autoAttack = true;
        this.attackCooldown = 0.5f;

        // Initialize Animation States
        // Idle: 2x2 grid, 4 frames
        idleState = new com.kelompok2.frontend.states.IdleState("Whisperwind/pcgp-whisperwind_1.png", 2, 2, 4, 0.15f);

        // Run: 3x4 grid, 10 frames
        runState = new com.kelompok2.frontend.states.RunningState("Whisperwind/pcgp-whisperwind-run.png", 3, 4, 10,
                0.1f);

        // Start with idle state
        currentState = idleState;
        currentState.enter(this);
        stateTime = 0f;

        // Initialize movement tracking
        previousPosition = new Vector2(x, y);
        isMoving = false;

        // Note: this.texture is no longer used for rendering, but kept for
        // compatibility if needed elsewhere
        // We can just use the first frame for the texture property if necessary,
        // otherwise leave as null/placeholder
        // For now, removing the AssetManager.loadTexture call for the placeholder.

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
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;

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

        // Flip logic (standard for all characters)
        // Asset faces LEFT by default? Or RIGHT?
        // Ryze/Insania assets face LEFT.
        // If this asset faces LEFT:
        // isFacingRight=true -> FLIP
        // isFacingRight=false -> NO FLIP

        // Assuming standard format like Ryze:
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

        if (innateSkill != null && innateSkill instanceof BaseSkill) {
            ((BaseSkill) innateSkill).render(batch);
        }
        if (secondarySkill != null) {
            ((BaseSkill) secondarySkill).render(batch);
        }
        if (ultimateSkill != null) {
            ((BaseSkill) ultimateSkill).render(batch);
        }
    }

    @Override
    public void performInnateSkill() {
        // Call overload dengan facing direction sebagai fallback
        Vector2 dir = new Vector2(isFacingRight ? 1 : -1, 0);
        Vector2 targetPos = position.cpy().add(dir.scl(100));
        performInnateSkill(targetPos);
    }

    // Getter untuk skill cooldown bar
    public float getSkillTimer() {
        return (innateSkill != null) ? innateSkill.getRemainingCooldown() : 0f;
    }

    public float getSkillCooldown() {
        return (innateSkill != null) ? innateSkill.getCooldown() : 0f;
    }

    @Override
    public float getInnateSkillTimer() {
        return (innateSkill != null) ? innateSkill.getRemainingCooldown() : 0f;
    }

    @Override
    public float getInnateSkillCooldown() {
        return (innateSkill != null) ? innateSkill.getCooldown() : 0f;
    }

    @Override
    public String getAttackAnimationType() {
        return "slash"; // Whisperwind uses slash animations
    }
}
