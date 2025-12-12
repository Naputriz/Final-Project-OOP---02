package com.kelompok2.frontend.strategies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class MeleeAttackStrategy implements AttackStrategy {
    private float range; // Jarak jangkauan serangan (dalam pixel)
    private float width; // Lebar hitbox serangan
    private float damageMultiplier; // Multiplier untuk damage (akan dikalikan dengan ATK)
    private float duration; // Durasi hitbox aktif

    public MeleeAttackStrategy(float range, float width, float damageMultiplier, float duration) {
        this.range = range;
        this.width = width;
        this.damageMultiplier = damageMultiplier;
        this.duration = duration;
    }

    public MeleeAttackStrategy() {
        this(80f, 60f, 1.0f, 0.2f);
    }

    @Override
    public void execute(GameCharacter attacker, Vector2 targetPos,
            Array<Projectile> projectiles, Array<MeleeAttack> meleeAttacks) {

        // Hitung posisi tengah karakter (spawn point serangan)
        float centerX = attacker.getPosition().x + attacker.getVisualWidth() / 2;
        float centerY = attacker.getPosition().y + attacker.getVisualHeight() / 2;

        // Hitung arah serangan (dari karakter ke target)
        Vector2 direction = new Vector2(targetPos.x - centerX, targetPos.y - centerY).nor();

        // Update facing direction karakter berdasarkan arah serangan
        if (direction.x > 0) {
            attacker.setFacingRight(true);
        } else if (direction.x < 0) {
            attacker.setFacingRight(false);
        }

        // Hitung posisi hitbox MULAI DARI UJUNG karakter
        // Hitbox harus di luar bounds karakter, tidak overlap

        // Jarak dari center ke edge karakter (radius visual)
        float characterRadius = Math.max(attacker.getVisualWidth(), attacker.getVisualHeight()) / 2;

        // Mulai hitbox dari ujung karakter + sedikit gap (10px)
        float startOffset = characterRadius + 10f;

        // Posisi awal hitbox (ujung karakter)
        float hitboxStartX = centerX + (direction.x * startOffset);
        float hitboxStartY = centerY + (direction.y * startOffset);

        // Extend hitbox sejauh 'range' dari titik awal
        float hitboxCenterX = hitboxStartX + (direction.x * range / 2);
        float hitboxCenterY = hitboxStartY + (direction.y * range / 2);

        // Posisi hitbox (pojok kiri-bawah untuk rectangle)
        float hitboxX = hitboxCenterX - (width / 2);
        float hitboxY = hitboxCenterY - (width / 2);

        // Hitung damage berdasarkan ATK karakter
        float finalDamage = attacker.getAtk() * damageMultiplier;

        // Buat MeleeAttack baru
        MeleeAttack attack = new MeleeAttack(
                hitboxX,
                hitboxY,
                width,
                width, // Hitbox berbentuk persegi untuk sekarang
                finalDamage,
                duration,
                attacker);

        // Tambahkan ke array meleeAttacks
        meleeAttacks.add(attack);
    }
}
