package com.kelompok2.frontend.entities;

import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.strategies.RangedAttackStrategy;

public class RangedEnemy extends BaseEnemy {

    private static final float ATTACK_RANGE = 300f; // Range to start shooting
    private static final float KITE_DISTANCE = 200f; // Too close, back away

    // Animation fields
    private com.kelompok2.frontend.states.AnimationState currentState;
    private com.kelompok2.frontend.states.AnimationState idleState;
    private com.kelompok2.frontend.states.AnimationState runningState;
    private float stateTime;
    private Vector2 previousPosition = new Vector2();

    public RangedEnemy(float x, float y, GameCharacter target) {
        super(x, y, 140f, 40f, target); // Moderate speed, Lower HP

        this.atk = 15f;
        this.bounds.setSize(32, 50); // Adjusted: 32x50 (More vertical)

        // Visual Setup: 2x Hitbox (64x64)
        this.renderWidth = 64f;
        this.renderHeight = 64f;
        this.boundsOffsetX = (renderWidth - bounds.width) / 2f;
        this.boundsOffsetY = 0f;

        // IMPORTANT: Use Ranged Strategy
        this.attackStrategy = new RangedAttackStrategy();
        this.autoAttack = true; // Enable auto-attacking

        // Initial animation state
        // Idle: 6 cols, 1 row
        this.idleState = new com.kelompok2.frontend.states.IdleState(
                "Enemies/enemies-vampire_idle.png", 6, 1, 6, 0.1f);
        // Run: 8 cols, 1 row (User correction)
        this.runningState = new com.kelompok2.frontend.states.RunningState(
                "Enemies/enemies-vampire_movement.png", 8, 1, 8, 0.1f);

        this.currentState = idleState;
        this.stateTime = 0f;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;

        // Simple state switch based on movement
        boolean isMoving = !position.epsilonEquals(previousPosition, 0.1f);
        if (isMoving && currentState == idleState) {
            currentState = runningState;
            stateTime = 0;
        } else if (!isMoving && currentState == runningState) {
            currentState = idleState;
            stateTime = 0;
        }
        previousPosition.set(position);

        // Update state
        currentState.update(this, delta);
    }

    @Override
    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        if (currentState != null) {
            com.badlogic.gdx.graphics.g2d.TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

            // Flip logic for Right-Facing Source Assets
            boolean targetFlip = !isFacingRight;
            if (currentFrame.isFlipX() != targetFlip) {
                currentFrame.flip(true, false);
            }

            float drawWidth = (renderWidth > 0) ? renderWidth : bounds.width;
            float drawHeight = (renderHeight > 0) ? renderHeight : bounds.height;

            // Set Color explicitly based on status
            batch.setColor(getRenderColor());

            batch.draw(currentFrame, position.x, position.y, drawWidth, drawHeight);

            // Reset color
            batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        } else {
            super.render(batch);
        }
    }

    @Override
    public void updateBehavior(float delta) {
        if (target != null) {
            float dist = Vector2.dst(this.position.x, this.position.y, target.getPosition().x, target.getPosition().y);

            float targetCenterX = target.getPosition().x + target.getVisualWidth() / 2;
            float targetFeetY = target.getPosition().y;
            float myCenterX = this.position.x + getVisualWidth() / 2;
            float myFeetY = this.position.y;

            Vector2 direction = new Vector2(targetCenterX - myCenterX, targetFeetY - myFeetY).nor();

            if (dist > ATTACK_RANGE) {
                // Too far, move closer
                move(direction, delta);
            } else if (dist < KITE_DISTANCE) {
                // Too close, move away (Kiting)
                direction.scl(-1f); // Reverse direction
                move(direction, delta);
            } else {
                // In "Sweet Spot" (Between 200 and 300)
                // Stop to shoot
                if (canAttack()) {
                    if (projectileList != null) {
                        attack(target.getPosition(), projectileList, null);
                        resetAttackTimer();
                    } else {
                        // Fallback if list not set (shouldn't happen with fix)
                        System.err.println("[RangedEnemy] No projectile list set!");
                    }
                }
            }
        }
    }

    @Override
    public String getAttackAnimationType() {
        return "shoot"; // Different animation trigger if we had sprites
    }

    @Override
    public float getXpReward() {
        return 40f;
    }
}
