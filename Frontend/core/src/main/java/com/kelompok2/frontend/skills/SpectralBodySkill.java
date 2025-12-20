package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class SpectralBodySkill extends BaseSkill {
    private static final float COOLDOWN = 15f;
    private static final float DURATION = 3f;

    private boolean isActive = false;
    private float activeTimer = 0f;

    public SpectralBodySkill() {
        super("Spectral Body", "Invulnerability for 3 seconds.", COOLDOWN);
    }

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos, Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {

        isActive = true;
        activeTimer = DURATION;

        // Logic to apply "Invulnerable" state to user would go here or be checked via
        // isActive
        // Currently Ryze uses checks on the timer/flag. Ryze entity will need to check
        // this skill's state.

        System.out.println("[SpectralBodySkill] Activated!");
        return true;
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

    public boolean isSpectralActive() {
        return isActive;
    }

    @Override
    public Skill copy() {
        return new SpectralBodySkill();
    }
}
