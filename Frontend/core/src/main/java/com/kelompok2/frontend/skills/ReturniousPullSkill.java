package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.pools.EnemyPool;

import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.systems.GameFacade;

public class ReturniousPullSkill extends BaseSkill {

    private EnemyPool enemyPool;
    private GameFacade gameFacade;

    public ReturniousPullSkill() {
        // Cooldown: 12 seconds
        super("Returnious Pull", "Pulls nearest marked enemy + Dmg + Stun.", 12f);
    }

    public void setEnemyPool(EnemyPool enemyPool) {
        this.enemyPool = enemyPool;
    }

    public void setGameFacade(GameFacade gameFacade) {
        this.gameFacade = gameFacade;
    }

    @Override
    public Skill copy() {
        ReturniousPullSkill copy = new ReturniousPullSkill();
        copy.setEnemyPool(this.enemyPool);
        copy.setGameFacade(this.gameFacade);
        return copy;
    }

    @Override
    protected void executeSkill(GameCharacter user, Vector2 targetPos, Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {

        if (enemyPool == null) {
            System.err.println("[ReturniousPullSkill] EnemyPool not set!");
            return;
        }

        // Logic: Find nearest marked enemy (DummyEnemy OR Boss)
        GameCharacter nearestMarkedEnemy = null;
        float minDst = Float.MAX_VALUE;

        // Check Dummy Enemies
        for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
            if (enemy.isDead() || !enemy.isMarked())
                continue;

            float dst = user.getPosition().dst(enemy.getPosition());
            if (dst < minDst) {
                minDst = dst;
                nearestMarkedEnemy = enemy;
            }
        }

        // Check Boss (via Facade)
        if (gameFacade != null) {
            Boss currentBoss = gameFacade.getSpawningSystem().getCurrentBoss();
            if (currentBoss != null && !currentBoss.isDead() && currentBoss.isMarked()) {
                float dst = user.getPosition().dst(currentBoss.getPosition());
                if (dst < minDst) {
                    minDst = dst;
                    nearestMarkedEnemy = currentBoss;
                }
            }
        }

        if (nearestMarkedEnemy != null) {
            // Apply effect
            // Pull enemy to user (offset slightly to avoid being stuck inside)
            Vector2 userPos = user.getPosition();
            Vector2 direction = nearestMarkedEnemy.getPosition().cpy().sub(userPos).nor();

            // Set position distance 40 units from player in direction of enemy
            // Set position distance based on combined radius + small buffer
            float pullDistance = (user.getVisualWidth() / 2f) + (nearestMarkedEnemy.getVisualWidth() / 2f) + 15f;
            Vector2 pullPos = userPos.cpy().add(direction.scl(pullDistance));

            // REMOVED: nearestMarkedEnemy.setPosition(pullPos.x, pullPos.y); (Instant
            // teleport caused bugs)

            // Deal high damage (200% ATK)
            float damage = user.getAtk() * 2.0f;

            // Use the new Pull Mechanic
            // Speed 1200f -> Fast pull (almost instant but travels)
            nearestMarkedEnemy.pull(pullPos, 1200f, damage, 1.0f, user);

            // Visual effect or log
            System.out.println("[Lumi] Pulling marked enemy: " + nearestMarkedEnemy.getClass().getSimpleName());

            // Cooldown is set handled by BaseSkill logic after this returns
        } else {
            System.out.println("[Lumi] No marked enemies found to pull.");
        }
    }
}
