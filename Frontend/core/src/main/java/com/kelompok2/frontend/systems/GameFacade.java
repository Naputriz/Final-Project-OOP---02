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
import com.kelompok2.frontend.managers.GameManager;
import com.kelompok2.frontend.pools.EnemyPool;
import com.kelompok2.frontend.pools.ProjectilePool;
import com.kelompok2.frontend.skills.FrozenApocalypseSkill;
import com.kelompok2.frontend.skills.InfernoNovaSkill;
import com.kelompok2.frontend.skills.InsanityBurstSkill;
import com.kelompok2.frontend.skills.Skill;

public class GameFacade {
    private GameEventManager eventManager;

    private RenderingSystem renderingSystem;
    private CollisionSystem collisionSystem;
    private SpawningSystem spawningSystem;
    // [HAPUS] private UISystem uiSystem;
    private BossCinematicSystem bossCinematicSystem;
    private MapBoundarySystem mapBoundarySystem;
    private MinimapSystem minimapSystem;

    private Array<MeleeAttack> playerMeleeAttacks;
    private Array<MeleeAttack> bossMeleeAttacks;
    private Array<Projectile> bossProjectiles;

    private GameCharacter player;
    private EnemyPool enemyPool;
    private Vector2 pendingBossCinematicPosition = null;

    public GameFacade(SpriteBatch batch, ShapeRenderer shapeRenderer, Texture background) {
        eventManager = GameEventManager.getInstance();
        playerMeleeAttacks = new Array<>();
        bossMeleeAttacks = new Array<>();
        bossProjectiles = new Array<>();

        renderingSystem = new RenderingSystem(batch, shapeRenderer, background);
        collisionSystem = new CollisionSystem();
        spawningSystem = new SpawningSystem();
        // [HAPUS] uiSystem = new UISystem(batch, shapeRenderer);
        bossCinematicSystem = new BossCinematicSystem();
        mapBoundarySystem = new MapBoundarySystem();
        minimapSystem = new MinimapSystem(shapeRenderer);

        System.out.println("[GameFacade] All subsystems created");
    }

    public void initialize(GameCharacter player, EnemyPool enemyPool, ProjectilePool projectilePool) {
        this.player = player;
        this.enemyPool = enemyPool;

        renderingSystem.initialize(player, enemyPool, projectilePool, playerMeleeAttacks, bossMeleeAttacks, bossProjectiles, eventManager);
        collisionSystem.initialize(player, enemyPool, projectilePool, playerMeleeAttacks, bossMeleeAttacks, bossProjectiles, eventManager);
        spawningSystem.initialize(player, enemyPool, eventManager, bossMeleeAttacks, bossProjectiles);
        // [HAPUS] uiSystem.initialize(player, enemyPool, eventManager);
        bossCinematicSystem.initialize(player, enemyPool);
        minimapSystem.initialize(player, enemyPool, spawningSystem);

        subscribeToEvents();
        System.out.println("[GameFacade] All subsystems initialized");
    }

    private void subscribeToEvents() {
        eventManager.subscribe(BossSpawnedEvent.class, this::onBossSpawned);
        eventManager.subscribe(BossDefeatedEvent.class, this::onBossDefeated);
    }

    private void onBossSpawned(BossSpawnedEvent event) {
        pendingBossCinematicPosition = event.getSpawnPosition().cpy();
        bossCinematicSystem.playBossMusic(event.getBossName());
    }

    private void onBossDefeated(BossDefeatedEvent event) {
        Skill ultimate = event.getUltimateSkill();
        if (ultimate instanceof InsanityBurstSkill) ((InsanityBurstSkill) ultimate).setEnemies(enemyPool.getActiveEnemies());
        else if (ultimate instanceof InfernoNovaSkill) ((InfernoNovaSkill) ultimate).setEnemies(enemyPool.getActiveEnemies());
        else if (ultimate instanceof FrozenApocalypseSkill) ((FrozenApocalypseSkill) ultimate).setEnemies(enemyPool.getActiveEnemies());

        player.setUltimateSkill(ultimate);
        player.gainXp(500f);
        spawningSystem.clearCurrentBoss();
        AudioManager.getInstance().stopMusic();
        AudioManager.getInstance().playMusic("Audio/battleThemeA.mp3", true);

        String characterToUnlock = mapBossToCharacter(event.getBossName());
        if (characterToUnlock != null) GameManager.getInstance().unlockCharacter(characterToUnlock);
    }

