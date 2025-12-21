package com.kelompok2.frontend.entities;

import com.badlogic.gdx.math.Vector2;

public class FastEnemy extends BaseEnemy {

    public FastEnemy(float x, float y, GameCharacter target) {
        super(x, y, 250f, 30f, target); // High Speed, Low HP

        this.atk = 10f; // Lower damage per hit
        this.bounds.setSize(24, 24); // Smaller size

        // Visual Setup: 2x Hitbox (48x48)
        this.renderWidth = 48f;
        this.renderHeight = 48f;
        this.boundsOffsetX = (renderWidth - bounds.width) / 2f;
        this.boundsOffsetY = 0f;

        // Initial animation state (Idle/Run same for Skull)
        // Skull.png is 2x2 grid (4 frames)
        com.kelompok2.frontend.states.IdleState idleState = new com.kelompok2.frontend.states.IdleState(
                "Enemies/Skull.png", 2, 2, 4, 0.1f);
        com.kelompok2.frontend.states.RunningState runState = new com.kelompok2.frontend.states.RunningState(
                "Enemies/Skull.png", 2, 2, 4, 0.1f);

        this.idleState = idleState;
        this.runningState = runState;
        this.currentState = idleState;
        this.stateTime = 0f;
    }

    // Animation fields
    private com.kelompok2.frontend.states.AnimationState currentState;
    private com.kelompok2.frontend.states.AnimationState idleState;
    private com.kelompok2.frontend.states.AnimationState runningState;
    private float stateTime;

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

    private Vector2 previousPosition = new Vector2();

    @Override
    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        // Logic similar to Aegis render
        if (currentState != null) {
            com.badlogic.gdx.graphics.g2d.TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

            // Flip logic for Right-Facing Source Assets
            // If facing Right (true) -> No Flip (false)
            // If facing Left (false) -> Flip (true)
            boolean targetFlip = !isFacingRight;
            if (currentFrame.isFlipX() != targetFlip) {
                currentFrame.flip(true, false);
            }

            float drawWidth = (renderWidth > 0) ? renderWidth : bounds.width;
            float drawHeight = (renderHeight > 0) ? renderHeight : bounds.height;

            batch.draw(currentFrame, position.x, position.y, drawWidth, drawHeight);
        } else {
            super.render(batch);
        }
    }

    @Override
    public void updateBehavior(float delta) {
        if (target != null) {
            // Aggressive chase
            float targetCenterX = target.getPosition().x + target.getVisualWidth() / 2;
            float targetFeetY = target.getPosition().y;
            float myCenterX = this.position.x + getVisualWidth() / 2;
            float myFeetY = this.position.y;

            Vector2 direction = new Vector2(targetCenterX - myCenterX, targetFeetY - myFeetY).nor();
            move(direction, delta);
        }
    }

    @Override
    public float getXpReward() {
        return 35f; // Higher reward for hitting small fast target
    }
}
