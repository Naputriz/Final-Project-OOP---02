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

    private float reductionAmount = 0.5f;

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

        System.out.println(
                "[Ice Shield] Shield activated for 5 seconds! " + (int) (reductionAmount * 100) + "% damage reduction");
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
        return reductionAmount;
    }

    public GameCharacter getShieldedUser() {
        return shieldedUser;
    }

    @Override
    public Skill copy() {
        IceShieldSkill copy = new IceShieldSkill();
        copy.reductionAmount = this.reductionAmount;
        copy.description = this.description;
        return copy;
    }

    @Override
    public float onOwnerTakeDamage(GameCharacter owner, float amount) {
        if (shieldActive) {
            float reduced = amount * (1.0f - reductionAmount); // If 0.5 reduction, take 0.5. If 0.8, take 0.2
            System.out.println("[Ice Shield] Shield active! Damage reduced from " + amount + " to " + reduced);
            return reduced;
        }
        return amount;
    }

    @Override
    public void onEquip(GameCharacter owner) {
        // Bonus for Isolde: 80% Damage Reduction (Ice Force)
        if (owner instanceof com.kelompok2.frontend.entities.Isolde) {
            this.reductionAmount = 0.8f;
            this.description = "Reduce damage by 80% for 5s - ISOLDE COMBO!";
            System.out.println("[Ice Shield] Combo activated for Isolde! Reduction increased to 80%.");
        }
    }
}
