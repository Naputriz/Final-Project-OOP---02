package com.kelompok2.frontend.systems;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.BaseEnemy;
import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.events.EnemyKilledEvent;
// import com.kelompok2.frontend.events.PlayerDamagedEvent; // Handled in GameCharacter
import com.kelompok2.frontend.managers.GameEventManager;
import com.kelompok2.frontend.pools.EnemyPool;

public class PlayerCollisionHandler {
    private GameCharacter player;
    private EnemyPool enemyPool;
    private Array<MeleeAttack> playerMeleeAttacks;
    private Array<MeleeAttack> bossMeleeAttacks;
    private GameEventManager eventManager;

    // Contact damage cooldown to prevent spam
    private float contactDamageCooldown = 0f;
    private static final float CONTACT_DAMAGE_INTERVAL = 0.5f;

    public void initialize(GameCharacter player, EnemyPool enemyPool,
            Array<MeleeAttack> playerMeleeAttacks, Array<MeleeAttack> bossMeleeAttacks,
            GameEventManager eventManager) {
        this.player = player;
        this.enemyPool = enemyPool;
        this.playerMeleeAttacks = playerMeleeAttacks;
        this.bossMeleeAttacks = bossMeleeAttacks;
        this.eventManager = eventManager;
    }

    public void update(float delta) {
        if (contactDamageCooldown > 0) {
            contactDamageCooldown -= delta;
        }
    }

    public void checkPlayerMeleeVsEnemies() {
        for (MeleeAttack attack : playerMeleeAttacks) {
            if (!attack.isActive())
                continue;

            for (BaseEnemy enemy : enemyPool.getActiveEnemies()) {
                if (enemy.isDead())
                    continue;

                if (attack.canHit(enemy)) {
                    // Insania deals 50% extra damage to insane enemies
                    float damage = attack.getDamage();
                    if (player instanceof com.kelompok2.frontend.entities.Insania && enemy.isInsane()) {
                        damage *= 1.5f;
                        System.out.println("[Insania] Bonus melee vs insane: " + damage);
                    }

                    // Lumi applies Mark
                    if (attack.appliesMark()) {
                        enemy.mark(10f); // Mark for 5 seconds
                        System.out.println("[Lumi] Enemy marked!");
                    }

                    // Apply Stun (Ground Slam, etc.)
                    if (attack.getStunDuration() > 0) {
                        enemy.stun(attack.getStunDuration());
                        System.out.println("[Collision] Enemy stunned for " + attack.getStunDuration() + "s");
                    }

                    enemy.takeDamage(damage);
                    attack.markAsHit(enemy);
                    eventManager.publish(
                            new com.kelompok2.frontend.events.EnemyDamagedEvent(enemy, damage, attack.isArts()));

                    if (enemy.isDead()) {
                        handleEnemyKilled(enemy);
                    }
                }
            }
        }
    }

    public void checkPlayerMeleeVsBoss(Boss boss) {
        for (MeleeAttack attack : playerMeleeAttacks) {
            if (!attack.isActive())
                continue;

            if (attack.canHit(boss)) {
                // Lumi applies Mark to bosses
                if (attack.appliesMark()) {
                    boss.mark(10f); // Mark for 5 seconds
                    System.out.println("[Lumi] Boss marked!");
                }

                // Apply Stun (Ground Slam, etc.)
                if (attack.getStunDuration() > 0) {
                    boss.stun(attack.getStunDuration());
                    System.out.println("[Collision] Boss stunned for " + attack.getStunDuration() + "s");
                }

                float damage = attack.getDamage();

                // Insania deals 50% extra damage to insane bosses
                if (player instanceof com.kelompok2.frontend.entities.Insania && boss.isInsane()) {
                    damage *= 1.5f;
                    System.out.println("[Insania] Bonus melee vs insane BOSS: " + damage);
                }

                boss.takeDamage(damage);
                attack.markAsHit(boss);
                eventManager
                        .publish(new com.kelompok2.frontend.events.EnemyDamagedEvent(boss, damage, attack.isArts()));
                System.out.println("[Collision] Boss hit by melee! HP: " + boss.getHp() + "/" + boss.getMaxHp());
            }
        }
    }

    public void checkBossMeleeVsPlayer(Boss boss) {
        for (MeleeAttack attack : bossMeleeAttacks) {
            if (!attack.isActive())
                continue;

            if (attack.canHit(player)) {
                float damage = attack.getDamage();
                player.takeDamage(damage, boss);
                attack.markAsHit(player);
                // eventManager.publish(new PlayerDamagedEvent(player, damage, player.getHp()));
                // // HANDLED IN GameCharacter
                System.out.println("[Collision] Player hit by boss melee! HP: " + player.getHp());
            }
        }
    }

    public void checkPlayerVsEnemyContact() {
        if (contactDamageCooldown > 0)
            return;

        Rectangle playerBounds = player.getBounds();

        for (BaseEnemy enemy : enemyPool.getActiveEnemies()) {
            if (enemy.isDead())
                continue;

            // Skip collision damage if enemy is being pulled (Lumi's Returnious Pull)
            if (enemy.isBeingPulled())
                continue;

            Rectangle enemyBounds = enemy.getBounds();

            if (playerBounds.overlaps(enemyBounds)) {
                float contactDamage = 5f;
                player.takeDamage(contactDamage, enemy);
                contactDamageCooldown = CONTACT_DAMAGE_INTERVAL;
                // eventManager.publish(new PlayerDamagedEvent(player, contactDamage,
                // player.getHp())); // HANDLED IN GameCharacter
                System.out.println("[Collision] Player touched enemy! HP: " + player.getHp());
                break;
            }
        }
    }

    public void checkPlayerVsBossContact(Boss boss) {
        if (contactDamageCooldown > 0)
            return;

        // Skip collision damage if boss is being pulled (Lumi's Returnious Pull)
        if (boss.isBeingPulled())
            return;

        Rectangle playerBounds = player.getBounds();
        Rectangle bossBounds = boss.getBounds();

        if (playerBounds.overlaps(bossBounds)) {
            float contactDamage = 10f;
            player.takeDamage(contactDamage, boss);
            contactDamageCooldown = CONTACT_DAMAGE_INTERVAL;
            // eventManager.publish(new PlayerDamagedEvent(player, contactDamage,
            // player.getHp())); // HANDLED IN GameCharacter
            System.out.println("[Collision] Player touched boss! HP: " + player.getHp());
        }
    }

    private void handleEnemyKilled(BaseEnemy enemy) {
        float xpGain = enemy.getXpReward();
        player.gainXp(xpGain);
        eventManager.publish(new EnemyKilledEvent(enemy, player, xpGain));
        System.out.println("[Collision] Enemy killed! XP gained: " + xpGain);
    }
}
