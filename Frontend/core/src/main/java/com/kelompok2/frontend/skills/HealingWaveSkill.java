package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class HealingWaveSkill extends BaseSkill {

    public HealingWaveSkill() {
        super("Healing Wave", "Restore 10% Max HP", 15f); // 15 second cooldown
    }

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {
        // Calculate healing amount - 10% to keep Aelita valuable (30%)
        float healAmount = user.getMaxHp() * 0.1f;

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
        return new HealingWaveSkill();
    }
}
