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

            Rectangle projBounds = projectile.getBounds();

            for (BaseEnemy enemy : enemyPool.getActiveEnemies()) {
                if (enemy.isDead())
                    continue;

                if (projBounds.overlaps(enemy.getBounds())) {
                    float damage = projectile.getDamage();
                    if (player instanceof com.kelompok2.frontend.entities.Insania && enemy.isInsane()) {
                        damage *= 1.5f;
                        System.out.println("[Insania] Bonus damage vs insane: " + damage);
                    }
                    enemy.takeDamage(damage);
                    projectile.active = false;

                    if (enemy.isDead()) {
                        handleEnemyKilled(enemy);
                    }
                    break;
                }
            }
        }
    }

    public void checkPlayerProjectilesVsBoss(Boss boss) {
        for (Projectile projectile : projectilePool.getActiveProjectiles()) {
            if (!projectile.active)
                continue;

            if (projectile.getBounds().overlaps(boss.getBounds())) {
                boss.takeDamage(projectile.getDamage());
                projectile.active = false;
                System.out.println("[Collision] Boss hit! HP: " + boss.getHp() + "/" + boss.getMaxHp());
                break;
            }
        }
    }

    public void checkBossProjectilesVsPlayer(Boss boss) {
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
}
