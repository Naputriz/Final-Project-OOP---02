package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class FastEnemy extends BaseEnemy {

    public FastEnemy(float x, float y, GameCharacter target) {
        super(x, y, 250f, 30f, target); // High Speed, Low HP

        this.atk = 10f; // Lower damage per hit
        this.bounds.setSize(24, 24); // Smaller size

        createTexture();
    }

    private void createTexture() {
        Pixmap pixmap = new Pixmap(24, 24, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.YELLOW);
        pixmap.fill();
        this.texture = new Texture(pixmap);
        pixmap.dispose();
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
