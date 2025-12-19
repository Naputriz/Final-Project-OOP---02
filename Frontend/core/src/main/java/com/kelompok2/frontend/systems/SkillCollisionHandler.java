package com.kelompok2.frontend.systems;

import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.events.EnemyKilledEvent;
import com.kelompok2.frontend.events.PlayerDamagedEvent;
import com.kelompok2.frontend.managers.GameEventManager;
import com.kelompok2.frontend.pools.EnemyPool;

public class SkillCollisionHandler {
    private GameCharacter player;
    private EnemyPool enemyPool;
    private GameEventManager eventManager;
    private float currentDelta;
    private long lastMindFractureHitOnPlayer = -1;

    public void initialize(GameCharacter player, EnemyPool enemyPool, GameEventManager eventManager) {
        this.player = player;
        this.enemyPool = enemyPool;
        this.eventManager = eventManager;
    }

    public void update(float delta) {
        this.currentDelta = delta;
    }

    public void checkPlayerSkillsVsEnemies() {
        // Isolde: Glacial Breath
        if (player instanceof com.kelompok2.frontend.entities.Isolde) {
            com.kelompok2.frontend.entities.Isolde isolde = (com.kelompok2.frontend.entities.Isolde) player;
            for (com.kelompok2.frontend.entities.GlacialBreath gb : isolde.getGlacialBreaths()) {
                if (!gb.isActive())
                    continue;

                for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
                    if (enemy.isDead())
                        continue;

                    if (gb.canHit(enemy)) {
                        enemy.takeDamage(gb.getDamage());
                        enemy.freeze();
                        gb.markAsHit(enemy);

                        if (enemy.isDead())
                            handleEnemyKilled(enemy);
                    }
                }
            }
        }

