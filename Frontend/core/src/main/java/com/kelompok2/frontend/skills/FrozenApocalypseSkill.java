package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

// Frozen Apocalypse - Ultimate from Boss Isolde (one-time use, screen-wide freeze, Arts×2.5)
public class FrozenApocalypseSkill extends BaseSkill {

    private float damageMultiplier = 2.5f;

    @Override
    public float getRadius() {
        return 2000f; // Screen-wide (larger than viewport diagonal)
    }

    // Enemy array untuk damage
    private Array<com.kelompok2.frontend.entities.BaseEnemy> enemies;

    // Boss untuk damage (optional)
    private com.kelompok2.frontend.entities.Boss currentBoss;

    public FrozenApocalypseSkill() {
        super("Frozen Apocalypse", "ULTIMATE: Screen-wide freeze (Arts×2.5, 5s freeze)", 0f); // 0 cooldown (one-time)
    }

    // Set enemy array before activate
    public void setEnemies(Array<com.kelompok2.frontend.entities.BaseEnemy> enemies) {
        this.enemies = enemies;
    }

    // Set boss before activate (optional)
    public void setBoss(com.kelompok2.frontend.entities.Boss boss) {
        this.currentBoss = boss;
    }

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {

        if (enemies == null) {
            System.out.println("[Frozen Apocalypse] No enemies array set!");
            return false;
        }

        // Screen-wide effect - affect ALL enemies regardless of position
        float damage = user.getArts() * damageMultiplier;
        int affectedCount = 0;

        for (com.kelompok2.frontend.entities.BaseEnemy enemy : enemies) {
            if (enemy.isDead())
                continue; // Skip already dead enemies

            // Freeze first, then damage (so if they die, logic holds)
            // But usually modify state then damage.
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
        return true;
    }

    @Override
    public Skill copy() {
        FrozenApocalypseSkill copy = new FrozenApocalypseSkill();
        copy.damageMultiplier = this.damageMultiplier;
        copy.description = this.description;
        return copy;
    }

    @Override
    public void onEquip(GameCharacter owner) {
        // Bonus for Isolde: Stronger multiplier (2.5 -> 3.5)
        if (owner instanceof com.kelompok2.frontend.entities.Isolde) {
            this.damageMultiplier = 3.5f;
            this.description = "ULTIMATE: Screen-wide freeze (Arts×3.5) - ISOLDE COMBO!";
            System.out.println("[Frozen Apocalypse] Combo activated for Isolde! Multiplier increased to 3.5x.");
        }
    }
}
