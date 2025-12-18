package com.kelompok2.frontend.strategies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

/**
 * Shield Bash attack strategy untuk Aegis
 * Extends MeleeAttackStrategy dengan damage scaling dari ATK + DEF
 * dan dash forward movement saat attack
 */
public class ShieldBashAttackStrategy extends MeleeAttackStrategy {

    private float defenseScaling; // Scaling dari DEF stat (contoh: 0.5 untuk 50% DEF)
    private float dashDistance; // Jarak dash forward (pixels)

    /**
     * @param range          Jangkauan serangan
     * @param width          Lebar hitbox
     * @param atkMultiplier  Multiplier untuk ATK (default 0.7)
     * @param defenseScaling Multiplier untuk DEF (default 0.5)
     * @param duration       Durasi hitbox aktif
     * @param dashDistance   Jarak dash forward saat attack
     */
    public ShieldBashAttackStrategy(float range, float width, float atkMultiplier,
            float defenseScaling, float duration, float dashDistance) {
        super(range, width, atkMultiplier, duration);
        this.defenseScaling = defenseScaling;
        this.dashDistance = dashDistance;
    }

    /**
     * Default constructor untuk Aegis
     * Range: 90px (sedikit lebih jauh dari melee biasa)
     * Width: 70px (shield bash lebih luas)
     * ATK scaling: 0.7x (lower base damage)
     * DEF scaling: 0.5x (compensates with defense)
     * Duration: 0.25s
     * Dash: 30px forward
     */
    public ShieldBashAttackStrategy() {
        this(90f, 70f, 0.7f, 0.5f, 0.25f, 30f);
    }

    @Override
    public void execute(GameCharacter attacker, Vector2 targetPos,
            Array<Projectile> projectiles, Array<MeleeAttack> meleeAttacks) {

        // Hitung posisi tengah karakter
        float centerX = attacker.getPosition().x + attacker.getVisualWidth() / 2;
        float centerY = attacker.getPosition().y + attacker.getVisualHeight() / 2;

        // Hitung arah serangan (dari karakter ke target)
        Vector2 direction = new Vector2(targetPos.x - centerX, targetPos.y - centerY).nor();

        // Update facing direction
        if (direction.x > 0) {
            attacker.setFacingRight(true);
        } else if (direction.x < 0) {
            attacker.setFacingRight(false);
        }

        // DASH FORWARD: Gerakkan karakter sedikit ke depan saat shield bash
        Vector2 dashMovement = direction.cpy().scl(dashDistance);
        Vector2 newPosition = attacker.getPosition().cpy().add(dashMovement);
        attacker.setPosition(newPosition.x, newPosition.y);

        // Calculate hitbox position (sama seperti MeleeAttackStrategy parent)
        float boundsX = attacker.getBounds().x;
        float boundsY = attacker.getBounds().y;
        float boundsWidth = attacker.getBounds().width;
        float boundsHeight = attacker.getBounds().height;

        float boundsCenterX = boundsX + boundsWidth / 2;
        float boundsCenterY = boundsY + boundsHeight / 2;

        float radiusX = boundsWidth / 2;
        float radiusY = boundsHeight / 2;

        float edgeOffsetX = Math.abs(direction.x) * radiusX;
        float edgeOffsetY = Math.abs(direction.y) * radiusY;
        float edgeOffset = (float) Math.sqrt(edgeOffsetX * edgeOffsetX + edgeOffsetY * edgeOffsetY);

        float hitboxStartX = boundsCenterX + (direction.x * (edgeOffset + 5));
        float hitboxStartY = boundsCenterY + (direction.y * (edgeOffset + 5));

        // Get range and width from parent
        float range = 90f; // Hardcoded for now (sesuai constructor default)
        float width = 70f;

        float hitboxCenterX = hitboxStartX + (direction.x * range / 2);
        float hitboxCenterY = hitboxStartY + (direction.y * range / 2);

        float hitboxX = hitboxCenterX - (width / 2);
        float hitboxY = hitboxCenterY - (width / 2);

        // CUSTOM DAMAGE CALCULATION: ATK scaling + DEF scaling
        // Contoh: (ATK × 0.7) + (DEF × 0.5)
        // Aegis dengan ATK=15, DEF=40 -> (15×0.7) + (40×0.5) = 10.5 + 20 = 30.5 damage
        float atkDamage = attacker.getAtk() * 0.7f; // ATK contribution
        float defDamage = attacker.getDef() * defenseScaling; // DEF contribution
        float finalDamage = atkDamage + defDamage;

        // Get animation type
        String animationType = attacker.getAttackAnimationType();

        // Calculate rotation
        float rotationAngle = (float) Math.toDegrees(Math.atan2(direction.y, direction.x));

        // Create melee attack
        MeleeAttack attack = new MeleeAttack(
                hitboxX,
                hitboxY,
                width,
                width,
                finalDamage,
                0.25f, // duration
                animationType,
                rotationAngle);

        meleeAttacks.add(attack);

        System.out.println("[ShieldBash] Dash " + dashDistance + "px, Damage: ATK(" +
                String.format("%.1f", atkDamage) + ") + DEF(" +
                String.format("%.1f", defDamage) + ") = " +
                String.format("%.1f", finalDamage));
    }
}
