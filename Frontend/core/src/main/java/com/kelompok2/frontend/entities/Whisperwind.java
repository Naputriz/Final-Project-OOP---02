package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.strategies.RangedAttackStrategy;

public class Whisperwind extends GameCharacter {

    // Hurricane Bind skill cooldown
    private float skillCooldown = 10f; // 10 seconds cooldown (from GDD)
    private float skillTimer = 0f;

    // Active projectiles from Hurricane Bind
    private Array<Projectile> hurricaneProjectiles;

    // Animation state system
    private com.kelompok2.frontend.states.AnimationState currentState;
    private com.kelompok2.frontend.states.AnimationState idleState;
    private com.kelompok2.frontend.states.AnimationState runState;
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
        this.skillName = "Hurricane Bind";
        this.skillDescription = "Wind ball with knockback and stun. Damage: Arts x2.0, Cooldown: 10s";

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

        // Ranged Attack Strategy - Air Slash (projectile dengan Arts scaling)
        // Light cyan untuk wind theme
        this.attackStrategy = new RangedAttackStrategy(0.8f, new Color(0.6f, 1f, 1f, 1f));

        // Auto attack (hold to shoot continuously)
        this.autoAttack = true;
        this.attackCooldown = 0.7f; // Cooldown 0.7 detik untuk ranged

        // Initialize hurricane projectiles array
        hurricaneProjectiles = new Array<>();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;

        // Check movement for state transition
        checkMovementState();

        // Update current state (animation)
        currentState.update(this, delta);

        // Update skill cooldown
        if (skillTimer > 0) {
            skillTimer -= delta;
        }

        // Update hurricane projectiles
        for (int i = hurricaneProjectiles.size - 1; i >= 0; i--) {
            Projectile p = hurricaneProjectiles.get(i);
            p.update(delta);
            if (!p.active) {
                hurricaneProjectiles.removeIndex(i);
            }
        }
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
            System.out.println("[Whisperwind] Hurricane Bind on cooldown: " +
                    String.format("%.1f", skillTimer) + "s remaining");
            return;
        }

        // Calculate direction dari posisi player ke mouse cursor
        float playerCenterX = position.x + getVisualWidth() / 2;
        float playerCenterY = position.y + getVisualHeight() / 2;
        Vector2 direction = new Vector2(
                mousePos.x - playerCenterX,
                mousePos.y - playerCenterY).nor();

        // Calculate damage (Arts scaling × 2.0 for knockback projectile)
        float damage = this.arts * 2.0f;

        // Create Hurricane Bind projectile (wind ball)
        // Slower moving, larger hitbox projectile with knockback
        Projectile windBall = new Projectile(
                playerCenterX, playerCenterY,
                direction, damage);

        // Make it cyan/wind colored
        windBall.setColor(new Color(0.5f, 1f, 1f, 0.9f));
        hurricaneProjectiles.add(windBall);

        // Reset cooldown
        skillTimer = skillCooldown;

        System.out.println("[Whisperwind] Hurricane Bind activated! Damage: " + damage +
                " (Wind ball with knockback + stun)");
    }

    public Array<Projectile> getHurricaneProjectiles() {
        return hurricaneProjectiles;
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
        return "slash"; // Whisperwind uses slash animations
    }
}
