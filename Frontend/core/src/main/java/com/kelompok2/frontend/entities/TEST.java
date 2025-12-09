package com.kelompok2.frontend.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class TEST extends GameCharacter {

    public TEST(float x, float y) {
        super(x, y, 300f, 100f);

        this.texture = new Texture(Gdx.files.internal("kotaro.png"));

        this.bounds.setSize(256, 256);

        this.autoAttack = true;
        this.attackCooldown = 0.1f;
    }

    @Override
    public void attack(Vector2 targetPos, Array<Projectile> projectiles) {
        float startX = this.position.x + this.getWidth() / 2;
        float startY = this.position.y + this.getHeight() / 2;

        Projectile p = new Projectile(startX, startY, targetPos.x, targetPos.y);
        projectiles.add(p);
    }

    @Override
    public void performInnateSkill() {
        System.out.println("Test is using skill");
    }
}
