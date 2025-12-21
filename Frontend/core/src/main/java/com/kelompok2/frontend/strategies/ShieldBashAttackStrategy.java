package com.kelompok2.frontend.strategies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class ShieldBashAttackStrategy extends MeleeAttackStrategy {

    private float defenseScaling; // Scaling dari DEF stat (contoh: 0.5 untuk 50% DEF)
    private float dashDistance; // Jarak dash forward (pixels)

    public ShieldBashAttackStrategy(float range, float width, float atkMultiplier,
            float defenseScaling, float duration, float dashDistance) {
        super(range, width, atkMultiplier, duration);
        this.defenseScaling = defenseScaling;
        this.dashDistance = dashDistance;
    }

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

        // Create shared hit registry effectively prevents double damage
        java.util.Set<GameCharacter> sharedHits = new java.util.HashSet<>();

        // 1. MAIN ATTACK (Visible, Reach)
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
        MeleeAttack mainAttack = new MeleeAttack(
                hitboxX,
                hitboxY,
                width,
                width,
                finalDamage,
                0.25f, // duration
                animationType,
                rotationAngle);

        mainAttack.setSharedHitRegistry(sharedHits);
        meleeAttacks.add(mainAttack);

        // 2. POINT BLANK ATTACK (Invisible, Collision Range)
        // Hitbox centered on the character itself to catch enemies standing on
        // top/inside
        // NOTE: boundsCenterX/Y are already based on NEW position after dash, so this
        // works!
        float pbWidth = width * 0.8f; // Slightly smaller to avoid hitting behind unnecessarily
        float pbX = boundsCenterX - (pbWidth / 2);
        float pbY = boundsCenterY - (pbWidth / 2);

        MeleeAttack pbAttack = new MeleeAttack(
                pbX,
                pbY,
                pbWidth,
                pbWidth,
                finalDamage,
                0.25f, // duration
                animationType,
                rotationAngle);

        pbAttack.setVisible(false); // Invisible
        pbAttack.setSharedHitRegistry(sharedHits); // Share hits so enemy only takes damage once
        meleeAttacks.add(pbAttack);

        System.out.println("[ShieldBash] Dash " + dashDistance + "px, Damage: ATK(" +
                String.format("%.1f", atkDamage) + ") + DEF(" +
                String.format("%.1f", defDamage) + ") = " +
                String.format("%.1f", finalDamage));
    }
}
