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

        // Hitung posisi hitbox yang DIMULAI DARI EDGE collision bounds karakter
        // (bukan visual bounds, tapi hitbox asli untuk collision)
        // Ini agar bisa hit musuh dekat tanpa overlap dengan karakter sendiri

        // Ambil posisi dan ukuran collision bounds karakter
        float boundsX = attacker.getBounds().x;
        float boundsY = attacker.getBounds().y;
        float boundsWidth = attacker.getBounds().width;
        float boundsHeight = attacker.getBounds().height;

        // Center dari collision bounds
        float boundsCenterX = boundsX + boundsWidth / 2;
        float boundsCenterY = boundsY + boundsHeight / 2;

        // Jarak dari center bounds ke edge bounds dalam arah attack
        // (menggunakan proyeksi direction ke ukuran bounds)
        float radiusX = boundsWidth / 2;
        float radiusY = boundsHeight / 2;

        // Offset dari center bounds ke edge bounds di arah attack
        float edgeOffsetX = Math.abs(direction.x) * radiusX;
        float edgeOffsetY = Math.abs(direction.y) * radiusY;
        float edgeOffset = (float) Math.sqrt(edgeOffsetX * edgeOffsetX + edgeOffsetY * edgeOffsetY);

        // Create shared hit registry effectively prevents double damage
        java.util.Set<GameCharacter> sharedHits = new java.util.HashSet<>();

        // 1. MAIN ATTACK (Visible, Reach)
        // Titik awal hitbox: dari edge collision bounds + 5px (original visual offset)
        // Ini memastikan visual slash sesuai dengan jarak yang diharapkan user
        float hitboxStartX = boundsCenterX + (direction.x * (edgeOffset + 5));
        float hitboxStartY = boundsCenterY + (direction.y * (edgeOffset + 5));

        // Extend hitbox sejauh 'range' dari titik awal
        float hitboxCenterX = hitboxStartX + (direction.x * range / 2);
        float hitboxCenterY = hitboxStartY + (direction.y * range / 2);

        // Posisi hitbox (pojok kiri-bawah untuk rectangle)
        float hitboxX = hitboxCenterX - (width / 2);
        float hitboxY = hitboxCenterY - (width / 2);

        // Hitung damage berdasarkan ATK karakter
        float finalDamage = attacker.getAtk() * damageMultiplier;

        // Use Template Method pattern - get animation type from character
        // polymorphically
        String animationType = attacker.getAttackAnimationType();

        // Hitung rotation angle dari direction vector
        // atan2 returns angle in radians, convert to degrees
        // atan2(y, x) gives angle from positive x-axis (right = 0°, up = 90°, left =
        // 180°, down = -90°/270°)
        float rotationAngle = (float) Math.toDegrees(Math.atan2(direction.y, direction.x));

        // Buat MeleeAttack baru dengan animation dan rotation
        MeleeAttack mainAttack = new MeleeAttack(
                hitboxX,
                hitboxY,
                width,
                width, // Hitbox berbentuk persegi untuk sekarang
                finalDamage,
                duration,
                animationType, // Pass animation type
                rotationAngle); // Pass rotation angle in degrees

        mainAttack.setSharedHitRegistry(sharedHits);
        meleeAttacks.add(mainAttack);

        // 2. POINT BLANK ATTACK (Invisible, Collision Range)
        // Hitbox centered on the character itself to catch enemies standing on top/inside
        float pbWidth = width * 0.8f; // Slightly smaller to avoid hitting behind unnecessarily
        float pbX = boundsCenterX - (pbWidth / 2);
        float pbY = boundsCenterY - (pbWidth / 2);

        MeleeAttack pbAttack = new MeleeAttack(
                pbX,
                pbY,
                pbWidth,
                pbWidth,
                finalDamage,
                duration,
                animationType,
                rotationAngle);

        pbAttack.setVisible(false); // Invisible
        pbAttack.setSharedHitRegistry(sharedHits); // Share hits so enemy only takes damage once
        meleeAttacks.add(pbAttack);
    }
}