        // Blaze: Hellfire Pillar
        if (player instanceof com.kelompok2.frontend.entities.Blaze) {
            com.kelompok2.frontend.entities.Blaze blaze = (com.kelompok2.frontend.entities.Blaze) player;
            if (blaze.isPillarActive()) {
                com.badlogic.gdx.math.Rectangle pillarBounds = new com.badlogic.gdx.math.Rectangle(
                        blaze.getPillarPosition().x - blaze.getPillarRadius(),
                        blaze.getPillarPosition().y - blaze.getPillarRadius(),
                        blaze.getPillarRadius() * 2,
                        blaze.getPillarRadius() * 2);

                for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
                    if (enemy.isDead())
                        continue;

                    if (pillarBounds.overlaps(enemy.getBounds())) {
                        float damagePerSecond = blaze.getArts() * 1.25f;
                        float damage = damagePerSecond * currentDelta;
                        enemy.takeDamage(damage);

                        if (enemy.isDead())
                            handleEnemyKilled(enemy);
                    }
                }
            }
        }

        // Whisperwind: Hurricane Bind
        if (player instanceof com.kelompok2.frontend.entities.Whisperwind) {
            com.kelompok2.frontend.entities.Whisperwind whisperwind = (com.kelompok2.frontend.entities.Whisperwind) player;
            for (Projectile hurricane : whisperwind.getHurricaneProjectiles()) {
                if (!hurricane.active)
                    continue;

                for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
                    if (enemy.isDead())
                        continue;

                    if (hurricane.getBounds().overlaps(enemy.getBounds())) {
                        enemy.takeDamage(hurricane.getDamage());
                        enemy.stun(3.0f);
                        hurricane.active = false;

                        if (enemy.isDead())
                            handleEnemyKilled(enemy);
                        break;
                    }
                }
            }
        }

        // Insania: Mind Fracture
        if (player instanceof com.kelompok2.frontend.entities.Insania) {
            com.kelompok2.frontend.entities.Insania insania = (com.kelompok2.frontend.entities.Insania) player;
            if (insania.shouldShowMindFractureCircle()) {
                long activationId = insania.getMindFractureActivationId();
                float radius = insania.getSkillRadius();
                float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;

                for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
                    if (enemy.isDead())
                        continue;
                    if (enemy.wasHitByMindFracture(activationId))
                        continue;

                    float enemyCenterX = enemy.getPosition().x + enemy.getVisualWidth() / 2;
                    float enemyCenterY = enemy.getPosition().y + enemy.getVisualHeight() / 2;
                    float distance = (float) Math.sqrt(
                            Math.pow(enemyCenterX - playerCenterX, 2) +
                                    Math.pow(enemyCenterY - playerCenterY, 2));

                    if (distance <= radius) {
                        float damage = player.getArts() * 0.75f;
                        enemy.makeInsane(5.0f);
                        enemy.takeDamage(damage);
                        enemy.markMindFractureHit(activationId);

                        if (enemy.isDead())
                            handleEnemyKilled(enemy);
                    }
                }
            }
        }
    }

    public void checkPlayerSkillsVsBoss(Boss boss) {
        // Isolde
        if (player instanceof com.kelompok2.frontend.entities.Isolde) {
            com.kelompok2.frontend.entities.Isolde isolde = (com.kelompok2.frontend.entities.Isolde) player;
            for (com.kelompok2.frontend.entities.GlacialBreath gb : isolde.getGlacialBreaths()) {
                if (!gb.isActive())
                    continue;
                if (gb.canHit(boss)) {
                    boss.takeDamage(gb.getDamage());
                    boss.freeze();
                    gb.markAsHit(boss);
                }
            }
        }

        // Blaze
        if (player instanceof com.kelompok2.frontend.entities.Blaze) {
            com.kelompok2.frontend.entities.Blaze blaze = (com.kelompok2.frontend.entities.Blaze) player;
            if (blaze.isPillarActive()) {
                com.badlogic.gdx.math.Rectangle pillarBounds = new com.badlogic.gdx.math.Rectangle(
                        blaze.getPillarPosition().x - blaze.getPillarRadius(),
                        blaze.getPillarPosition().y - blaze.getPillarRadius(),
                        blaze.getPillarRadius() * 2,
                        blaze.getPillarRadius() * 2);

                if (pillarBounds.overlaps(boss.getBounds())) {
                    float damagePerSecond = blaze.getArts() * 1.25f;
                    float damage = damagePerSecond * currentDelta;
                    boss.takeDamage(damage);
                }
            }
        }

        // Whisperwind
        if (player instanceof com.kelompok2.frontend.entities.Whisperwind) {
            com.kelompok2.frontend.entities.Whisperwind whisperwind = (com.kelompok2.frontend.entities.Whisperwind) player;
            for (Projectile hurricane : whisperwind.getHurricaneProjectiles()) {
                if (!hurricane.active)
                    continue;
                if (hurricane.getBounds().overlaps(boss.getBounds())) {
                    boss.takeDamage(hurricane.getDamage());
                    boss.stun(3.0f);
                    hurricane.active = false;
                    break;
                }
            }
        }

        // Insania
        if (player instanceof com.kelompok2.frontend.entities.Insania) {
            com.kelompok2.frontend.entities.Insania insania = (com.kelompok2.frontend.entities.Insania) player;
            if (insania.shouldShowMindFractureCircle()) {
                long activationId = insania.getMindFractureActivationId();
                if (!boss.wasHitByMindFracture(activationId)) {
                    float radius = insania.getSkillRadius();
                    float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                    float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
                    float bossCenterX = boss.getPosition().x + boss.getVisualWidth() / 2;
                    float bossCenterY = boss.getPosition().y + boss.getVisualHeight() / 2;

                    float distance = (float) Math.sqrt(
                            Math.pow(bossCenterX - playerCenterX, 2) +
                                    Math.pow(bossCenterY - playerCenterY, 2));

                    if (distance <= radius) {
                        float damage = player.getArts() * 0.75f;
                        boss.applyInsanity();
                        boss.takeDamage(damage);
                        boss.markMindFractureHit(activationId);
                    }
                }
            }
        }
    }

    public void checkBossSkillsVsPlayer(Boss boss) {
        // BossBlaze
        if (boss instanceof com.kelompok2.frontend.entities.BossBlaze) {
            com.kelompok2.frontend.entities.BossBlaze bossBlaze = (com.kelompok2.frontend.entities.BossBlaze) boss;
            if (bossBlaze.isPillarActive()) {
                com.badlogic.gdx.math.Rectangle pillarBounds = new com.badlogic.gdx.math.Rectangle(
                        bossBlaze.getPillarPosition().x - bossBlaze.getPillarRadius(),
                        bossBlaze.getPillarPosition().y - bossBlaze.getPillarRadius(),
                        bossBlaze.getPillarRadius() * 2,
                        bossBlaze.getPillarRadius() * 2);

                if (pillarBounds.overlaps(player.getBounds())) {
                    float damagePerSecond = bossBlaze.getArts() * 1.25f;
                    float damage = damagePerSecond * currentDelta;
                    player.takeDamage(damage, boss);
                    eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                }
            }
        }

        // BossIsolde
        if (boss instanceof com.kelompok2.frontend.entities.BossIsolde) {
            com.kelompok2.frontend.entities.BossIsolde bossIsolde = (com.kelompok2.frontend.entities.BossIsolde) boss;
            for (com.kelompok2.frontend.entities.GlacialBreath gb : bossIsolde.getGlacialBreaths()) {
                if (!gb.isActive())
                    continue;

                if (gb.canHit(player)) {
                    float damage = gb.getDamage();
                    player.takeDamage(damage, boss);
                    player.freeze(3.0f);
                    gb.markAsHit(player);
                    eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                }
            }
        }

        // BossInsania
        if (boss instanceof com.kelompok2.frontend.entities.BossInsania) {
            com.kelompok2.frontend.entities.BossInsania bossInsania = (com.kelompok2.frontend.entities.BossInsania) boss;
            if (bossInsania.shouldShowMindFractureCircle()) {
                long activationId = bossInsania.getMindFractureActivationId();

                if (lastMindFractureHitOnPlayer != activationId) {
                    float radius = bossInsania.getSkillRadius();
                    float bossCenterX = boss.getPosition().x + boss.getVisualWidth() / 2;
                    float bossCenterY = boss.getPosition().y + boss.getVisualHeight() / 2;
                    float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                    float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;

                    float distance = (float) Math.sqrt(
                            Math.pow(playerCenterX - bossCenterX, 2) +
                                    Math.pow(playerCenterY - bossCenterY, 2));

                    if (distance <= radius) {
                        float damage = boss.getArts() * 0.75f;
                        player.takeDamage(damage, boss);
                        player.makeInsane(5.0f);
                        eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                        lastMindFractureHitOnPlayer = activationId;
                    }
                }
            }
        }
    }

    private void handleEnemyKilled(DummyEnemy enemy) {
        float xpGain = enemy.getXpReward();
        player.gainXp(xpGain);
        eventManager.publish(new EnemyKilledEvent(enemy, player, xpGain));
        System.out.println("[Collision] Enemy killed! XP gained: " + xpGain);
    }
}
