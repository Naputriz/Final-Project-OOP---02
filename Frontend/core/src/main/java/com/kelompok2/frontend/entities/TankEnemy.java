package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class TankEnemy extends BaseEnemy {

    public TankEnemy(float x, float y, GameCharacter target) {
        super(x, y, 90f, 150f, target); // Low Speed, High HP (3x normal)

        this.atk = 20f;
        this.def = 25f; // Has defense
        this.bounds.setSize(48, 48); // Bigger size

        createTexture();
    }

    private void createTexture() {
        Pixmap pixmap = new Pixmap(48, 48, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.GREEN); // Green for tank
        pixmap.fill();
        this.texture = new Texture(pixmap);
        pixmap.dispose();
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
