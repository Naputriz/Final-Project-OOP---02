package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

// Inferno Nova - Ultimate skill from Boss Blaze (one-time use, Arts×4.0, 400px AoE)
public class InfernoNovaSkill extends BaseSkill {

    private static final float RADIUS = 400f; // 400px radius
    private static final float DAMAGE_MULTIPLIER = 4.0f; // Arts × 4.0 (highest damage)

    @Override
    public float getRadius() {
        return RADIUS;
    }

    // Enemy array untuk damage
    private Array<com.kelompok2.frontend.entities.BaseEnemy> enemies;

    // Boss untuk damage (optional)
    private com.kelompok2.frontend.entities.Boss currentBoss;

    public InfernoNovaSkill() {
        super("Inferno Nova", "ULTIMATE: Massive fire explosion (400px, Arts×4.0)", 0f); // 0 cooldown (one-time)
    }

    // Set enemy array (called by GameScreen when unlocking ultimate)
    public void setEnemies(Array<com.kelompok2.frontend.entities.BaseEnemy> enemies) {
        this.enemies = enemies;
    }

    // Set boss (optional)
    public void setBoss(com.kelompok2.frontend.entities.Boss boss) {
        this.currentBoss = boss;
    }

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {

        if (enemies == null) {
            System.out.println("[ERROR] InfernoNovaSkill: No enemy array set!");
            return false;
        }

        System.out.println("[ULTIMATE] INFERNO NOVA ACTIVATED!");

        // Calculate center position
        float centerX = user.getPosition().x + user.getVisualWidth() / 2;
        float centerY = user.getPosition().y + user.getVisualHeight() / 2;

        // Deal massive damage to all enemies in radius
        float damage = user.getArts() * DAMAGE_MULTIPLIER;
        int affectedCount = 0;

        for (com.kelompok2.frontend.entities.BaseEnemy enemy : enemies) {
            if (enemy.isDead())
                continue; // Skip already dead enemies

            float enemyCenterX = enemy.getPosition().x + enemy.getVisualWidth() / 2;
            float enemyCenterY = enemy.getPosition().y + enemy.getVisualHeight() / 2;

            float dx = enemyCenterX - centerX;
            float dy = enemyCenterY - centerY;
            float distanceSq = dx * dx + dy * dy;

            if (distanceSq <= RADIUS * RADIUS) {
                // Deal massive fire damage
                enemy.takeDamage(damage);

                // Grant XP if enemy was killed
                if (enemy.isDead()) {
                    user.gainXp(enemy.getXpReward());
                    System.out.println("[Inferno Nova] Killed enemy, granted " + enemy.getXpReward() + " XP!");
                }
                affectedCount++;
            }
        }

        // Also affect boss if present and in radius
        if (currentBoss != null && !currentBoss.isDead()) {
            float bossCenterX = currentBoss.getPosition().x + currentBoss.getVisualWidth() / 2;
            float bossCenterY = currentBoss.getPosition().y + currentBoss.getVisualHeight() / 2;
            float bossDx = bossCenterX - centerX;
            float bossDy = bossCenterY - centerY;
            float bossDistanceSq = bossDx * bossDx + bossDy * bossDy;

            if (bossDistanceSq <= RADIUS * RADIUS) {
                currentBoss.takeDamage(damage);
                System.out.println("[ULTIMATE] Inferno Nova hit boss! Massive damage: " + damage);
            }
        }

        System.out.println("[ULTIMATE] Inferno Nova! Incinerated " + affectedCount +
                " enemies with " + damage + " damage each!");
        return true;
    }

    @Override
    public Skill copy() {
        return new InfernoNovaSkill();
    }
}
