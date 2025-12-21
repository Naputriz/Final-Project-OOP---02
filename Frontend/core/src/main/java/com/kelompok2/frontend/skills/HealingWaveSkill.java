package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class HealingWaveSkill extends BaseSkill {

    private float healPercentage = 0.10f;

    public HealingWaveSkill() {
        super("Healing Wave", "Restore 10% Max HP", 15f); // 15 second cooldown
    }

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {
        // Calculate healing amount from percentage
        float healAmount = user.getMaxHp() * healPercentage;

        // Apply healing
        float hpBefore = user.getHp();
        user.heal(healAmount);
        float hpAfter = user.getHp();
        float actualHealing = hpAfter - hpBefore;

        System.out.println(
                "[Healing Wave] Healed for " + actualHealing + " HP (" + hpAfter + "/" + user.getMaxHp() + ")");
        return true;
    }

    @Override
    public Skill copy() {
        HealingWaveSkill copy = new HealingWaveSkill();
        copy.healPercentage = this.healPercentage;
        copy.description = this.description;
        return copy;
    }

    @Override
    public void onEquip(GameCharacter owner) {
        // Bonus for Aelita: Double healing (10% -> 20%)
        if (owner instanceof com.kelompok2.frontend.entities.Aelita) {
            this.healPercentage = 0.20f;
            this.description = "Restore 20% Max HP - AELITA COMBO!";
            System.out.println("[Healing Wave] Combo activated for Aelita! Healing increased to 20%.");
        }
    }
}
