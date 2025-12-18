package com.kelompok2.frontend.systems;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.events.EnemyKilledEvent;
import com.kelompok2.frontend.managers.GameEventManager;
import com.kelompok2.frontend.events.PlayerDamagedEvent;
import com.kelompok2.frontend.pools.EnemyPool;
import com.kelompok2.frontend.pools.ProjectilePool;

public class CollisionSystem {
    // Entity references
    private GameCharacter player;
    private EnemyPool enemyPool;
    private ProjectilePool projectilePool;
    private Array<MeleeAttack> playerMeleeAttacks;
    private Array<MeleeAttack> bossMeleeAttacks;
    private Array<Projectile> bossProjectiles;

    // Event Manager
    private GameEventManager eventManager;

    public CollisionSystem() {
        // Empty constructor
    }

    public void initialize(GameCharacter player, EnemyPool enemyPool, ProjectilePool projectilePool,
            Array<MeleeAttack> playerMeleeAttacks, Array<MeleeAttack> bossMeleeAttacks,
            Array<Projectile> bossProjectiles, GameEventManager eventManager) {
        this.player = player;
        this.enemyPool = enemyPool;
        this.projectilePool = projectilePool;
        this.playerMeleeAttacks = playerMeleeAttacks;
        this.bossMeleeAttacks = bossMeleeAttacks;
        this.bossProjectiles = bossProjectiles;
        this.eventManager = eventManager;
    }

    public void checkAllCollisions(float delta, Boss currentBoss) {
        // Store delta for use in damage calculations
        this.currentDelta = delta;

        // Tick down contact damage cooldown
        if (contactDamageCooldown > 0) {
            contactDamageCooldown -= delta;
        }

        // Player attacks vs enemies
        checkPlayerProjectilesVsEnemies();
        checkPlayerMeleeVsEnemies();

        // ✅ FIX: Player skills vs enemies
        checkPlayerSkillsVsEnemies();

        // Player attacks vs boss
        if (currentBoss != null && !currentBoss.isDead()) {
            checkPlayerProjectilesVsBoss(currentBoss);
            checkPlayerMeleeVsBoss(currentBoss);

            // ✅ FIX: Player skills vs boss
            checkPlayerSkillsVsBoss(currentBoss);
        }

        // Enemy/Boss attacks vs player
        checkBossProjectilesVsPlayer(currentBoss);
        checkBossMeleeVsPlayer(currentBoss);

        // ✅ FIX: Boss skills vs player
        if (currentBoss != null && !currentBoss.isDead()) {
            checkBossSkillsVsPlayer(currentBoss);
        }

        // Contact damage
        checkPlayerVsEnemyContact();
        if (currentBoss != null && !currentBoss.isDead()) {
            checkPlayerVsBossContact(currentBoss);
        }

        // ✅ FIX: Insane enemies have friendly fire
        checkInsaneEnemyCollisions();
    }

    // ========== PLAYER ATTACKS VS ENEMIES ==========

