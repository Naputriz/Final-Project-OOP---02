package com.kelompok2.frontend.systems;

import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.events.EnemyKilledEvent;
import com.kelompok2.frontend.managers.GameEventManager;
import com.kelompok2.frontend.pools.EnemyPool;
import com.kelompok2.frontend.pools.ProjectilePool;

public class CollisionSystem {
    // Entity references
    private GameCharacter player;
    private EnemyPool enemyPool;
    private GameEventManager eventManager;

    // Handlers
    private PlayerCollisionHandler playerCollisionHandler;
    private ProjectileCollisionHandler projectileCollisionHandler;
    private SkillCollisionHandler skillCollisionHandler;

    // Friendly Fire Logic (specific to Insane enemies, kept here for orchestration)
    private java.util.Map<String, Float> friendlyFireCooldowns = new java.util.HashMap<>();
    private static final float FRIENDLY_FIRE_COOLDOWN = 0.5f;
    private float currentDelta = 0f;

    public CollisionSystem() {
        this.playerCollisionHandler = new PlayerCollisionHandler();
        this.projectileCollisionHandler = new ProjectileCollisionHandler();
        this.skillCollisionHandler = new SkillCollisionHandler();
    }

    public void initialize(GameCharacter player, EnemyPool enemyPool, ProjectilePool projectilePool,
            Array<MeleeAttack> playerMeleeAttacks, Array<MeleeAttack> bossMeleeAttacks,
            Array<Projectile> bossProjectiles, GameEventManager eventManager) {
        this.player = player;
        this.enemyPool = enemyPool;
        this.eventManager = eventManager;

        // Initialize handlers
        playerCollisionHandler.initialize(player, enemyPool, playerMeleeAttacks, bossMeleeAttacks, eventManager);
        projectileCollisionHandler.initialize(player, enemyPool, projectilePool, bossProjectiles, eventManager);
        skillCollisionHandler.initialize(player, enemyPool, eventManager);
    }

    public void checkAllCollisions(float delta, Boss currentBoss) {
        this.currentDelta = delta;

        // Update handlers with time delta
        playerCollisionHandler.update(delta);
        skillCollisionHandler.update(delta);

        // 1. Projectile Collisions
        projectileCollisionHandler.checkPlayerProjectilesVsEnemies();
        if (currentBoss != null && !currentBoss.isDead()) {
            projectileCollisionHandler.checkPlayerProjectilesVsBoss(currentBoss);
        }
        projectileCollisionHandler.checkBossProjectilesVsPlayer(currentBoss);
        // ✅ FIX: Check Ranged Enemy projectiles
        projectileCollisionHandler.checkEnemyProjectilesVsPlayer();

        // 2. Melee Attack Collisions
        playerCollisionHandler.checkPlayerMeleeVsEnemies();
        if (currentBoss != null && !currentBoss.isDead()) {
            playerCollisionHandler.checkPlayerMeleeVsBoss(currentBoss);
        }
        playerCollisionHandler.checkBossMeleeVsPlayer(currentBoss);
        playerCollisionHandler.checkPlayerVsEnemyContact();
        if (currentBoss != null && !currentBoss.isDead()) {
            playerCollisionHandler.checkPlayerVsBossContact(currentBoss);
        }

        // 3. Skill Collisions
        skillCollisionHandler.checkPlayerSkillsVsEnemies();
        if (currentBoss != null && !currentBoss.isDead()) {
            skillCollisionHandler.checkPlayerSkillsVsBoss(currentBoss);
            skillCollisionHandler.checkBossSkillsVsPlayer(currentBoss);
        }

        // 4. Insane Enemy Friendly Fire
        checkInsaneEnemyCollisions();
    }

    private void checkInsaneEnemyCollisions() {
        Array<com.kelompok2.frontend.entities.BaseEnemy> activeEnemies = enemyPool.getActiveEnemies();

        // Update friendly fire cooldowns
        java.util.Iterator<java.util.Map.Entry<String, Float>> it = friendlyFireCooldowns.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<String, Float> entry = it.next();
            entry.setValue(entry.getValue() - currentDelta);
            if (entry.getValue() <= 0) {
                it.remove();
            }
        }

        // Check each insane enemy against all other enemies
        for (int i = 0; i < activeEnemies.size; i++) {
            com.kelompok2.frontend.entities.BaseEnemy insaneEnemy = activeEnemies.get(i);

            if (!insaneEnemy.isInsane() || insaneEnemy.isDead()) {
                continue;
            }

            for (int j = 0; j < activeEnemies.size; j++) {
                if (i == j)
                    continue; // Skip self

                com.kelompok2.frontend.entities.BaseEnemy otherEnemy = activeEnemies.get(j);
                if (otherEnemy.isDead())
                    continue;

                if (insaneEnemy.getBounds().overlaps(otherEnemy.getBounds())) {
                    String pairKey = i + "-" + j;
                    Float cooldown = friendlyFireCooldowns.get(pairKey);

                    if (cooldown == null || cooldown <= 0) {
                        float damage = insaneEnemy.getAtk();
                        otherEnemy.takeDamage(damage);
                        friendlyFireCooldowns.put(pairKey, FRIENDLY_FIRE_COOLDOWN);
                        System.out.println("[Friendly Fire] Insane enemy hit another enemy for " + damage + " damage!");

                        if (otherEnemy.isDead()) {
                            handleEnemyKilled(otherEnemy);
                        }
                    }
                }
            }
        }
    }

    private void handleEnemyKilled(com.kelompok2.frontend.entities.BaseEnemy enemy) {
        float xpGain = enemy.getXpReward();
        player.gainXp(xpGain);
        eventManager.publish(new EnemyKilledEvent(enemy, player, xpGain));
        System.out.println("[Collision] Enemy killed! XP gained: " + xpGain);
    }
}
