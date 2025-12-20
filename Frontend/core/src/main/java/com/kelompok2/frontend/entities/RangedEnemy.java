package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.strategies.RangedAttackStrategy;

public class RangedEnemy extends BaseEnemy {

    private static final float ATTACK_RANGE = 300f; // Range to start shooting
    private static final float KITE_DISTANCE = 200f; // Too close, back away

    public RangedEnemy(float x, float y, GameCharacter target) {
        super(x, y, 140f, 40f, target); // Moderate speed, Lower HP

        this.atk = 15f;
        this.bounds.setSize(32, 32);

        // IMPORTANT: Use Ranged Strategy
        this.attackStrategy = new RangedAttackStrategy();
        this.autoAttack = true; // Enable auto-attacking

        createTexture();
    }

    private void createTexture() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.ORANGE); // Orange for ranged
        pixmap.fill();
        this.texture = new Texture(pixmap);
        pixmap.dispose();
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
