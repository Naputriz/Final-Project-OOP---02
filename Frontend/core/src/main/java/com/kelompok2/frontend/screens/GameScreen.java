package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.entities.Ryze;
import com.kelompok2.frontend.entities.Isolde;
import com.kelompok2.frontend.entities.Insania;
import com.kelompok2.frontend.entities.Blaze;
import com.kelompok2.frontend.entities.GlacialBreath;
import com.kelompok2.frontend.utils.InputHandler;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.GameManager;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.pools.ProjectilePool;
import com.kelompok2.frontend.pools.EnemyPool;
import com.kelompok2.frontend.factories.EnemyFactory;
import com.kelompok2.frontend.factories.EnemyType;

import java.util.Iterator;

public class GameScreen extends ScreenAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private GameCharacter player;
    private InputHandler inputHandler;
    private OrthographicCamera camera;
    private Texture background;

    private ProjectilePool projectilePool;
    private Array<MeleeAttack> meleeAttacks;
    private EnemyPool enemyPool;

    private float spawnTimer = 0;
    private Main game;
    private String selectedCharacter;

    private boolean isPaused = false;

    public GameScreen(Main game, String selectedCharacter) {
        this.game = game;
        this.selectedCharacter = selectedCharacter;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Inisialisasi Object Pools
        projectilePool = new ProjectilePool(50);
        meleeAttacks = new Array<>();

        // Setup Kamera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        // Load background
        background = AssetManager.getInstance().loadTexture("FireflyPlaceholder.jpg");

        // Spawn Player
        switch (selectedCharacter) {
            case "Ryze":
                player = new Ryze(0, 0);
                break;
            case "Isolde":
                player = new Isolde(0, 0);
                break;
            case "Insania":
                player = new Insania(0, 0);
                break;
            case "Blaze":
                player = new Blaze(0, 0);
                break;
            default:
                player = new Isolde(0, 0);
                this.selectedCharacter = "Isolde";
                break;
        }

        player.setPosition(0, 0);

        // Inisialisasi EnemyPool
        enemyPool = new EnemyPool(player, 30);

        // Inisialisasi GameManager
        GameManager.getInstance().startNewGame(this.selectedCharacter);

        // Setup Input Handler
        inputHandler = new InputHandler(player, camera, projectilePool, meleeAttacks);

        // Play BGM
        AudioManager.getInstance().playMusic("Audio/battleThemeA.mp3", true);
    }

    /**
     * Getter untuk karakter yang dipilih, digunakan oleh PauseScreen untuk Restart.
     */
    public String getSelectedCharacter() {
        return selectedCharacter;
    }

    @Override
    public void render(float delta) {
        // --- 1. LOGIC UPDATE SECTION ---
        // Jika delta == 0, berarti kita dipanggil oleh PauseScreen/LevelUpScreen sebagai background overlay.
        // Maka kita skip update logic agar game terlihat "Frozen".
        boolean renderingForOverlay = (delta == 0);

        if (!renderingForOverlay) {
            // Handle Pause Input (ESC)
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                // Pindah ke PauseScreen, kirim 'this' agar bisa dirender di background
                game.setScreen(new PauseScreen(game, this));
                return; // Stop processing current frame
            }

            // Cek level-up pending
            if (player.isLevelUpPending() && !isPaused) {
                pauseForLevelUp();
                return;
            }

            // Skip update jika internal pause aktif
            if (isPaused) {
                return;
            }

            // UPDATE LOGIC
            player.update(delta);
            inputHandler.update(delta);

            // Update pools
            projectilePool.update(delta);
            updateMeleeAttacks(delta);
            enemyPool.update(delta);

            checkCollisions(delta);
            spawnEnemies(delta);

            // Update GameManager state
            GameManager.getInstance().updateGameTime(delta);

            // Cek Game Over
            if (player.isDead()) {
                GameManager.getInstance().setGameOver(true);
                int finalLvl = GameManager.getInstance().getCurrentLevel();
                float finalTime = GameManager.getInstance().getGameTime();

                System.out.println("Game over! Switching to GameOverScreen...");
                ((Main) Gdx.app.getApplicationListener())
                    .setScreen(new GameOverScreen(game, selectedCharacter, finalLvl, finalTime));
                return;
            }

            // Update posisi kamera agar selalu mengikuti player
            camera.position.set(
                player.getPosition().x + player.getVisualWidth() / 2,
                player.getPosition().y + player.getVisualHeight() / 2,
                0);
            camera.update();
        }

        // --- 2. DRAWING SECTION (Selalu dijalankan) ---

        // Clear Screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Set projection matrix
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();

        // Draw Tiled Background
        float bgSize = 500;
        int startX = (int) (camera.position.x - camera.viewportWidth / 2) / (int) bgSize - 1;
        int startY = (int) (camera.position.y - camera.viewportHeight / 2) / (int) bgSize - 1;
        int endX = (int) (camera.position.x + camera.viewportWidth / 2) / (int) bgSize + 1;
        int endY = (int) (camera.position.y + camera.viewportHeight / 2) / (int) bgSize + 1;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                batch.draw(background, x * bgSize, y * bgSize, bgSize, bgSize);
            }
        }

        // Render Entities
        player.render(batch);
        projectilePool.render(batch);
        enemyPool.render(batch);

        batch.end();

        // Render Visual Effects & UI Overlay
        renderMeleeAttacks();
        renderGlacialBreaths();
        renderMindFracture();
        renderHellfirePillar();

        drawHealthBars();
    }

    private void drawHealthBars() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Render HP Player
        drawBar(player);
        // Render XP player
        drawXpBar(player);
        // Render Skill Cooldown player
        drawSkillCooldownBar(player);

        // Render HP Musuh
        for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
            drawBar(enemy);
        }

        shapeRenderer.end();
    }

    private void drawXpBar(GameCharacter character) {
        float x = character.getPosition().x;
        float y = character.getPosition().y + character.getVisualHeight() + 13;
        float width = character.getVisualWidth();
        float height = 4;

        float maxXp = character.getXpToNextLevel();
        float xpPercent = (maxXp > 0) ? character.getCurrentXp() / maxXp : 0;

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(x, y, width * xpPercent, height);
    }

    private void drawBar(GameCharacter character) {
        float x = character.getPosition().x;
        boolean isPlayer = (character instanceof Ryze || character instanceof Isolde || character instanceof Insania || character instanceof Blaze);
        float offset = isPlayer ? 25 : 5;
        float y = character.getPosition().y + character.getVisualHeight() + offset;

        float width = character.getVisualWidth();
        float height = 5;

        float hpPercent = character.getHp() / character.getMaxHp();

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(x, y, width * hpPercent, height);
    }

    private void drawSkillCooldownBar(GameCharacter character) {
        if (!(character instanceof Ryze || character instanceof Isolde || character instanceof Insania || character instanceof Blaze)) {
            return;
        }

        float x = character.getPosition().x;
        float y = character.getPosition().y + character.getVisualHeight() + 19;
        float width = character.getVisualWidth();
        float height = 4;

        float skillTimer = 0f;
        float skillCooldown = 1f;

        if (character instanceof Ryze) {
            Ryze ryze = (Ryze) character;
            skillTimer = ryze.getSkillTimer();
            skillCooldown = ryze.getSkillCooldown();
        } else if (character instanceof Isolde) {
            Isolde isolde = (Isolde) character;
            skillTimer = isolde.getSkillTimer();
            skillCooldown = isolde.getSkillCooldown();
        } else if (character instanceof Insania) {
            Insania insania = (Insania) character;
            skillTimer = insania.getSkillTimer();
            skillCooldown = insania.getSkillCooldown();
        } else if (character instanceof Blaze) {
            Blaze blaze = (Blaze) character;
            skillTimer = blaze.getSkillTimer();
            skillCooldown = blaze.getSkillCooldown();
        }

        float cooldownPercent = (skillCooldown > 0) ? (skillCooldown - skillTimer) / skillCooldown : 1f;

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.setColor(new Color(1f, 0.8f, 0f, 1f));
        shapeRenderer.rect(x, y, width * cooldownPercent, height);
    }

    private void updateMeleeAttacks(float delta) {
        Iterator<MeleeAttack> iter = meleeAttacks.iterator();
        while (iter.hasNext()) {
            MeleeAttack m = iter.next();
            m.update(delta);
            if (!m.isActive()) {
                iter.remove();
            }
        }
    }

    private void renderMeleeAttacks() {
        batch.begin();
        for (MeleeAttack m : meleeAttacks) {
            if (m.isActive()) {
                m.render(batch);
            }
        }
        batch.end();
    }

    private void renderGlacialBreaths() {
        if (!(player instanceof Isolde)) return;
        Isolde isolde = (Isolde) player;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (GlacialBreath gb : isolde.getGlacialBreaths()) {
            if (gb.isActive()) {
                gb.render(shapeRenderer);
            }
        }
        shapeRenderer.end();
    }

    private void renderMindFracture() {
        if (!(player instanceof Insania)) return;
        Insania insania = (Insania) player;
        if (insania.shouldShowMindFractureCircle()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
            float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
            Gdx.gl.glLineWidth(3);
            shapeRenderer.setColor(0.8f, 0.3f, 0.8f, 0.7f);
            shapeRenderer.circle(playerCenterX, playerCenterY, insania.getSkillRadius(), 50);
            Gdx.gl.glLineWidth(1);
            shapeRenderer.end();
        }
    }

    private void renderHellfirePillar() {
        if (!(player instanceof Blaze)) return;
        Blaze blaze = (Blaze) player;
        if (blaze.isPillarActive()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0.5f, 0f, 0.4f);
            shapeRenderer.circle(blaze.getPillarPosition().x, blaze.getPillarPosition().y, blaze.getPillarRadius(), 50);
            shapeRenderer.end();
        }
    }

    private void spawnEnemies(float delta) {
        spawnTimer += delta;
        if (spawnTimer > 1.5f) {
            float angle = MathUtils.random(360);
            float distance = MathUtils.random(600, 800);
            float x = player.getPosition().x + MathUtils.cosDeg(angle) * distance;
            float y = player.getPosition().y + MathUtils.sinDeg(angle) * distance;

            int currentLevel = GameManager.getInstance().getCurrentLevel();
            // Note: Pastikan class EnemyFactory Anda ada dan valid
            EnemyType type = EnemyFactory.getRandomEnemyType(currentLevel);

            // Spawn dari pool
            DummyEnemy enemy = enemyPool.obtain(x, y);
            spawnTimer = 0;
        }
    }

    private void checkCollisions(float delta) {
        Array<Projectile> activeProjectiles = projectilePool.getActiveProjectiles();
        Array<DummyEnemy> activeEnemies = enemyPool.getActiveEnemies();

        // Cek Peluru vs Musuh
        for (Projectile p : activeProjectiles) {
            Rectangle pBounds = p.getBounds();
            for (DummyEnemy e : activeEnemies) {
                if (pBounds.overlaps(e.getBounds())) {
                    p.active = false;
                    e.takeDamage(p.getDamage());
                    if (e.isDead()) {
                        player.gainXp(e.getXpReward());
                        System.out.println("Enemy Killed!");
                    }
                    break;
                }
            }
        }

        // Cek Melee Attacks vs Musuh
        for (MeleeAttack m : meleeAttacks) {
            if (!m.isActive()) continue;
            Rectangle mBounds = m.getBounds();
            for (DummyEnemy e : activeEnemies) {
                if (mBounds.overlaps(e.getBounds()) && m.canHit(e)) {
                    float damage = m.getDamage();
                    if (player instanceof Insania && e.isInsane()) {
                        damage *= 1.5f;
                    }
                    e.takeDamage(damage);
                    m.markAsHit(e);
                    if (e.isDead()) {
                        player.gainXp(e.getXpReward());
                    }
                }
            }
        }

        // Isolde Skill
        if (player instanceof Isolde) {
            Isolde isolde = (Isolde) player;
            for (GlacialBreath gb : isolde.getGlacialBreaths()) {
                if (!gb.isActive()) continue;
                for (DummyEnemy e : activeEnemies) {
                    if (gb.canHit(e)) {
                        e.takeDamage(gb.getDamage());
                        e.freeze();
                        gb.markAsHit(e);
                        if (e.isDead()) {
                            player.gainXp(e.getXpReward());
                        }
                    }
                }
            }
        }

        // Blaze Skill
        if (player instanceof Blaze) {
            Blaze blaze = (Blaze) player;
            if (blaze.isPillarActive()) {
                Vector2 pillarPos = blaze.getPillarPosition();
                float pillarRadius = blaze.getPillarRadius();
                float pillarDamage = blaze.getArts() * 2.0f;

                for (DummyEnemy e : activeEnemies) {
                    float enemyX = e.getBounds().x + e.getBounds().width / 2;
                    float enemyY = e.getBounds().y + e.getBounds().height / 2;
                    float distance = pillarPos.dst(enemyX, enemyY);
                    if (distance <= pillarRadius) {
                        e.takeDamage(pillarDamage * delta);
                        if (e.isDead()) {
                            player.gainXp(e.getXpReward());
                        }
                    }
                }
            }
        }

        // Insania Skill
        if (player instanceof Insania) {
            Insania insania = (Insania) player;
            if (insania.hasJustUsedMindFracture()) {
                float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
                float skillRadius = insania.getSkillRadius();
                float baseDamage = player.getArts() * 0.2f;

                for (DummyEnemy e : activeEnemies) {
                    float enemyCenterX = e.getPosition().x + e.getVisualWidth() / 2;
                    float enemyCenterY = e.getPosition().y + e.getVisualHeight() / 2;
                    float dx = enemyCenterX - playerCenterX;
                    float dy = enemyCenterY - playerCenterY;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                    if (distance <= skillRadius) {
                        e.applyInsanity();
                        e.takeDamage(baseDamage);
                        if (e.isDead()) {
                            player.gainXp(e.getXpReward());
                        }
                    }
                }
            }
        }

        // Cek Musuh vs Player
        Rectangle playerBounds = player.getBounds();
        for (DummyEnemy e : activeEnemies) {
            if (e.getBounds().overlaps(playerBounds)) {
                if (e.canAttack()) {
                    player.takeDamage(10);
                    e.resetAttackTimer();
                }
            }
        }

        // Friendly Fire (Insane enemies)
        for (int i = 0; i < activeEnemies.size; i++) {
            DummyEnemy insaneEnemy = activeEnemies.get(i);
            if (!insaneEnemy.isInsane()) continue;
            for (int j = 0; j < activeEnemies.size; j++) {
                if (i == j) continue;
                DummyEnemy targetEnemy = activeEnemies.get(j);
                if (insaneEnemy.getBounds().overlaps(targetEnemy.getBounds())) {
                    if (insaneEnemy.canAttack()) {
                        float damage = insaneEnemy.getAtk();
                        targetEnemy.takeDamage(damage);
                        insaneEnemy.resetAttackTimer();
                        if (targetEnemy.isDead()) {
                            player.gainXp(targetEnemy.getXpReward());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        player.dispose();
        projectilePool.dispose();
        enemyPool.dispose();
    }

    /**
     * Pause game dan tampilkan LevelUpScreen (Overlay).
     */
    public void pauseForLevelUp() {
        isPaused = true;
        // PENTING: Passing 'this' (GameScreen instance) ke LevelUpScreen
        // agar LevelUpScreen bisa merender background game yang beku.
        game.setScreen(new LevelUpScreen(game, this, player));
        System.out.println("[GameScreen] Game paused for level-up selection");
    }

    /**
     * Resume game setelah effect dipilih.
     */
    public void resumeFromLevelUp() {
        isPaused = false;
        System.out.println("[GameScreen] Game resumed from level-up");
    }
}
