package com.kelompok2.frontend.strategies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;


public class RangedAttackStrategy implements AttackStrategy {
    private float damageMultiplier; // Multiplier untuk damage (akan dikalikan dengan Arts)

    public RangedAttackStrategy(float damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    // Ranged attack untuk sekarang scaling pure arts
    public RangedAttackStrategy() {
        this(1.0f);
    }

    @Override
    public void execute(GameCharacter attacker, Vector2 targetPos,
            Array<Projectile> projectiles, Array<MeleeAttack> meleeAttacks) {

        // Hitung posisi spawn projectile (tengah karakter)
        float startX = attacker.getPosition().x + attacker.getVisualWidth() / 2;
        float startY = attacker.getPosition().y + attacker.getVisualHeight() / 2;

        // Hitung damage berdasarkan Arts karakter
        float finalDamage = attacker.getArts() * damageMultiplier;

        // Buat Projectile baru dengan damage yang sudah dikalkulasi
        Projectile p = new Projectile(startX, startY, targetPos.x, targetPos.y, finalDamage);
        projectiles.add(p);
    }
}
