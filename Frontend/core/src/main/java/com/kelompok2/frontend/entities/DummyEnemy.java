package com.kelompok2.frontend.entities;

import com.badlogic.gdx.math.Vector2;

public class DummyEnemy extends BaseEnemy {

    // Animation fields
    private com.kelompok2.frontend.states.AnimationState currentState;
    private com.kelompok2.frontend.states.AnimationState idleState;
    private com.kelompok2.frontend.states.AnimationState runningState;
    private float stateTime;

    public DummyEnemy(float x, float y, GameCharacter target) {
        super(x, y, 125f, 50f, target); // Speed 125, HP 50

        this.atk = 15f;
        this.bounds.setSize(32, 32);

        // Visual Setup: 3x Hitbox (96x96) - User requested larger size
        this.renderWidth = 96f;
        this.renderHeight = 96f;
        this.boundsOffsetX = (renderWidth - bounds.width) / 2f;
        this.boundsOffsetY = 0f;

        // Initial animation state
        // Idle: 6 cols, 1 row
        this.idleState = new com.kelompok2.frontend.states.IdleState(
                "Enemies/enemies-skeleton2_idle.png", 6, 1, 6, 0.1f);
        // Run: 10 cols, 1 row (Note typo in filename)
        this.runningState = new com.kelompok2.frontend.states.RunningState(
                "Enemies/enemies-skeleton2_movemen.png", 10, 1, 10, 0.1f);

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

    private Vector2 previousPosition = new Vector2();

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

            // Set Color explicitly based on status (BaseEnemy handles frozen, GameCharacter
            // handles others)
            batch.setColor(getRenderColor());

            batch.draw(currentFrame, position.x, position.y, drawWidth, drawHeight);

            // Reset Color
            batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        } else {
            super.render(batch);
        }
    }

    @Override
    public void updateBehavior(float delta) {
        if (target != null) {
            // Simple chase behavior
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
        return 25f;
    }
}