    /**
     * Check player projectiles hitting enemies
     */
    private void checkPlayerProjectilesVsEnemies() {
        for (Projectile projectile : projectilePool.getActiveProjectiles()) {
            if (!projectile.active)
                continue;

            Rectangle projBounds = projectile.getBounds();

            for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
                if (enemy.isDead())
                    continue;

                Rectangle enemyBounds = enemy.getBounds();

                if (projBounds.overlaps(enemyBounds)) {
                    // ✅ FIX: Insania deals 50% extra damage to insane enemies
                    float damage = projectile.getDamage();
                    if (player instanceof com.kelompok2.frontend.entities.Insania && enemy.isInsane()) {
                        damage *= 1.5f;
                        System.out.println("[Insania] Bonus damage vs insane: " + damage);
                    }
                    enemy.takeDamage(damage);
                    projectile.active = false;

                    // Check if enemy died
                    if (enemy.isDead()) {
                        handleEnemyKilled(enemy);
                    }
                    break;
                }
            }
        }
    }

    private void checkPlayerMeleeVsEnemies() {
        for (MeleeAttack attack : playerMeleeAttacks) {
            if (!attack.isActive())
                continue;

            for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
                if (enemy.isDead())
                    continue;

                if (attack.canHit(enemy)) {
                    // ✅ FIX: Insania deals 50% extra damage to insane enemies
                    float damage = attack.getDamage();
                    if (player instanceof com.kelompok2.frontend.entities.Insania && enemy.isInsane()) {
                        damage *= 1.5f;
                        System.out.println("[Insania] Bonus melee vs insane: " + damage);
                    }
                    enemy.takeDamage(damage);
                    attack.markAsHit(enemy);

                    if (enemy.isDead()) {
                        handleEnemyKilled(enemy);
                    }
                }
            }
        }
    }

    private void checkPlayerProjectilesVsBoss(Boss boss) {
        for (Projectile projectile : projectilePool.getActiveProjectiles()) {
            if (!projectile.active)
                continue;

            Rectangle projBounds = projectile.getBounds();

            Rectangle bossBounds = boss.getBounds();

            if (projBounds.overlaps(bossBounds)) {
                boss.takeDamage(projectile.getDamage());
                projectile.active = false;
                System.out.println("[Collision] Boss hit! HP: " + boss.getHp() + "/" + boss.getMaxHp());
                break;
            }
        }
    }

    private void checkPlayerMeleeVsBoss(Boss boss) {
        for (MeleeAttack attack : playerMeleeAttacks) {
            if (!attack.isActive())
                continue;

            if (attack.canHit(boss)) {
                boss.takeDamage(attack.getDamage());
                attack.markAsHit(boss);
                System.out.println("[Collision] Boss hit by melee! HP: " + boss.getHp() + "/" + boss.getMaxHp());
            }
        }
    }

    private void checkBossProjectilesVsPlayer(Boss boss) {
        for (Projectile projectile : bossProjectiles) {
            if (!projectile.active)
                continue;

            Rectangle projBounds = projectile.getBounds();

            Rectangle playerBounds = player.getBounds();

            if (projBounds.overlaps(playerBounds)) {
                float damage = projectile.getDamage();
                player.takeDamage(damage, boss);
                projectile.active = false;
                eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                System.out.println("[Collision] Player hit by projectile! HP: " + player.getHp());
                break;
            }
        }
    }

    private void checkBossMeleeVsPlayer(Boss boss) {
        for (MeleeAttack attack : bossMeleeAttacks) {
            if (!attack.isActive())
                continue;

            if (attack.canHit(player)) {
                float damage = attack.getDamage();
                player.takeDamage(damage, boss);
                attack.markAsHit(player);
                eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                System.out.println("[Collision] Player hit by boss melee! HP: " + player.getHp());
            }
        }
    }

    // Contact damage cooldown to prevent spam
    private float contactDamageCooldown = 0f;
    private static final float CONTACT_DAMAGE_INTERVAL = 0.5f; // 0.5 seconds between contact damage

    // Store delta for damage calculations
    private float currentDelta = 0f;

    private java.util.Map<String, Float> friendlyFireCooldowns = new java.util.HashMap<>();
    private static final float FRIENDLY_FIRE_COOLDOWN = 0.5f; // Same as contact damage
    private long lastMindFractureHitOnPlayer = -1;
    private java.util.Set<Integer> bosseMeleeHitsOnPlayer = new java.util.HashSet<>();

    private void checkPlayerVsEnemyContact() {
        // Skip if on cooldown
        if (contactDamageCooldown > 0) {
            return;
        }

        Rectangle playerBounds = player.getBounds();

        for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
            if (enemy.isDead())
                continue;

            Rectangle enemyBounds = enemy.getBounds();

            if (playerBounds.overlaps(enemyBounds)) {
                // Enemy deals contact damage
                float contactDamage = 5f; // Small contact damage
                player.takeDamage(contactDamage, enemy);
                contactDamageCooldown = CONTACT_DAMAGE_INTERVAL; // Set cooldown
                System.out.println("[Collision] Player touched enemy! HP: " + player.getHp());

                // Push player back slightly to avoid getting stuck
                break; // Only one contact damage per frame
            }
        }
    }

    private void checkPlayerVsBossContact(Boss boss) {
        // Skip if on cooldown (same cooldown as enemy contact)
        if (contactDamageCooldown > 0) {
            return;
        }

        Rectangle playerBounds = player.getBounds();
        Rectangle bossBounds = boss.getBounds();

        if (playerBounds.overlaps(bossBounds)) {
            float contactDamage = 10f; // Boss contact damage is higher
            player.takeDamage(contactDamage, boss);
            contactDamageCooldown = CONTACT_DAMAGE_INTERVAL; // Set cooldown
            System.out.println("[Collision] Player touched boss! HP: " + player.getHp());
        }
    }

    private void checkPlayerSkillsVsEnemies() {
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
                        enemy.freeze(); // Apply freeze effect
                        gb.markAsHit(enemy);

                        if (enemy.isDead()) {
                            handleEnemyKilled(enemy);
                        }
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
                        // ✅ FIX: Apply DOT damage (Blaze Arts * 1.25 per second - balanced)
                        float damagePerSecond = blaze.getArts() * 1.25f; // Reduced from 2.0 to 1.25 for balance
                        float damage = damagePerSecond * currentDelta;
                        enemy.takeDamage(damage);

                        if (enemy.isDead()) {
                            handleEnemyKilled(enemy);
                        }
                    }
                }
            }
        }

        // Whisperwind: Hurricane Bind projectiles
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
                        enemy.stun(3.0f); // ✅ FIX: Stun for 3 seconds (doesn't break on hit)
                        hurricane.active = false; // Deactivate hurricane
                        System.out.println("[Collision] Enemy hit by Hurricane Bind! Damage: " + hurricane.getDamage()
                                + " - Stunned!");

                        if (enemy.isDead()) {
                            handleEnemyKilled(enemy);
                        }
                        break; // Hurricane hits one enemy then disappears
                    }
                }
            }
        }

        // Insania: Mind Fracture
        if (player instanceof com.kelompok2.frontend.entities.Insania) {
            com.kelompok2.frontend.entities.Insania insania = (com.kelompok2.frontend.entities.Insania) player;
            if (insania.shouldShowMindFractureCircle()) {
                long activationId = insania.getMindFractureActivationId();
                float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
                float radius = insania.getSkillRadius();

                for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
                    if (enemy.isDead())
                        continue;

                    // ✅ FIX: Skip if already hit by this activation
                    if (enemy.wasHitByMindFracture(activationId)) {
                        continue;
                    }

                    float enemyCenterX = enemy.getPosition().x + enemy.getVisualWidth() / 2;
                    float enemyCenterY = enemy.getPosition().y + enemy.getVisualHeight() / 2;
                    float distance = (float) Math.sqrt(
                            Math.pow(enemyCenterX - playerCenterX, 2) +
                                    Math.pow(enemyCenterY - playerCenterY, 2));

                    if (distance <= radius) {
                        // ✅ FIX: Mind Fracture deals damage + applies insanity
                        float damage = player.getArts() * 0.75f; // Arts × 0.75 damage
                        enemy.makeInsane(5.0f);
                        enemy.takeDamage(damage);
                        enemy.markMindFractureHit(activationId); // ✅ Mark as hit

                        if (enemy.isDead()) {
                            handleEnemyKilled(enemy);
                        }
                        System.out.println("[Collision] Enemy hit by Mind Fracture! Damage: " + damage + " - Insane!");
                    }
                }
            }
        }
    }

    private void checkPlayerSkillsVsBoss(Boss boss) {
        // Isolde: Glacial Breath vs Boss
        if (player instanceof com.kelompok2.frontend.entities.Isolde) {
            com.kelompok2.frontend.entities.Isolde isolde = (com.kelompok2.frontend.entities.Isolde) player;
            for (com.kelompok2.frontend.entities.GlacialBreath gb : isolde.getGlacialBreaths()) {
                if (!gb.isActive())
                    continue;

                if (gb.canHit(boss)) {
                    boss.takeDamage(gb.getDamage());
                    boss.freeze(); // ✅ FIX: Boss now supports freeze
                    gb.markAsHit(boss);
                    System.out.println("[Collision] Boss hit by Glacial Breath! HP: " + boss.getHp() + " - Frozen!");
                }
            }
        }

        // Blaze: Hellfire Pillar vs Boss
        if (player instanceof com.kelompok2.frontend.entities.Blaze) {
            com.kelompok2.frontend.entities.Blaze blaze = (com.kelompok2.frontend.entities.Blaze) player;
            if (blaze.isPillarActive()) {
                com.badlogic.gdx.math.Rectangle pillarBounds = new com.badlogic.gdx.math.Rectangle(
                        blaze.getPillarPosition().x - blaze.getPillarRadius(),
                        blaze.getPillarPosition().y - blaze.getPillarRadius(),
                        blaze.getPillarRadius() * 2,
                        blaze.getPillarRadius() * 2);

                if (pillarBounds.overlaps(boss.getBounds())) {
                    // Damage handled by skill itself
                }
            }
        }

        // Blaze: Hellfire Pillar vs Boss
        if (player instanceof com.kelompok2.frontend.entities.Blaze) {
            com.kelompok2.frontend.entities.Blaze blaze = (com.kelompok2.frontend.entities.Blaze) player;
            if (blaze.isPillarActive()) {
                com.badlogic.gdx.math.Rectangle pillarBounds = new com.badlogic.gdx.math.Rectangle(
                        blaze.getPillarPosition().x - blaze.getPillarRadius(),
                        blaze.getPillarPosition().y - blaze.getPillarRadius(),
                        blaze.getPillarRadius() * 2,
                        blaze.getPillarRadius() * 2);

                if (pillarBounds.overlaps(boss.getBounds())) {
                    // Apply DOT damage to boss
                    float damagePerSecond = blaze.getArts() * 1.25f; // Reduced from 2.0 for balance
                    float damage = damagePerSecond * currentDelta;
                    boss.takeDamage(damage);
                    System.out.println(
                            "[Collision] Boss hit by Hellfire Pillar! Damage: " + damage + ", HP: " + boss.getHp());
                }
            }
        }

        // Whisperwind: Hurricane Bind vs Boss
        if (player instanceof com.kelompok2.frontend.entities.Whisperwind) {
            com.kelompok2.frontend.entities.Whisperwind whisperwind = (com.kelompok2.frontend.entities.Whisperwind) player;
            for (Projectile hurricane : whisperwind.getHurricaneProjectiles()) {
                if (!hurricane.active)
                    continue;

                if (hurricane.getBounds().overlaps(boss.getBounds())) {
                    boss.takeDamage(hurricane.getDamage());
                    boss.stun(3.0f); // Stun boss for 3 seconds (doesn't break on hit)
                    hurricane.active = false;
                    System.out.println("[Collision] Boss hit by Hurricane Bind! Damage: " + hurricane.getDamage()
                            + ", HP: " + boss.getHp() + " - Stunned!");
                    break;
                }
            }
        }

        if (player instanceof com.kelompok2.frontend.entities.Insania) {
            com.kelompok2.frontend.entities.Insania insania = (com.kelompok2.frontend.entities.Insania) player;
            if (insania.shouldShowMindFractureCircle()) {
                long activationId = insania.getMindFractureActivationId();

                // ✅ FIX: Skip if boss already hit by this activation
                if (!boss.wasHitByMindFracture(activationId)) {
                    float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                    float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
                    float radius = insania.getSkillRadius();

                    float bossCenterX = boss.getPosition().x + boss.getVisualWidth() / 2;
                    float bossCenterY = boss.getPosition().y + boss.getVisualHeight() / 2;
                    float distance = (float) Math.sqrt(
                            Math.pow(bossCenterX - playerCenterX, 2) +
                                    Math.pow(bossCenterY - playerCenterY, 2));

                    if (distance <= radius) {
                        // ✅ FIX: Mind Fracture deals damage + applies insanity to boss
                        float damage = player.getArts() * 0.75f; // Arts × 0.75 damage
                        boss.applyInsanity();
                        boss.takeDamage(damage);
                        boss.markMindFractureHit(activationId); // ✅ Mark as hit
                        System.out.println("[Collision] Boss hit by Mind Fracture! Damage: " + damage + " - Insane!");
                    }
                }
            }
        }
    }

    private void checkBossSkillsVsPlayer(Boss boss) {
        // BossBlaze: Hellfire Pillar
        if (boss instanceof com.kelompok2.frontend.entities.BossBlaze) {
            com.kelompok2.frontend.entities.BossBlaze bossBlaze = (com.kelompok2.frontend.entities.BossBlaze) boss;
            if (bossBlaze.isPillarActive()) {
                com.badlogic.gdx.math.Rectangle pillarBounds = new com.badlogic.gdx.math.Rectangle(
                        bossBlaze.getPillarPosition().x - bossBlaze.getPillarRadius(),
                        bossBlaze.getPillarPosition().y - bossBlaze.getPillarRadius(),
                        bossBlaze.getPillarRadius() * 2,
                        bossBlaze.getPillarRadius() * 2);

                if (pillarBounds.overlaps(player.getBounds())) {
                    // ✅ FIX: Apply DOT damage to player
                    float damagePerSecond = bossBlaze.getArts() * 1.25f; // Same as enemy version
                    float damage = damagePerSecond * currentDelta;
                    player.takeDamage(damage, boss);
                    eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                    // Don't log every tick - too spammy
                }
            }
        }

        // BossIsolde: Glacial Breath
        if (boss instanceof com.kelompok2.frontend.entities.BossIsolde) {
            com.kelompok2.frontend.entities.BossIsolde bossIsolde = (com.kelompok2.frontend.entities.BossIsolde) boss;
            for (com.kelompok2.frontend.entities.GlacialBreath gb : bossIsolde.getGlacialBreaths()) {
                if (!gb.isActive())
                    continue;

                if (gb.canHit(player)) {
                    float damage = gb.getDamage();
                    player.takeDamage(damage, boss);
                    player.freeze(3.0f); // ✅ FIX: Freeze player for 3 seconds
                    gb.markAsHit(player);
                    eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                    System.out.println(
                            "[Collision] Player hit by boss Glacial Breath! HP: " + player.getHp() + " - Frozen!");
                }
            }
        }

        // BossInsania: Mind Fracture
        if (boss instanceof com.kelompok2.frontend.entities.BossInsania) {
            com.kelompok2.frontend.entities.BossInsania bossInsania = (com.kelompok2.frontend.entities.BossInsania) boss;
            if (bossInsania.shouldShowMindFractureCircle()) {
                long activationId = bossInsania.getMindFractureActivationId();

                // ✅ FIX: Only hit player once per activation
                if (lastMindFractureHitOnPlayer != activationId) {
                    float bossCenterX = boss.getPosition().x + boss.getVisualWidth() / 2;
                    float bossCenterY = boss.getPosition().y + boss.getVisualHeight() / 2;
                    float radius = bossInsania.getSkillRadius();

                    float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                    float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
                    float distance = (float) Math.sqrt(
                            Math.pow(playerCenterX - bossCenterX, 2) +
                                    Math.pow(playerCenterY - bossCenterY, 2));

                    if (distance <= radius) {
                        // Apply damage and insanity to player (once per activation)
                        float damage = boss.getArts() * 0.75f;
                        player.takeDamage(damage, boss);
                        player.makeInsane(5.0f);
                        eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                        lastMindFractureHitOnPlayer = activationId; // Mark as hit
                        System.out.println("[Collision] Player hit by Mind Fracture! Damage: " + damage + " - Insane!");
                    }
                }
            }
        }
    }

    private void checkInsaneEnemyCollisions() {
        Array<DummyEnemy> activeEnemies = enemyPool.getActiveEnemies();

        // Update friendly fire cooldowns
        java.util.Iterator<java.util.Map.Entry<String, Float>> it = friendlyFireCooldowns.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<String, Float> entry = it.next();
            entry.setValue(entry.getValue() - currentDelta);
            if (entry.getValue() <= 0) {
                it.remove(); // Remove expired cooldowns
            }
        }

        // Check each insane enemy against all other enemies
        for (int i = 0; i < activeEnemies.size; i++) {
            DummyEnemy insaneEnemy = activeEnemies.get(i);

            // Skip if not insane or dead
            if (!insaneEnemy.isInsane() || insaneEnemy.isDead()) {
                continue;
            }

            // Check collision with other enemies
            for (int j = 0; j < activeEnemies.size; j++) {
                if (i == j)
                    continue; // Skip self

                DummyEnemy otherEnemy = activeEnemies.get(j);
                if (otherEnemy.isDead())
                    continue;

                // Check if they're overlapping
                if (insaneEnemy.getBounds().overlaps(otherEnemy.getBounds())) {
                    // ✅ FIX: Check cooldown before applying damage
                    String pairKey = i + "-" + j; // Unique key for this pair
                    Float cooldown = friendlyFireCooldowns.get(pairKey);

                    if (cooldown == null || cooldown <= 0) {
                        // Apply damage once per cooldown
                        float damage = insaneEnemy.getAtk(); // Full ATK damage per hit
                        otherEnemy.takeDamage(damage);
                        friendlyFireCooldowns.put(pairKey, FRIENDLY_FIRE_COOLDOWN);
                        System.out.println("[Friendly Fire] Insane enemy hit another enemy for " + damage + " damage!");

                        if (otherEnemy.isDead()) {
                            handleEnemyKilled(otherEnemy);
                            System.out.println("[Friendly Fire] Insane enemy killed another enemy!");
                        }
                    }
                }
            }
        }
    }

    private void handleEnemyKilled(DummyEnemy enemy) {
        // Grant XP to player (use enemy's XP reward)
        float xpGain = enemy.getXpReward(); // DummyEnemy has 25 XP
        player.gainXp(xpGain);

        // Publish event
        eventManager.publish(new EnemyKilledEvent(enemy, player, xpGain));

        System.out.println("[Collision] Enemy killed! XP gained: " + xpGain);
    }
}
