package com.kelompok2.frontend.strategies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class MarkingMeleeAttackStrategy extends MeleeAttackStrategy {

    public MarkingMeleeAttackStrategy(float range, float width, float damageMultiplier, float duration) {
        super(range, width, damageMultiplier, duration);
    }

    @Override
    public void execute(GameCharacter attacker, Vector2 targetPos,
            Array<Projectile> projectiles, Array<MeleeAttack> meleeAttacks) {

        // Simpan jumlah melee attacks sebelum execute
        int previousSize = meleeAttacks.size;

        // Panggil parent execute untuk membuat MeleeAttack dengan positioning yang benar
        super.execute(attacker, targetPos, projectiles, meleeAttacks);

        // Set appliesMark = true pada MeleeAttack yang baru dibuat
        if (meleeAttacks.size > previousSize) {
            MeleeAttack newAttack = meleeAttacks.get(meleeAttacks.size - 1);
            newAttack.setAppliesMark(true);
        }
    }
}
