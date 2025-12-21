package com.kelompok2.frontend.systems;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.BaseEnemy;
import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.events.EnemyKilledEvent;
import com.kelompok2.frontend.events.PlayerDamagedEvent;
import com.kelompok2.frontend.managers.GameEventManager;
import com.kelompok2.frontend.pools.EnemyPool;
import com.kelompok2.frontend.pools.ProjectilePool;

public class ProjectileCollisionHandler {
    private GameCharacter player;
    private EnemyPool enemyPool;
    private ProjectilePool projectilePool;
    private Array<Projectile> bossProjectiles;
    private GameEventManager eventManager;

    public void initialize(GameCharacter player, EnemyPool enemyPool, ProjectilePool projectilePool,
            Array<Projectile> bossProjectiles, GameEventManager eventManager) {
        this.player = player;
        this.enemyPool = enemyPool;
        this.projectilePool = projectilePool;
        this.bossProjectiles = bossProjectiles;
        this.eventManager = eventManager;
    }

    public void checkPlayerProjectilesVsEnemies() {
        for (Projectile projectile : projectilePool.getActiveProjectiles()) {
            if (!projectile.active)
                continue;

            // ✅ FIX: Skip enemy projectiles in player collision check
            if (projectile.isEnemyProjectile())
                continue;

            Rectangle projBounds = projectile.getBounds();

            for (BaseEnemy enemy : enemyPool.getActiveEnemies()) {
                if (enemy.isDead())
                    continue;

                if (projBounds.overlaps(enemy.getBounds())) {
                    if (!projectile.canHit(enemy))
                        continue; // Skip if already hit by this projectile (piercing)

                    float damage = projectile.getDamage();
                    if (player instanceof com.kelompok2.frontend.entities.Insania && enemy.isInsane()) {
                        damage *= 1.5f;
                        System.out.println("[Insania] Bonus damage vs insane: " + damage);
                    }
                    enemy.takeDamage(damage);

                    // Piercing Logic
                    projectile.addHit(enemy);
                    if (!projectile.isPiercing()) {
                        projectile.active = false;
                    }

                    eventManager.publish(
                            new com.kelompok2.frontend.events.EnemyDamagedEvent(enemy, damage, projectile.isArts()));

                    if (enemy.isDead()) {
                        handleEnemyKilled(enemy);
                    }

                    if (!projectile.active)
                        break;
                }
            }
        }
    }

    public void checkPlayerProjectilesVsBoss(Boss boss) {
        for (Projectile projectile : projectilePool.getActiveProjectiles()) {
            if (!projectile.active)
                continue;

            // ✅ FIX: Skip enemy projectiles against boss
            if (projectile.isEnemyProjectile())
                continue;

            if (projectile.getBounds().overlaps(boss.getBounds())) {
                if (!projectile.canHit(boss))
                    continue;

                float damage = projectile.getDamage();
                boss.takeDamage(damage);

                projectile.addHit(boss);
                if (!projectile.isPiercing()) {
                    projectile.active = false;
                }

                eventManager.publish(
                        new com.kelompok2.frontend.events.EnemyDamagedEvent(boss, damage, projectile.isArts()));
                System.out.println("[Collision] Boss hit! HP: " + boss.getHp() + "/" + boss.getMaxHp());
                if (!projectile.active)
                    break;
            }
        }
    }

    public void checkBossProjectilesVsPlayer(Boss boss) {
        // Ryze Spectral Body Fix
        if (player.isInvulnerable()) {
            return;
        }

        for (Projectile projectile : bossProjectiles) {
            if (!projectile.active)
                continue;

            if (projectile.getBounds().overlaps(player.getBounds())) {
                float damage = projectile.getDamage();
                player.takeDamage(damage, boss);
                projectile.active = false;
                eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                System.out.println("[Collision] Player hit by projectile! HP: " + player.getHp());
                break;
            }
        }
    }

    private void handleEnemyKilled(BaseEnemy enemy) {
        float xpGain = enemy.getXpReward();
        player.gainXp(xpGain);
        eventManager.publish(new EnemyKilledEvent(enemy, player, xpGain));
        System.out.println("[Collision] Enemy killed! XP gained: " + xpGain);
    }

    public void checkEnemyProjectilesVsPlayer() {
        // Ryze Spectral Body Fix
        if (player.isInvulnerable()) {
            return;
        }

        for (Projectile projectile : projectilePool.getActiveProjectiles()) {
            if (!projectile.active)
                continue;

            // ✅ FIX: Only check enemy projectiles
            if (!projectile.isEnemyProjectile())
                continue;

            if (projectile.getBounds().overlaps(player.getBounds())) {
                float damage = projectile.getDamage();
                player.takeDamage(damage, null); // Attacker unknown for generic projectiles
                projectile.active = false;
                eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                System.out.println("[Collision] Player hit by Ranged Enemy! HP: " + player.getHp());
            }
        }
    }
}
