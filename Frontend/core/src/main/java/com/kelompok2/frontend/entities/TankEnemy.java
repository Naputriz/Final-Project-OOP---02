package com.kelompok2.frontend.entities;

import com.badlogic.gdx.math.Vector2;

public class TankEnemy extends BaseEnemy {

    // Animation fields
    private com.kelompok2.frontend.states.AnimationState currentState;
    private com.kelompok2.frontend.states.AnimationState idleState;
    private com.kelompok2.frontend.states.AnimationState runningState;
    private float stateTime;
    private Vector2 previousPosition = new Vector2(); // Moving this up here

    public TankEnemy(float x, float y, GameCharacter target) {
        super(x, y, 90f, 150f, target); // Low Speed, High HP (3x normal)

        this.atk = 20f;
        this.def = 25f; // Has defense
        this.bounds.setSize(60, 100); // Bigger size, adjusted to 60x100

        // Visual Setup: ~2.6x Hitbox (128x128) - User requested larger size
        this.renderWidth = 128f;
        this.renderHeight = 128f;
        this.boundsOffsetX = (renderWidth - bounds.width) / 2f;
        this.boundsOffsetY = 0f;

        // Initial animation state
        // Idle: 6 cols, 1 row
        this.idleState = new com.kelompok2.frontend.states.IdleState(
                "Enemies/enemies-skeleton1_idle.png", 6, 1, 6, 0.1f);
        // Run: 10 cols, 1 row
        this.runningState = new com.kelompok2.frontend.states.RunningState(
                "Enemies/enemies-skeleton1_movement.png", 10, 1, 10, 0.1f);

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
            // Relentless slow chase
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
        return 50f; // High reward for killing tank
    }
}
