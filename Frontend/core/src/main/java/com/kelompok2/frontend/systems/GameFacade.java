package com.kelompok2.frontend.systems;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.events.BossDefeatedEvent;
import com.kelompok2.frontend.events.BossSpawnedEvent;
import com.kelompok2.frontend.managers.GameEventManager;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.pools.EnemyPool;
import com.kelompok2.frontend.pools.ProjectilePool;
import com.kelompok2.frontend.skills.FrozenApocalypseSkill;
import com.kelompok2.frontend.skills.InfernoNovaSkill;
import com.kelompok2.frontend.skills.InsanityBurstSkill;
import com.kelompok2.frontend.skills.Skill;

public class GameFacade {
    // Event Manager (Observer Pattern)
    private GameEventManager eventManager;

    // All subsystems
    private RenderingSystem renderingSystem;
    private CollisionSystem collisionSystem;
    private SpawningSystem spawningSystem;
    private UISystem uiSystem;
    private BossCinematicSystem bossCinematicSystem;
    private MapBoundarySystem mapBoundarySystem;

    // Attack arrays (shared between systems)
    private Array<MeleeAttack> playerMeleeAttacks;
    private Array<MeleeAttack> bossMeleeAttacks;
    private Array<Projectile> bossProjectiles;

    // Entity references
    private GameCharacter player;
    private EnemyPool enemyPool;
    // ProjectilePool passed to subsystems, not stored here if unused

    // Boss cinematic temporary state
    private Vector2 pendingBossCinematicPosition = null;

    public GameFacade(SpriteBatch batch, ShapeRenderer shapeRenderer, Texture background) {
        // Initialize Event Manager (Singleton)
        eventManager = GameEventManager.getInstance();

        // Initialize attack arrays
        playerMeleeAttacks = new Array<>();
        bossMeleeAttacks = new Array<>();
        bossProjectiles = new Array<>();

        // Create subsystems
        renderingSystem = new RenderingSystem(batch, shapeRenderer, background);
        collisionSystem = new CollisionSystem();
        spawningSystem = new SpawningSystem();
        uiSystem = new UISystem(batch, shapeRenderer);
        bossCinematicSystem = new BossCinematicSystem();
        mapBoundarySystem = new MapBoundarySystem();

        System.out.println("[GameFacade] All subsystems created");
    }

    public void initialize(GameCharacter player, EnemyPool enemyPool, ProjectilePool projectilePool) {
        this.player = player;
        this.enemyPool = enemyPool;

        // Initialize each subsystem
        renderingSystem.initialize(player, enemyPool, projectilePool, playerMeleeAttacks, bossMeleeAttacks,
                bossProjectiles, eventManager);
        collisionSystem.initialize(player, enemyPool, projectilePool, playerMeleeAttacks, bossMeleeAttacks,
                bossProjectiles, eventManager);
        spawningSystem.initialize(player, enemyPool, eventManager, bossMeleeAttacks, bossProjectiles);
        uiSystem.initialize(player, enemyPool, eventManager);
        bossCinematicSystem.initialize(player, enemyPool);

        // Subscribe to boss events untuk handle boss spawning & defeat
        subscribeToEvents();

        System.out.println("[GameFacade] All subsystems initialized");
    }

    private void subscribeToEvents() {
        // Boss spawned -> start cinematic
        eventManager.subscribe(BossSpawnedEvent.class, this::onBossSpawned);

        // Boss defeated -> handle reward
        eventManager.subscribe(BossDefeatedEvent.class, this::onBossDefeated);
    }

    private void onBossSpawned(BossSpawnedEvent event) {
        System.out.println("[GameFacade] Handling boss spawn: " + event.getBossName());

        // ✅ FIX: Save position, camera will be passed in next update() call
        pendingBossCinematicPosition = event.getSpawnPosition().cpy();

        // Play boss music (delegated to BossCinematicSystem)
        bossCinematicSystem.playBossMusic(event.getBossName());
    }

    private void onBossDefeated(BossDefeatedEvent event) {
        System.out.println("[GameFacade] Handling boss defeat: " + event.getBossName());

        Skill ultimate = event.getUltimateSkill();

        // Set enemy array for ultimate skills
        if (ultimate instanceof InsanityBurstSkill) {
            ((InsanityBurstSkill) ultimate).setEnemies(enemyPool.getActiveEnemies());
        } else if (ultimate instanceof InfernoNovaSkill) {
            ((InfernoNovaSkill) ultimate).setEnemies(enemyPool.getActiveEnemies());
        } else if (ultimate instanceof FrozenApocalypseSkill) {
            ((FrozenApocalypseSkill) ultimate).setEnemies(enemyPool.getActiveEnemies());
        }

        // Give ultimate to player
        player.setUltimateSkill(ultimate);

        // Grant large XP reward
        float bossXP = 500f;
        player.gainXp(bossXP);
        System.out.println("[Player] Gained " + bossXP + " XP from boss kill!");

        // Clear boss from spawning system
        spawningSystem.clearCurrentBoss();

        // Resume normal BGM
        AudioManager.getInstance().stopMusic();
        AudioManager.getInstance().playMusic("Audio/battleThemeA.mp3", true);
    }