    public void update(float delta, OrthographicCamera camera) {
        Boss currentBoss = spawningSystem.getCurrentBoss();
        updateUltimateSkillReferences(currentBoss);

        if (pendingBossCinematicPosition != null) {
            bossCinematicSystem.startBossPanSequence(pendingBossCinematicPosition, camera);
            pendingBossCinematicPosition = null;
        }

        if (bossCinematicSystem.isCinematicActive()) {
            bossCinematicSystem.updateCameraPan(delta, camera);
        } else {
            mapBoundarySystem.update(player, camera);
        }

        spawningSystem.update(delta);

        if (!bossCinematicSystem.isCinematicActive()) {
            for (com.kelompok2.frontend.entities.BaseEnemy enemy : enemyPool.getActiveEnemies()) {
                if (!enemy.isDead()) enemy.update(delta);
            }
            if (currentBoss != null && !currentBoss.isDead()) currentBoss.update(delta);
        } else {
            if (currentBoss != null && !currentBoss.isDead()) currentBoss.updateAnimationsOnly(delta);
        }

        collisionSystem.checkAllCollisions(delta, currentBoss);
        // [HAPUS] uiSystem.update(delta);
        updateMeleeAttacks(delta);

        for (int i = bossProjectiles.size - 1; i >= 0; i--) {
            Projectile proj = bossProjectiles.get(i);
            proj.update(delta);
            if (!proj.active) bossProjectiles.removeIndex(i);
        }

        if (currentBoss != null && currentBoss.isDead()) handleBossDefeat(currentBoss);
    }

    private void updateUltimateSkillReferences(Boss currentBoss) {
        if ((player.hasUltimateSkill() || player.isUltimateUsed()) && player.getUltimateSkill() != null) {
            Skill ult = player.getUltimateSkill();
            if (ult instanceof InsanityBurstSkill) ((InsanityBurstSkill) ult).setBoss(currentBoss);
            else if (ult instanceof InfernoNovaSkill) ((InfernoNovaSkill) ult).setBoss(currentBoss);
            else if (ult instanceof FrozenApocalypseSkill) ((FrozenApocalypseSkill) ult).setBoss(currentBoss);
        }
    }

    public void render(OrthographicCamera camera) {
        Boss currentBoss = spawningSystem.getCurrentBoss();
        renderingSystem.render(camera, currentBoss);

        // Render UI overlay (Hide during cinematic)
        if (!bossCinematicSystem.isCinematicActive()) {
            //uiSystem.render(camera, currentBoss);
            minimapSystem.render(camera);
        }
    }

    private void updateMeleeAttacks(float delta) {
        for (int i = playerMeleeAttacks.size - 1; i >= 0; i--) {
            MeleeAttack m = playerMeleeAttacks.get(i);
            m.update(delta);
            if (!m.isActive()) playerMeleeAttacks.removeIndex(i);
        }
        for (int i = bossMeleeAttacks.size - 1; i >= 0; i--) {
            MeleeAttack m = bossMeleeAttacks.get(i);
            m.update(delta);
            if (!m.isActive()) bossMeleeAttacks.removeIndex(i);
        }
    }

    private void handleBossDefeat(Boss boss) {
        eventManager.publish(new BossDefeatedEvent(boss, boss.getBossName(), boss.createUltimateSkill()));
    }

    public void startBossCinematic(Vector2 bossPosition, OrthographicCamera camera) {
        bossCinematicSystem.startBossPanSequence(bossPosition, camera);
    }

    private String mapBossToCharacter(String bossName) {
        // nama boss
        if (bossName.contains("Isolde"))
            return "Isolde";
        if (bossName.contains("Insania"))
            return "Insania";
        if (bossName.contains("Blaze"))
            return "Blaze";
        // boss lain belum ditambahkan
        return null;
    }

    // Getters
    public RenderingSystem getRenderingSystem() { return renderingSystem; }
    public CollisionSystem getCollisionSystem() { return collisionSystem; }
    public SpawningSystem getSpawningSystem() { return spawningSystem; }
    // [HAPUS] public UISystem getUISystem()
    public BossCinematicSystem getBossCinematicSystem() { return bossCinematicSystem; } // Penting untuk GameScreen
    public GameEventManager getEventManager() { return eventManager; }
    public Array<MeleeAttack> getPlayerMeleeAttacks() { return playerMeleeAttacks; }
    public Array<MeleeAttack> getBossMeleeAttacks() { return bossMeleeAttacks; }
    public Array<Projectile> getBossProjectiles() { return bossProjectiles; }

    public void dispose() {
        // [HAPUS] uiSystem.dispose();
    }
}
