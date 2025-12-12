package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class DummyEnemy extends GameCharacter {
    private GameCharacter target;

    public DummyEnemy(float x, float y, GameCharacter target) {
        super(x, y, 150f, 50f); // Speed 150 (lebih lambat dari player), HP 50
        this.target = target;

        // Bikin tekstur kotak merah
        createTexture();

        // Ukuran musuh
        this.bounds.setSize(32, 32);

        this.attackCooldown = 1.0f;
    }

    private void createTexture() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();
        this.texture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void performInnateSkill() {
        // Dummy gak punya skill
    }

    public float getXpReward() {
        return 25f;
    }

    // Logic AI: Kejar target
    public void update(float delta) {
        super.update(delta);

        if (target != null) {
            // titik tengah visual player
            float targetCenterX = target.getPosition().x + target.getVisualWidth() / 2;
            float targetFeetY = target.getPosition().y;

            // titik tengah visual dummy enemy
            float myCenterX = this.position.x + getVisualWidth() / 2;
            float myFeetY = this.position.y;
            Vector2 direction = new Vector2(
                    targetCenterX - myCenterX,
                    targetFeetY - myFeetY).nor(); // Normalisasi biar speed konstan

            move(direction.x * delta, direction.y * delta);
        }
    }
}