    public void update(float delta, OrthographicCamera camera) {
        Boss currentBoss = spawningSystem.getCurrentBoss();

        updateUltimateSkillReferences(currentBoss);

        if (pendingBossCinematicPosition != null) {
            bossCinematicSystem.startBossPanSequence(pendingBossCinematicPosition, camera);
            pendingBossCinematicPosition = null; // Clear pending
        }

        // Update boss cinematic system
        if (bossCinematicSystem.isCinematicActive()) {
            bossCinematicSystem.updateCameraPan(delta, camera);
        } else {
            mapBoundarySystem.update(player, camera);
        }

        // Update spawning (enemies and bosses)
        spawningSystem.update(delta);

        if (!bossCinematicSystem.isCinematicActive()) {
            // Update enemies AI
            for (com.kelompok2.frontend.entities.BaseEnemy enemy : enemyPool.getActiveEnemies()) {
                if (!enemy.isDead()) {
                    enemy.update(delta);
                }
            }

            // Update boss AI
            if (currentBoss != null && !currentBoss.isDead()) {
                currentBoss.update(delta);
            }
        } else {
            // ✅ FIX: During cinematic, still update boss animations (not AI)
            if (currentBoss != null && !currentBoss.isDead()) {
                currentBoss.updateAnimationsOnly(delta); // Keep boss animating during cinematic
            }
        }
        // Note: Boss frozen during cinematic except for animations

        // Update collisions
        collisionSystem.checkAllCollisions(delta, currentBoss);

        // Update UI timers
        uiSystem.update(delta);

        // Update melee attacks
        updateMeleeAttacks(delta);

        // Update boss projectiles
        for (int i = bossProjectiles.size - 1; i >= 0; i--) {
            Projectile proj = bossProjectiles.get(i);
            proj.update(delta);
            if (!proj.active) {
                bossProjectiles.removeIndex(i);
            }
        }

        // Check boss defeat
        if (currentBoss != null && currentBoss.isDead()) {
            handleBossDefeat(currentBoss);
        }
    }

    // Update ultimate skill references to current boss and enemies
    private void updateUltimateSkillReferences(Boss currentBoss) {
        if (player.hasUltimateSkill() || player.isUltimateUsed()) {
            Skill ultimate = player.getUltimateSkill();
            if (ultimate != null) {
                // Update boss reference
                if (ultimate instanceof InsanityBurstSkill) {
                    ((InsanityBurstSkill) ultimate).setBoss(currentBoss);
                    ((InsanityBurstSkill) ultimate).setEnemies(enemyPool.getActiveEnemies());
                } else if (ultimate instanceof InfernoNovaSkill) {
                    ((InfernoNovaSkill) ultimate).setBoss(currentBoss);
                    ((InfernoNovaSkill) ultimate).setEnemies(enemyPool.getActiveEnemies());
                } else if (ultimate instanceof FrozenApocalypseSkill) {
                    ((FrozenApocalypseSkill) ultimate).setBoss(currentBoss);
                    ((FrozenApocalypseSkill) ultimate).setEnemies(enemyPool.getActiveEnemies());
                }
            }
        }
    }

    public void render(OrthographicCamera camera) {
        Boss currentBoss = spawningSystem.getCurrentBoss();

        // Render game world
        renderingSystem.render(camera, currentBoss);

        // Render UI overlay (Hide during cinematic)
        if (!bossCinematicSystem.isCinematicActive()) {
            uiSystem.render(camera, currentBoss);
        }
    }

    private void updateMeleeAttacks(float delta) {
        // Update player attacks
        for (int i = playerMeleeAttacks.size - 1; i >= 0; i--) {
            MeleeAttack m = playerMeleeAttacks.get(i);
            m.update(delta);
            if (!m.isActive()) {
                playerMeleeAttacks.removeIndex(i);
            }
        }

        // Update boss attacks
        for (int i = bossMeleeAttacks.size - 1; i >= 0; i--) {
            MeleeAttack m = bossMeleeAttacks.get(i);
            m.update(delta);
            if (!m.isActive()) {
                bossMeleeAttacks.removeIndex(i);
            }
        }
    }

    private void handleBossDefeat(Boss boss) {
        String bossName = boss.getBossName();
        Skill ultimate = boss.createUltimateSkill();

        System.out.println("[GameFacade] Boss defeated: " + bossName);

        // Publish BossDefeatedEvent (Observer Pattern)
        eventManager.publish(new BossDefeatedEvent(boss, bossName, ultimate));
    }

    public void startBossCinematic(Vector2 bossPosition, OrthographicCamera camera) {
        bossCinematicSystem.startBossPanSequence(bossPosition, camera);
    }

    // Getters for subsystems
    public RenderingSystem getRenderingSystem() {
        return renderingSystem;
    }

    public CollisionSystem getCollisionSystem() {
        return collisionSystem;
    }

    public SpawningSystem getSpawningSystem() {
        return spawningSystem;
    }

    public UISystem getUISystem() {
        return uiSystem;
    }

    public BossCinematicSystem getBossCinematicSystem() {
        return bossCinematicSystem;
    }

    public GameEventManager getEventManager() {
        return eventManager;
    }

    public Array<MeleeAttack> getPlayerMeleeAttacks() {
        return playerMeleeAttacks;
    }

    public Array<MeleeAttack> getBossMeleeAttacks() {
        return bossMeleeAttacks;
    }

    public Array<Projectile> getBossProjectiles() {
        return bossProjectiles;
    }

    public void dispose() {
        uiSystem.dispose();
    }
}
