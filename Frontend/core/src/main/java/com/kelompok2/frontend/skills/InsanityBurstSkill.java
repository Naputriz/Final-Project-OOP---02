package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

// Insanity Burst - Ultimate from Boss Insania (one-time use, Arts×3.0, 500px AoE with insanity effect)
public class InsanityBurstSkill extends BaseSkill {

    private float radius = 500f; // Massive 500px radius
    private float damageMultiplier = 3.0f; // Arts × 3.0
    private float insanityDuration = 5.0f; // Default 5s
    private com.kelompok2.frontend.managers.GameEventManager eventManager;

    @Override
    public float getRadius() {
        return radius;
    }

    // Enemy array untuk damage (akan diset dari GameScreen saat activate)
    private Array<com.kelompok2.frontend.entities.BaseEnemy> enemies;

    // Boss untuk damage (optional)
    private com.kelompok2.frontend.entities.Boss currentBoss;

    public InsanityBurstSkill() {
        super("Insanity Burst", "ULTIMATE: Massive AoE Insanity (500px, Arts×3.0)", 0f); // 0 cooldown (one-time)
    }

    // Set enemy array before activate (called from GameScreen)
    public void setEnemies(Array<com.kelompok2.frontend.entities.BaseEnemy> enemies) {
        this.enemies = enemies;
    }

    // Set boss before activate (optional)
    public void setBoss(com.kelompok2.frontend.entities.Boss boss) {
        this.currentBoss = boss;
    }

    public void setEventManager(com.kelompok2.frontend.managers.GameEventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {

        if (enemies == null) {
            System.out.println("[Insanity Burst] No enemies array set!");
            return false;
        }

        // Calculate center position (around user)
        float centerX = user.getPosition().x + user.getVisualWidth() / 2;
        float centerY = user.getPosition().y + user.getVisualHeight() / 2;

        // Apply damage and insanity to all enemies in radius
        float damage = user.getArts() * damageMultiplier;
        int affectedCount = 0;

        for (com.kelompok2.frontend.entities.BaseEnemy enemy : enemies) {
            if (enemy.isDead())
                continue; // Skip already dead enemies

            float enemyCenterX = enemy.getPosition().x + enemy.getVisualWidth() / 2;
            float enemyCenterY = enemy.getPosition().y + enemy.getVisualHeight() / 2;

            float dx = enemyCenterX - centerX;
            float dy = enemyCenterY - centerY;
            float distanceSq = dx * dx + dy * dy;

            if (distanceSq <= radius * radius) {
                // Apply insanity effect
                enemy.makeInsane(insanityDuration); // Use configurable duration

                // Deal psychic damage
                enemy.takeDamage(damage);

                if (eventManager != null) {
                    eventManager
                            .publish(new com.kelompok2.frontend.events.EnemyDamagedEvent(enemy, damage, true));
                }

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

            if (bossDistanceSq <= radius * radius) {
                currentBoss.makeInsane(insanityDuration); // Use configurable duration
                currentBoss.takeDamage(damage);

                if (eventManager != null) {
                    eventManager.publish(
                            new com.kelompok2.frontend.events.EnemyDamagedEvent(currentBoss, damage, true));
                }
                System.out.println("[ULTIMATE] Insanity Burst hit boss! Damage: " + damage + ", Insanity applied!");
            }
        }

        System.out.println("[ULTIMATE] Insanity Burst! Affected " + affectedCount +
                " enemies with " + damage + " damage each!");
        return true;
    }

    @Override
    public Skill copy() {
        InsanityBurstSkill copy = new InsanityBurstSkill();
        copy.radius = this.radius;
        copy.damageMultiplier = this.damageMultiplier;
        copy.insanityDuration = this.insanityDuration;
        copy.description = this.description;
        copy.setEventManager(this.eventManager);
        return copy;
    }

    @Override
    public void onEquip(GameCharacter owner) {
        // Bonus for Insania: Longer Insanity (5s -> 10s) and massive damage (3.0 ->
        // 5.0)
        if (owner instanceof com.kelompok2.frontend.entities.Insania) {
            this.insanityDuration = 10.0f;
            this.damageMultiplier = 5.0f;
            this.description = "ULTIMATE: Massive AoE Insanity (10s, Arts×5.0) - INSANIA COMBO!";
            System.out.println("[Insanity Burst] Combo activated for Insania! Duration 10s, Multiplier 5.0x.");
        }
    }
}
