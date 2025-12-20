package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class FireballSkill extends BaseSkill {

    public FireballSkill() {
        super("Fireball", "Shoots a large fireball (Arts × 3.0)", 8f); // 8 second cooldown
    }

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {
        // Calculate direction to target
        Vector2 userCenter = new Vector2(
                user.getPosition().x + user.getVisualWidth() / 2,
                user.getPosition().y + user.getVisualHeight() / 2);

        Vector2 direction = new Vector2(targetPos).sub(userCenter).nor();

        // Calculate damage: Arts × 3.0
        float damage = user.getArts() * 3.0f;

        // Create fireball projectile using existing Projectile API
        if (projectiles != null) {
            // Try to reuse inactive projectile from pool
            Projectile fireball = null;
            for (Projectile p : projectiles) {
                if (!p.active) {
                    fireball = p;
                    break;
                }
            }

            // If no inactive projectile, create new one using proper constructor
            if (fireball == null) {
                fireball = new Projectile(userCenter.x, userCenter.y, direction, damage);
                projectiles.add(fireball);
            } else {
                // Reset existing projectile using reset method
                fireball.reset(userCenter.x, userCenter.y, direction, damage);
            }

            fireball.setFireball(true); // Mark as fireball for orange color

            System.out.println("[Fireball] Launched! Damage: " + damage + ", Direction: " + direction);
        }
        return true;
    }

    @Override
    public Skill copy() {
        return new FireballSkill();
    }
}
