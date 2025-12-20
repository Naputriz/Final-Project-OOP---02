package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class IceShieldSkill extends BaseSkill {

    // Shield state tracking
    private boolean shieldActive = false;
    private float shieldDuration = 5f;
    private float shieldTimer = 0f;

    private GameCharacter shieldedUser;

    public IceShieldSkill() {
        super("Ice Shield", "Reduce damage by 50% for 5s", 20f); // 20 second cooldown
    }

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {
        // Activate shield
        shieldActive = true;
        shieldTimer = shieldDuration;
        shieldedUser = user;

        System.out.println("[Ice Shield] Shield activated for 5 seconds! 50% damage reduction");
        return true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update shield duration
        if (shieldActive) {
            shieldTimer -= delta;
            if (shieldTimer <= 0) {
                shieldActive = false;
                shieldTimer = 0;
                System.out.println("[Ice Shield] Shield expired");
            }
        }
    }

    public boolean isShieldActive() {
        return shieldActive;
    }

    public float getShieldTimeRemaining() {
        return shieldTimer;
    }

    public float getDamageReduction() {
        return 0.5f;
    }

    public GameCharacter getShieldedUser() {
        return shieldedUser;
    }

    @Override
    public Skill copy() {
        return new IceShieldSkill();
    }

    @Override
    public float onOwnerTakeDamage(GameCharacter owner, float amount) {
        if (shieldActive) {
            float reduced = amount * 0.5f;
            System.out.println("[Ice Shield] Shield active! Damage reduced from " + amount + " to " + reduced);
            return reduced;
        }
        return amount;
    }
}
