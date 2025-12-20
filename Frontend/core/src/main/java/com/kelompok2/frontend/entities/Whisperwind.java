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
    private float stateTime;

    public Whisperwind(float x, float y) {
        super(x, y, 190f, 110f); // Moderate speed, Moderate HP

        // Set stats sesuai GDD
        this.atk = 15f; // Low ATK
        this.arts = 38f; // High Arts
        this.def = 12f; // Moderate Defence

        // Initialize Animation States
        // Idle: 2x2 grid, 4 frames
        idleState = new com.kelompok2.frontend.states.IdleState("Whisperwind/pcgp-whisperwind_1.png", 2, 2, 4, 0.15f);

        // Start with idle state
        currentState = idleState;
        currentState.enter(this);
        stateTime = 0f;

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

        // Draw current frame
        batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);
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
