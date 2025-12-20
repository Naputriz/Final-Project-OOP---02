package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
// import com.badlogic.gdx.graphics.g2d.GlyphLayout; // Unused
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class DamageIndicator {
    private Vector2 position;
    private String text;
    private Color color;
    private float lifeTime;
    private float maxLifeTime;
    private float speed = 50f; // Floating speed
    private boolean active;

    public DamageIndicator(float x, float y, float damage, Color color) {
        this.position = new Vector2(x, y);
        if (damage < 1f && damage > 0f) {
            this.text = String.format("%.1f", damage);
        } else {
            this.text = String.valueOf((int) damage);
        }
        this.color = color;
        this.maxLifeTime = 1.0f; // 1 second lifetime
        this.lifeTime = maxLifeTime;
        this.active = true;
    }

    public void update(float delta) {
        if (!active)
            return;

        position.y += speed * delta; // Float up
        lifeTime -= delta;

        if (lifeTime <= 0) {
            active = false;
        }
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        if (!active)
            return;

        Color originalColor = font.getColor();

        // Fade out alpha
        float alpha = lifeTime / maxLifeTime;
        font.setColor(color.r, color.g, color.b, alpha);

        font.draw(batch, text, position.x, position.y);

        font.setColor(originalColor); // Restore original color
    }

    public boolean isActive() {
        return active;
    }
}
