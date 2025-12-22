package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.strategies.RangedAttackStrategy;

public class Isolde extends GameCharacter {

    // Animation state system
    private com.kelompok2.frontend.states.AnimationState currentState;
    private com.kelompok2.frontend.states.AnimationState idleState;
    private com.kelompok2.frontend.states.AnimationState runState;
    private float stateTime;

    // Movement tracking
    private Vector2 previousPosition;
    private boolean isMoving;

    // Glacial Breath skill cooldown
    private float skillCooldown = 10f; // 10 seconds cooldown
    private float skillTimer = 0f;

    // Active Glacial Breath attacks
    private Array<GlacialBreath> glacialBreaths;

    public Isolde(float x, float y) {
        super(x, y, 180f, 120f);

        // Set stats sesuai role Arts Attacker
        this.atk = 20f; // Ranged ATK (balanced with Blaze)
        this.arts = 40f; // High Arts (main damage stat, similar to Insania)
        this.def = 15f; // Moderate Defence (ranged glass cannon)
        this.title = "The Frost Kaiser";
        this.description = "They say Blaze brings the apocalypse of fire, but Isolde brings the silence that follows. The Frost Kaiser is a being of elegant cruelty, believing that the only way to truly 'save' the world is to stop it entirely. With her return, she intends to drape the nations in an eternal winter, reclaiming the control stripped from her centuries ago. Where Blaze burns, she preserves—trapping her victims in ice for eternity.";
        this.skillName = "Glacial Breath";
        this.skillDescription = "Cone attack that freezes enemies. Damage: Arts x1.0, Cooldown: 10s";

        // Initialize Animation States
        // Both Idle and Run use the same asset:
        // Isolde/pcgp-isolde.png: 2 cols, 3 rows, 5 frames
        idleState = new com.kelompok2.frontend.states.IdleState("Isolde/pcgp-isolde.png", 2, 3, 5, 0.15f);
        runState = new com.kelompok2.frontend.states.RunningState("Isolde/pcgp-isolde.png", 2, 3, 5, 0.1f);

        // Start with idle state
        currentState = idleState;
        currentState.enter(this);
        stateTime = 0f;

        // Initialize movement tracking
        previousPosition = new Vector2(x, y);
        isMoving = false;

        // Ukuran visual dan hitbox
        float visualSize = 128f; // Reduced size for better gameplay visibility
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

        Texture iceBallTexture = AssetManager.getInstance().loadTexture(AssetManager.ICE_BALL);
        // Ranged Attack Strategy - Shoot icicles (projectile dengan Arts scaling)
        // RangedAttackStrategy(damageMultiplier, color) - Light blue untuk frost theme
        // Damage lowered to 0.5x untuk testing freeze mechanics
        this.attackStrategy = new RangedAttackStrategy(0.8f, 400, iceBallTexture); // Light blue/cyan

        // Auto attack (hold to shoot continuously)
        this.autoAttack = true;
        this.attackCooldown = 0.8f; // Cooldown 0.8 detik untuk ranged

        // Initialize glacial breaths array
        glacialBreaths = new Array<>();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;

        // Check movement for state transition
        checkMovementState();

        // Update animation state
        currentState.update(this, delta);

        // Update skill cooldown
        if (skillTimer > 0) {
            skillTimer -= delta;
        }

        // Update active glacial breaths
        for (int i = glacialBreaths.size - 1; i >= 0; i--) {
            GlacialBreath gb = glacialBreaths.get(i);
            gb.update(delta);
            if (!gb.isActive()) {
                glacialBreaths.removeIndex(i);
            }
        }
    }

    private void checkMovementState() {
        // Compare current position with previous position
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
        // Get frame saat ini dari animation
        TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

        // Hitung posisi render (position sudah correct, tidak perlu offset lagi)
        float renderX = position.x;
        float renderY = position.y;

        // Set Color explicitly based on status
        batch.setColor(getRenderColor());

        // Flip sprite jika menghadap kiri
        // Flip sprite based heavily on assumption that asset faces LEFT
        // If facing RIGHT, we need to flip it
        if (isFacingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }
        // If facing LEFT, we need to ensure it is NOT flipped (original state)
        else if (!isFacingRight && currentFrame.isFlipX()) {
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
    public void performInnateSkill(Vector2 mousePos) {
        // Check cooldown
        if (skillTimer > 0) {
            System.out.println("[Isolde] Glacial Breath on cooldown: " +
                    String.format("%.1f", skillTimer) + "s remaining");
            return;
        }

        // Calculate direction dari posisi player ke mouse cursor
        float playerCenterX = position.x + getVisualWidth() / 2;
        float playerCenterY = position.y + getVisualHeight() / 2;
        Vector2 direction = new Vector2(
                mousePos.x - playerCenterX,
                mousePos.y - playerCenterY).nor();

        // Calculate damage (Arts scaling x 1.0)
        float damage = this.arts * 1.0f;

        // Create Glacial Breath cone attack (0.5s duration) aiming toward mouse
        GlacialBreath glacialBreath = new GlacialBreath(this, direction, damage, 0.5f);
        glacialBreaths.add(glacialBreath);

        // Reset cooldown
        skillTimer = skillCooldown;

        System.out.println("[Isolde] Glacial Breath activated! Damage: " + damage +
                " (aimed at mouse)");
    }

    public Array<GlacialBreath> getGlacialBreaths() {
        return glacialBreaths;
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
        return "slash"; // Isolde uses slash animations
    }
}
