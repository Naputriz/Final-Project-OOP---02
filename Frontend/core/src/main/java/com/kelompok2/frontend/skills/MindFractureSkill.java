package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class MindFractureSkill extends BaseSkill {
    private static final float COOLDOWN = 10f;
    private static final float RADIUS = 300f;
    private static final float CIRCLE_DISPLAY_DURATION = 0.5f;

    private float circleDisplayTimer = 0f;
    private long activationId = 0;

    public MindFractureSkill() {
        super("Mind Fracture", "AoE insanity debuff and damage.", COOLDOWN);
    }

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos, Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {

        // Reset display timer
        circleDisplayTimer = CIRCLE_DISPLAY_DURATION;
        activationId++;

        System.out.println("[MindFractureSkill] Activated! ID: " + activationId);
        return true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (circleDisplayTimer > 0) {
            circleDisplayTimer -= delta;
        }
    }

    public boolean shouldShowCircle() {
        return circleDisplayTimer > 0;
    }

    public float getRadius() {
        return RADIUS;
    }

    public long getActivationId() {
        return activationId;
    }

    @Override
    public Skill copy() {
        return new MindFractureSkill();
    }
}
