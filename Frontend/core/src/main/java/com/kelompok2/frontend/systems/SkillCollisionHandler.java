package com.kelompok2.frontend.systems;

import com.kelompok2.frontend.entities.BaseEnemy;
import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.events.EnemyKilledEvent;
import com.kelompok2.frontend.managers.GameEventManager;
// import com.kelompok2.frontend.events.PlayerDamagedEvent; // Handled in GameCharacter
import com.kelompok2.frontend.pools.EnemyPool;
import com.kelompok2.frontend.skills.HurricaneBindSkill;

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
        // Kei: Hallucina Mist (Phantom Haze)
        if (player instanceof com.kelompok2.frontend.entities.Kei) {
            com.kelompok2.frontend.entities.Kei kei = (com.kelompok2.frontend.entities.Kei) player;
            if (kei.shouldShowPhantomHazeCircle()) {
                long activationId = kei.getPhantomHazeActivationId();
                float radius = kei.getSkillRadius();
                float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;

                for (BaseEnemy enemy : enemyPool.getActiveEnemies()) {
                    if (enemy.isDead())
                        continue;
                    if (enemy.wasHitByPhantomHaze(activationId))
                        continue;

                    float enemyCenterX = enemy.getPosition().x + enemy.getVisualWidth() / 2;
                    float enemyCenterY = enemy.getPosition().y + enemy.getVisualHeight() / 2;
                    float distance = (float) Math.sqrt(
                            Math.pow(enemyCenterX - playerCenterX, 2) +
                                    Math.pow(enemyCenterY - playerCenterY, 2));

                    if (distance <= radius) {
                        float damage = player.getArts() * 0.5f;
                        enemy.hallucinate(5.0f);
                        enemy.takeDamage(damage);
                        enemy.markPhantomHazeHit(activationId);
                        eventManager.publish(new com.kelompok2.frontend.events.EnemyDamagedEvent(enemy, damage, true));

                        if (enemy.isDead())
                            handleEnemyKilled(enemy);
                    }
                }
            }
        }

        // Isolde: Glacial Breath
        if (player instanceof com.kelompok2.frontend.entities.Isolde) {
            com.kelompok2.frontend.entities.Isolde isolde = (com.kelompok2.frontend.entities.Isolde) player;
            for (com.kelompok2.frontend.entities.GlacialBreath gb : isolde.getGlacialBreaths()) {
                if (!gb.isActive())
                    continue;

                for (BaseEnemy enemy : enemyPool.getActiveEnemies()) {
                    if (enemy.isDead())
                        continue;

                    if (gb.canHit(enemy)) {
                        enemy.takeDamage(gb.getDamage());
                        enemy.freeze();
                        gb.markAsHit(enemy);
                        eventManager.publish(
                                new com.kelompok2.frontend.events.EnemyDamagedEvent(enemy, gb.getDamage(), true));

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

                for (BaseEnemy enemy : enemyPool.getActiveEnemies()) {
                    if (enemy.isDead())
                        continue;

                    if (pillarBounds.overlaps(enemy.getBounds())) {
                        float damagePerSecond = blaze.getArts() * 1.25f;
                        float damage = damagePerSecond * currentDelta;
                        enemy.takeDamage(damage);
                        // Publish event effectively for DoT, relying on small numbers accumulating
                        // visually or just showing small numbers
                        if (damage > 0.1f) { // Lower threshold to 0.1 to catch smaller ticks
                            eventManager
                                    .publish(new com.kelompok2.frontend.events.EnemyDamagedEvent(enemy, damage, true));
                        }

                        if (enemy.isDead())
                            handleEnemyKilled(enemy);
                    }
                }
            }
        }

        // Whisperwind: Hurricane Bind
        if (player instanceof com.kelompok2.frontend.entities.Whisperwind) {
            if (player.getInnateSkill() instanceof HurricaneBindSkill) {
                HurricaneBindSkill hurricaneSkill = (HurricaneBindSkill) player.getInnateSkill();

                for (Projectile hurricane : hurricaneSkill.getActiveProjectiles()) {
                    if (!hurricane.active) continue;

                    for (BaseEnemy enemy : enemyPool.getActiveEnemies()) {
                        if (enemy.isDead()) continue;

                        if (hurricane.getBounds().overlaps(enemy.getBounds())) {

                            // 1. Check if we already hit this enemy (prevents hitting same enemy every frame)
                            if (hurricane.canHit(enemy)) {
                                enemy.takeDamage(hurricane.getDamage());
                                enemy.stun(3.0f);

                                // 2. Add to hit list
                                hurricane.addHit(enemy);

                                eventManager.publish(new com.kelompok2.frontend.events.EnemyDamagedEvent(enemy, hurricane.getDamage(), true));

                                if (enemy.isDead()) handleEnemyKilled(enemy);

                                // 3. Only destroy if NOT piercing
                                if (!hurricane.isPiercing()) {
                                    hurricane.active = false;
                                    break;
                                }
                            }
                        }
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

                for (BaseEnemy enemy : enemyPool.getActiveEnemies()) {
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
                        enemy.makeInsane(1.5f);
                        enemy.takeDamage(damage);
                        enemy.markMindFractureHit(activationId);
                        eventManager.publish(new com.kelompok2.frontend.events.EnemyDamagedEvent(enemy, damage, true));

                        if (enemy.isDead())
                            handleEnemyKilled(enemy);
                    }
                }
            }
        }
    }

    public void checkPlayerSkillsVsBoss(Boss boss) {
        // Kei
        if (player instanceof com.kelompok2.frontend.entities.Kei) {
            com.kelompok2.frontend.entities.Kei kei = (com.kelompok2.frontend.entities.Kei) player;
            if (kei.shouldShowPhantomHazeCircle()) {
                long activationId = kei.getPhantomHazeActivationId();
                if (!boss.wasHitByPhantomHaze(activationId)) {
                    float radius = kei.getSkillRadius();
                    float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                    float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
                    float bossCenterX = boss.getPosition().x + boss.getVisualWidth() / 2;
                    float bossCenterY = boss.getPosition().y + boss.getVisualHeight() / 2;

                    float distance = (float) Math.sqrt(
                            Math.pow(bossCenterX - playerCenterX, 2) +
                                    Math.pow(bossCenterY - playerCenterY, 2));

                    if (distance <= radius) {
                        float damage = player.getArts() * 0.5f;
                        boss.hallucinate(3.0f); // Less duration on boss
                        boss.takeDamage(damage);
                        boss.markPhantomHazeHit(activationId);
                        eventManager.publish(new com.kelompok2.frontend.events.EnemyDamagedEvent(boss, damage, true));
                    }
                }
            }
        }

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
                    eventManager
                            .publish(new com.kelompok2.frontend.events.EnemyDamagedEvent(boss, gb.getDamage(), true));
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
                    if (damage > 0.1f) {
                        eventManager.publish(new com.kelompok2.frontend.events.EnemyDamagedEvent(boss, damage, true));
                    }
                }
            }
        }

        // Whisperwind
        if (player instanceof com.kelompok2.frontend.entities.Whisperwind) {
            if (player.getInnateSkill() instanceof HurricaneBindSkill) {
                HurricaneBindSkill hurricaneSkill = (HurricaneBindSkill) player.getInnateSkill();

                for (Projectile hurricane : hurricaneSkill.getActiveProjectiles()) {
                    if (!hurricane.active) continue;

                    if (hurricane.getBounds().overlaps(boss.getBounds())) {
                        if (hurricane.canHit(boss)) {
                            boss.takeDamage(hurricane.getDamage());
                            boss.stun(3.0f);

                            hurricane.addHit(boss);

                            eventManager.publish(
                                new com.kelompok2.frontend.events.EnemyDamagedEvent(boss, hurricane.getDamage(), true));

                            if (!hurricane.isPiercing()) {
                                hurricane.active = false;
                            }
                        }
                    }
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
                        eventManager.publish(new com.kelompok2.frontend.events.EnemyDamagedEvent(boss, damage, true));
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
                    float damagePerSecond = bossBlaze.getArts() * 35.0f; // Increased to compensate for 0.5s warning
                                                                         // delay
                    float damage = damagePerSecond * currentDelta;
                    player.takeDamage(damage, boss);
                    // eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                    // // HANDLED IN GameCharacter
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
                    // eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                    // // HANDLED IN GameCharacter
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
                        player.makeInsane(0.5f);
                        // eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                        // // HANDLED IN GameCharacter
                        lastMindFractureHitOnPlayer = activationId;
                    }
                }
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
