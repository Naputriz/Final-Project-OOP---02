package com.kelompok2.frontend.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.kelompok2.frontend.strategies.RangedAttackStrategy;

public class TEST extends GameCharacter {

    public TEST(float x, float y) {
        super(x, y, 300f, 100f);

        this.texture = new Texture(Gdx.files.internal("kotaro.png"));

        this.bounds.setSize(256, 256);

        // TEST menggunakan ranged attack
        this.attackStrategy = new RangedAttackStrategy();

        this.autoAttack = true;
        this.attackCooldown = 0.1f;
    }

    @Override
    public void performInnateSkill() {
        System.out.println("Test is using skill");
    }

    @Override
    public float getInnateSkillTimer() {
        return 0;
    }

    @Override
    public float getInnateSkillCooldown() {
        return 0;
    }

    @Override
    public String getAttackAnimationType() {
        return "slash";
    }
}
