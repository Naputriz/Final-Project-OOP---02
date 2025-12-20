package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

// Frozen Apocalypse - Ultimate from Boss Isolde (one-time use, screen-wide freeze, Arts×2.5)
public class FrozenApocalypseSkill extends BaseSkill {

    private static final float DAMAGE_MULTIPLIER = 2.5f; // Arts × 2.5

    @Override
    public float getRadius() {
        return 2000f; // Screen-wide (larger than viewport diagonal)
    }

    // Enemy array untuk damage
    private Array<DummyEnemy> enemies;

    // Boss untuk damage (optional)
    private com.kelompok2.frontend.entities.Boss currentBoss;

    public FrozenApocalypseSkill() {
        super("Frozen Apocalypse", "ULTIMATE: Screen-wide freeze (Arts×2.5, 5s freeze)", 0f); // 0 cooldown (one-time)
    }

    // Set enemy array before activate
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
            System.out.println("[Frozen Apocalypse] No enemies array set!");
            return;
        }

        // Screen-wide effect - affect ALL enemies regardless of position
        float damage = user.getArts() * DAMAGE_MULTIPLIER;
        int affectedCount = 0;

        for (DummyEnemy enemy : enemies) {
            if (enemy.isDead())
                continue; // Skip already dead enemies

            enemy.takeDamage(damage);
            enemy.freeze();

            // Grant XP if enemy was killed
            if (enemy.isDead()) {
                user.gainXp(enemy.getXpReward());
                System.out.println("[Frozen Apocalypse] Killed enemy, granted " + enemy.getXpReward() + " XP!");
            }
            affectedCount++;
        }

        // Also affect boss if present
        if (currentBoss != null && !currentBoss.isDead()) {
            currentBoss.takeDamage(damage); // Damage first
            currentBoss.freeze(5.0f); // 5 second freeze after damage
            System.out.println("[ULTIMATE] Frozen Apocalypse hit boss! Damage: " + damage);
        }

        System.out.println("[ULTIMATE] Frozen Apocalypse! Froze and damaged " + affectedCount +
                " enemies with " + damage + " damage each!");
    }

    @Override
    public Skill copy() {
        return new FrozenApocalypseSkill();
    }
}
