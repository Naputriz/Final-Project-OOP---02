package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class HellfirePillarSkill extends BaseSkill {
    private static final float COOLDOWN = 5f;
    private static final float DURATION = 2f;
    private static final float RADIUS = 40f;

    private boolean isActive = false;
    private float activeTimer = 0f;
    private Vector2 pillarPosition;

    public HellfirePillarSkill() {
        super("Hellfire Pillar", "Summons a pillar of fire.", COOLDOWN);
        this.pillarPosition = new Vector2();
    }

    @Override
    protected void executeSkill(GameCharacter user, Vector2 targetPos, Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {

        pillarPosition.set(targetPos);
        isActive = true;
        activeTimer = DURATION;

        System.out.println("[HellfirePillarSkill] Activated at " + targetPos);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (isActive) {
            activeTimer -= delta;
            if (activeTimer <= 0) {
                isActive = false;
            }
        }
    }

    public boolean isPillarActive() {
        return isActive;
    }

    public Vector2 getPillarPosition() {
        return pillarPosition;
    }

    public float getPillarRadius() {
        return RADIUS;
    }

    @Override
    public Skill copy() {
        return new HellfirePillarSkill();
    }
}
