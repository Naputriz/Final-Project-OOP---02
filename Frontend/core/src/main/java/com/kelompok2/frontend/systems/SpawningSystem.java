package com.kelompok2.frontend.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.entities.BossBlaze;
import com.kelompok2.frontend.entities.BossInsania;
import com.kelompok2.frontend.entities.BossIsolde;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.events.BossSpawnedEvent;
import com.kelompok2.frontend.managers.GameEventManager;
import com.kelompok2.frontend.factories.EnemyFactory;
import com.kelompok2.frontend.factories.EnemyType;
import com.kelompok2.frontend.managers.GameManager;
import com.kelompok2.frontend.pools.EnemyPool;
import com.badlogic.gdx.utils.Array;

public class SpawningSystem {
    // Enemy spawning
    private float spawnTimer = 0;
    private EnemyPool enemyPool;
    private GameCharacter player;

    // Boss spawning
    private float bossSpawnInterval = 90f; // 5 seconds for testing (change to 300f for 5 min production)
    private float bossSpawnTimer = 0f;
    private Boss currentBoss = null;

    // Random boss selection (no repeats until all defeated)
    private Array<String> availableBosses = new Array<>();

    // Event Manager để publish events
    private GameEventManager eventManager;

    // Boss attack arrays (injected from GameFacade)
    private Array<com.kelompok2.frontend.entities.MeleeAttack> bossMeleeAttacks;
    private Array<com.kelompok2.frontend.entities.Projectile> bossProjectiles;

    public SpawningSystem() {
        // Initialize available bosses for random spawning
        availableBosses.addAll("Insania", "Blaze", "Isolde");
    }

    public void initialize(GameCharacter player, EnemyPool enemyPool, GameEventManager eventManager,
            Array<com.kelompok2.frontend.entities.MeleeAttack> bossMeleeAttacks,
            Array<com.kelompok2.frontend.entities.Projectile> bossProjectiles) {
        this.player = player;
        this.enemyPool = enemyPool;
        this.eventManager = eventManager;
        this.bossMeleeAttacks = bossMeleeAttacks;
        this.bossProjectiles = bossProjectiles;
    }

    public void update(float delta) {
        spawnEnemies(delta);
        updateBossSpawning(delta);
    }

    private void spawnEnemies(float delta) {
        // Don't spawn regular enemies during boss fights
        if (currentBoss != null && !currentBoss.isDead()) {
            return;
        }

        // Calculate spawn interval based on player level
        // Base: 1.5s, decreases by 0.05s per level, min 0.1s
        float baseInterval = 1.5f;
        float reductionPerLevel = 0.05f;
        float minInterval = 0.1f;

        float currentInterval = Math.max(minInterval, baseInterval - (player.getLevel() * reductionPerLevel));

        spawnTimer += delta;
        if (spawnTimer > currentInterval) {
            float angle = MathUtils.random(360);
            float distance = MathUtils.random(600, 800);
            // Calculate potential position
            // ✨ Fix: Constrain X and Y to map boundaries
            float mapW = MapBoundarySystem.getMapWidth();
            float mapH = MapBoundarySystem.getMapHeight();
            float padding = 64f;

            float x = player.getPosition().x + MathUtils.cosDeg(angle) * distance;
            float y = player.getPosition().y + MathUtils.sinDeg(angle) * distance;

            // Clamp coordinate
            x = MathUtils.clamp(x, padding, mapW - padding);
            y = MathUtils.clamp(y, padding, mapH - padding);

            int currentLevel = GameManager.getInstance().getCurrentLevel();
            EnemyType type = EnemyFactory.getRandomEnemyType(currentLevel);

            // Spawn dari pool
            enemyPool.obtain(type, x, y);
            spawnTimer = 0;
        }
    }

    private void updateBossSpawning(float delta) {
        // Don't spawn if boss already active
        if (currentBoss != null && !currentBoss.isDead()) {
            return;
        }

        bossSpawnTimer += delta;

        // Check if it's time to spawn a boss
        if (bossSpawnTimer >= bossSpawnInterval) {
            spawnBoss();
            bossSpawnTimer = 0f;
        }
    }

    private void spawnBoss() {
        // Replenish boss pool if empty
        if (availableBosses.size == 0) {
            availableBosses.addAll("Insania", "Blaze", "Isolde");
            System.out.println("[Boss] All bosses defeated! Replenishing pool...");
        }

        // Random selection
        int randomIndex = MathUtils.random(0, availableBosses.size - 1);
        String bossName = availableBosses.get(randomIndex);
        availableBosses.removeIndex(randomIndex);

        // Spawn position - far from player
        float angle = MathUtils.random(360);
        float distance = 1000f; // Farther than normal enemies

        // ✨ Fix: Constrain X and Y to map boundaries
        float mapW = MapBoundarySystem.getMapWidth();
        float mapH = MapBoundarySystem.getMapHeight();
        float padding = 200f; // Larger padding for boss

        float x = player.getPosition().x + MathUtils.cosDeg(angle) * distance;
        float y = player.getPosition().y + MathUtils.sinDeg(angle) * distance;

        // Clamp coordinate
        x = MathUtils.clamp(x, padding, mapW - padding);
        y = MathUtils.clamp(y, padding, mapH - padding);

        // Get player level for scaling
        int playerLevel = player.getLevel();

        // Create boss instance
        switch (bossName) {
            case "Insania":
                currentBoss = new BossInsania(x, y, player, playerLevel);
                break;
            case "Blaze":
                currentBoss = new BossBlaze(x, y, player, playerLevel);
                break;
            case "Isolde":
                currentBoss = new BossIsolde(x, y, player, playerLevel);
                break;
        }

        currentBoss.setAttackArrays(bossMeleeAttacks, bossProjectiles);
        System.out.println("[SpawningSystem] Injected attack arrays into boss");

        System.out.println("[Boss] " + bossName + " has spawned! Level: " + playerLevel);
        System.out.println("[Boss] Position: x=" + x + ", y=" + y);

        // Calculate boss center position for camera pan
        float bossCenterX = x + currentBoss.getVisualWidth() / 2;
        float bossCenterY = y + currentBoss.getVisualHeight() / 2;
        Vector2 bossPosition = new Vector2(bossCenterX, bossCenterY);

        // Publish BossSpawnedEvent (Observer Pattern)
        eventManager.publish(new BossSpawnedEvent(currentBoss, bossName, bossPosition));

        System.out.println("[SpawningSystem] Published BossSpawnedEvent for: " + bossName);
    }

    public Boss getCurrentBoss() {
        return currentBoss;
    }

    public void clearCurrentBoss() {
        currentBoss = null;
    }

    public void setBossSpawnInterval(float interval) {
        this.bossSpawnInterval = interval;
    }
}
