package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

// Insanity Burst - Ultimate from Boss Insania (one-time use, Arts×3.0, 500px AoE with insanity effect)
public class InsanityBurstSkill extends BaseSkill {

    private static final float RADIUS = 500f; // Massive 500px radius
    private static final float DAMAGE_MULTIPLIER = 3.0f; // Arts × 3.0

    @Override
    public float getRadius() {
        return RADIUS;
    }

    // Enemy array untuk damage (akan diset dari GameScreen saat activate)
    private Array<DummyEnemy> enemies;

    // Boss untuk damage (optional)
    private com.kelompok2.frontend.entities.Boss currentBoss;

    public InsanityBurstSkill() {
        super("Insanity Burst", "ULTIMATE: Massive AoE Insanity (500px, Arts×3.0)", 0f); // 0 cooldown (one-time)
    }

    // Set enemy array before activate (called from GameScreen)
    public void setEnemies(Array<DummyEnemy> enemies) {
        this.enemies = enemies;
    }

    // Set boss before activate (optional)
    public void setBoss(com.kelompok2.frontend.entities.Boss boss) {
        this.currentBoss = boss;
    }

    @Override
    protected void executeSkill(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {

        if (enemies == null) {
            System.out.println("[Insanity Burst] No enemies array set!");
            return;
        }

        // Calculate center position (around user)
        float centerX = user.getPosition().x + user.getVisualWidth() / 2;
        float centerY = user.getPosition().y + user.getVisualHeight() / 2;

        // Apply damage and insanity to all enemies in radius
        float damage = user.getArts() * DAMAGE_MULTIPLIER;
        int affectedCount = 0;

        for (DummyEnemy enemy : enemies) {
            if (enemy.isDead())
                continue; // Skip already dead enemies

            float enemyCenterX = enemy.getPosition().x + enemy.getVisualWidth() / 2;
            float enemyCenterY = enemy.getPosition().y + enemy.getVisualHeight() / 2;

            float dx = enemyCenterX - centerX;
            float dy = enemyCenterY - centerY;
            float distanceSq = dx * dx + dy * dy;

            if (distanceSq <= RADIUS * RADIUS) {
                // Apply insanity effect
                enemy.makeInsane(5.0f); // Use GameCharacter's system

                // Deal psychic damage
                enemy.takeDamage(damage);

                // Grant XP if enemy was killed
                if (enemy.isDead()) {
                    user.gainXp(enemy.getXpReward());
                    System.out.println("[Insanity Burst] Killed enemy, granted " + enemy.getXpReward() + " XP!");
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
                currentBoss.makeInsane(5.0f); // 5 second insanity
                currentBoss.takeDamage(damage);
                System.out.println("[ULTIMATE] Insanity Burst hit boss! Damage: " + damage + ", Insanity applied!");
            }
        }

        System.out.println("[ULTIMATE] Insanity Burst! Affected " + affectedCount +
                " enemies with " + damage + " damage each!");
    }

    @Override
    public Skill copy() {
        return new InsanityBurstSkill();
    }
}
