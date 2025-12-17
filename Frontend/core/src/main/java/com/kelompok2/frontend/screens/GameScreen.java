package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
import com.kelompok2.frontend.entities.Whisperwind;
import com.kelompok2.frontend.entities.GlacialBreath;
// Boss imports
import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.entities.BossInsania;
import com.kelompok2.frontend.entities.BossBlaze;
import com.kelompok2.frontend.entities.BossIsolde;
// Ultimate skill imports
import com.kelompok2.frontend.skills.Skill;
import com.kelompok2.frontend.skills.InsanityBurstSkill;
import com.kelompok2.frontend.skills.InfernoNovaSkill;
import com.kelompok2.frontend.skills.FrozenApocalypseSkill;
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
    private boolean isDisposed = false; // Guard against double disposal

    // Boss spawning system
    private float bossSpawnInterval = 5f; // 1 minute for testing (change to 300f for 5 min production)
    private float bossSpawnTimer = 0f;
    private Boss currentBoss = null;

    // Boss attacks (separate from player attacks)
    private Array<Projectile> bossProjectiles = new Array<>();
    private Array<MeleeAttack> bossMeleeAttacks = new Array<>();

    // Random boss selection (no repeats until all defeated)
    private Array<String> availableBosses = new Array<>();

    // Camera panning for boss spawn
    private boolean cameraPanningToBoss = false;
    private float cameraPanTimer = 0f;
    private static final float CAMERA_PAN_TO_BOSS_DURATION = 1.0f; // Pan to boss
    private static final float CAMERA_HOLD_DURATION = 3.0f; // Hold on boss
    private static final float CAMERA_PAN_BACK_DURATION = 1.0f; // Pan back to player
    private static final float CAMERA_PAN_DURATION = CAMERA_PAN_TO_BOSS_DURATION + CAMERA_HOLD_DURATION
            + CAMERA_PAN_BACK_DURATION; // Total: 5 seconds
    private Vector2 originalCameraPos = new Vector2();
    private Vector2 targetCameraPos = new Vector2();
    private float originalCameraZoom = 1f;
    private static final float BOSS_ZOOM_LEVEL = 0.5f; // Zoom in MORE (smaller = closer)

    // Ultimate activation message
    private String ultimateMessage = "";
    private float ultimateMessageTimer = 0f;

    // Game timer
    private float gameTimer = 0f;
    private static final float ULTIMATE_MESSAGE_DURATION = 3.0f;

    // Boss UI
    private BitmapFont bossFont;

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

        // Spawn Player - based on character selection
        // TODO: Bikin lebih scalable biar ga perlu tambah setiap karakter
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
            case "Whisperwind":
                player = new Whisperwind(0, 0);
                break;
            case "Aelita":
                player = new com.kelompok2.frontend.entities.Aelita(0, 0);
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

        // Initialize boss system
        bossFont = new BitmapFont();
        bossFont.getData().setScale(2.0f);
        bossFont.setColor(Color.WHITE);

        // Initialize available bosses for random spawning
        availableBosses.addAll("Insania", "Blaze", "Isolde");

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
        // Jika delta == 0, berarti kita dipanggil oleh PauseScreen/LevelUpScreen
        // sebagai background overlay.
        // Maka kita skip update logic agar game terlihat "Frozen".
        boolean renderingForOverlay = (delta == 0);

        if (!renderingForOverlay) {
            // Handle Pause Input (ESC)
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                // Pindah ke PauseScreen, kirim 'this' agar bisa dirender di background
                game.setScreen(new PauseScreen(game, this));
                return; // Stop processing current frame
            }

            // Ultimate skill (R key)
            if (Gdx.input.isKeyJustPressed(Input.Keys.R) && player.hasUltimateSkill()) {
                System.out.println("[Player] Attempting to use ultimate skill...");

                // Get world coordinates of mouse
                com.badlogic.gdx.math.Vector3 mousePos3D = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(),
                        Gdx.input.getY(), 0);
                camera.unproject(mousePos3D);
                Vector2 mousePos = new Vector2(mousePos3D.x, mousePos3D.y);

                // Log info
                Skill ultimate = player.getUltimateSkill();
                System.out.println("[Player] Using ultimate: " + ultimate.getName());

                // Set enemies array for ultimate skills before activation
                if (ultimate instanceof com.kelompok2.frontend.skills.FrozenApocalypseSkill) {
                    ((com.kelompok2.frontend.skills.FrozenApocalypseSkill) ultimate)
                            .setEnemies(enemyPool.getActiveEnemies());
                    ((com.kelompok2.frontend.skills.FrozenApocalypseSkill) ultimate).setBoss(currentBoss);
                } else if (ultimate instanceof com.kelompok2.frontend.skills.InsanityBurstSkill) {
                    ((com.kelompok2.frontend.skills.InsanityBurstSkill) ultimate)
                            .setEnemies(enemyPool.getActiveEnemies());
                    ((com.kelompok2.frontend.skills.InsanityBurstSkill) ultimate).setBoss(currentBoss);
                } else if (ultimate instanceof com.kelompok2.frontend.skills.InfernoNovaSkill) {
                    ((com.kelompok2.frontend.skills.InfernoNovaSkill) ultimate)
                            .setEnemies(enemyPool.getActiveEnemies());
                    ((com.kelompok2.frontend.skills.InfernoNovaSkill) ultimate).setBoss(currentBoss);
                }

                // Execute ultimate skill
                player.performUltimateSkill(mousePos, projectilePool.getActiveProjectiles(), meleeAttacks);

                // Show activation message
                if (ultimate != null) {
                    ultimateMessage = "*** " + ultimate.getName().toUpperCase() + " ACTIVATED! ***";
                    ultimateMessageTimer = ULTIMATE_MESSAGE_DURATION;
                }
            }

            // Cek level-up pending
            if (player.isLevelUpPending() && !isPaused) {
                pauseForLevelUp();
                return;
            }

            // Always update camera panning (even if game is paused)
            if (cameraPanningToBoss) {
                updateCameraPan(delta);
            }

            // Skip update jika internal pause aktif
            if (isPaused) {
                return;
            }

            // UPDATE LOGIC
            // Skip player/boss updates during camera pan
            if (!cameraPanningToBoss) {
                player.update(delta);
            }

            // Skip input handling during camera pan
            if (!cameraPanningToBoss) {
                inputHandler.update(delta);
            }

            // Update pools - skip during camera pan for true "frozen time" effect
            if (!cameraPanningToBoss) {
                projectilePool.update(delta);
                updateMeleeAttacks(delta);
            }

            // Skip enemy updates during camera pan to prevent enemies from surrounding
            // player
            if (!cameraPanningToBoss) {
                enemyPool.update(delta);
            }

            // Update ultimate message timer
            if (ultimateMessageTimer > 0) {
                ultimateMessageTimer -= delta;
                if (ultimateMessageTimer < 0) {
                    ultimateMessage = "";
                }
            }

            // Update boss - animations only during camera pan, full update otherwise
            if (currentBoss != null && !currentBoss.isDead()) {
                if (cameraPanningToBoss) {
                    // During cinematic: only update animations
                    currentBoss.updateAnimationsOnly(delta);
                } else {
                    // Normal gameplay: full update including AI
                    currentBoss.update(delta);

                    // Collect boss attacks into GameScreen arrays for rendering/collision
                    // Don't clear - let attacks persist for their duration
                    if (currentBoss instanceof BossInsania) {
                        // Sync boss attacks with GameScreen array (add new ones)
                        for (MeleeAttack attack : ((BossInsania) currentBoss).getMeleeAttacks()) {
                            if (!bossMeleeAttacks.contains(attack, true)) {
                                bossMeleeAttacks.add(attack);
                            }
                        }
                    } else if (currentBoss instanceof BossBlaze) {
                        // Sync boss attacks with GameScreen array (add new ones)
                        for (MeleeAttack attack : ((BossBlaze) currentBoss).getMeleeAttacks()) {
                            if (!bossMeleeAttacks.contains(attack, true)) {
                                bossMeleeAttacks.add(attack);
                            }
                        }
                    } else if (currentBoss instanceof BossIsolde) {
                        bossProjectiles.clear();
                        bossProjectiles.addAll(((BossIsolde) currentBoss).getProjectiles());
                    }
                }
            }

            checkCollisions(delta);
            spawnEnemies(delta);
            updateBossSpawning(delta); // Boss spawning system

            // Increment game timer
            gameTimer += delta;

            // Update GameManager state
            GameManager.getInstance().updateGameTime(delta);

            // Cek Game Over - but skip if screen is disposed
            if (!isDisposed && player.isDead()) {
                GameManager.getInstance().setGameOver(true);
                int finalLvl = GameManager.getInstance().getCurrentLevel();
                float finalTime = GameManager.getInstance().getGameTime();

                System.out.println("Game over! Switching to GameOverScreen...");
                ((Main) Gdx.app.getApplicationListener())
                        .setScreen(new GameOverScreen(game, selectedCharacter, finalLvl, finalTime));
                return;
            }

            // Update posisi kamera agar selalu mengikuti player (skip during camera pan)
            if (!cameraPanningToBoss) {
                camera.position.set(
                        player.getPosition().x + player.getVisualWidth() / 2,
                        player.getPosition().y + player.getVisualHeight() / 2,
                        0);
            }
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

        batch.end(); // End batch before ShapeRenderer

        // Render Verdant Domain with ShapeRenderer (underneath sprites)
        renderVerdantDomain();

        batch.begin(); // Restart batch for sprites

        // Render Entities (will draw on top of Verdant Domain)
        player.render(batch);
        projectilePool.render(batch);
        enemyPool.render(batch);

        // Render boss if active
        if (currentBoss != null && !currentBoss.isDead()) {
            currentBoss.render(batch);
        }

        batch.end();

        // Render Visual Effects & UI Overlay
        renderMeleeAttacks();
        renderGlacialBreaths();

        // Render Hurricane Bind projectiles (Whisperwind)
        renderHurricaneBinds();

        // Render Mind Fracture circle (untuk visual feedback)
        renderMindFracture();
        renderHellfirePillar();

        // Render Secondary Skills visuals
        renderBladeFury();
        renderGroundSlam();
        renderIceShield();

        // Render Boss Skills visuals
        renderBossSkills();

        drawHealthBars();
    }

    private void drawHealthBars() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Render HP Player
        drawBar(player);
        // Render XP player
        drawXpBar(player);

        // Render E Skill Cooldown player
        drawSkillCooldownBar(player);

        // Render Q Secondary Skill Cooldown player
        drawSecondarySkillBar(player);

        // Render Boss Health Bar if active (at top of screen)
        if (currentBoss != null && !currentBoss.isDead()) {
            drawBossHealthBar(currentBoss);
        }

        // Always render HP bars for regular enemies
        for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
            drawBar(enemy);
        }

        shapeRenderer.end();

        // Render visual indicators for boss skills
        if (currentBoss != null && !currentBoss.isDead()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1f, 0f, 0f, 0.5f); // Semi-transparent red

            if (currentBoss instanceof BossInsania) {
                BossInsania insania = (BossInsania) currentBoss;
                if (insania.shouldShowMindFractureCircle()) {
                    float centerX = currentBoss.getPosition().x + currentBoss.getVisualWidth() / 2;
                    float centerY = currentBoss.getPosition().y + currentBoss.getVisualHeight() / 2;
                    shapeRenderer.circle(centerX, centerY, insania.getSkillRadius());
                }
            } else if (currentBoss instanceof BossBlaze) {
                BossBlaze blaze = (BossBlaze) currentBoss;
                if (blaze.isPillarActive()) {
                    Vector2 pillarPos = blaze.getPillarPosition();
                    shapeRenderer.setColor(1f, 0.5f, 0f, 0.7f); // Orange
                    shapeRenderer.circle(pillarPos.x, pillarPos.y, blaze.getPillarRadius());
                }
            }

            shapeRenderer.end();
        }

        // Render boss title text if boss is active (above the boss)
        if (currentBoss != null && !currentBoss.isDead()) {
            batch.begin();

            // Position above boss
            float bossX = currentBoss.getPosition().x;
            float bossY = currentBoss.getPosition().y + currentBoss.getVisualHeight();
            float bossWidth = currentBoss.getVisualWidth() * 2f;

            // Get boss name and title
            String bossName = currentBoss.getBossName();
            String bossTitle = currentBoss.getBossTitle();
            String fullTitle = bossName + " - " + bossTitle; // e.g., "Insania - The Chaos Kaiser"

            // Calculate text position (centered above boss)
            GlyphLayout titleLayout = new GlyphLayout(bossFont, fullTitle);
            float titleX = bossX + (bossWidth - titleLayout.width) / 2;
            float titleY = bossY + 50f; // Above health bar

            // Draw title with shadow for visibility
            bossFont.setColor(0f, 0f, 0f, 0.7f); // Shadow
            bossFont.draw(batch, fullTitle, titleX + 2, titleY - 2);
            bossFont.setColor(1f, 0.84f, 0f, 1f); // Gold
            bossFont.draw(batch, fullTitle, titleX, titleY);
            bossFont.setColor(Color.WHITE); // Reset

            batch.end();
        }

        // Render ultimate activation message
        if (ultimateMessageTimer > 0 && !ultimateMessage.isEmpty()) {
            batch.begin();
            bossFont.setColor(Color.GOLD);
            // Center the message on screen
            float messageX = (camera.viewportWidth - ultimateMessage.length() * 15f) / 2f;
            float messageY = camera.viewportHeight - 150f; // Near top of screen
            bossFont.draw(batch, ultimateMessage, messageX, messageY);
            batch.end();
        }

        // Render game timer at top center
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        int minutes = (int) (gameTimer / 60);
        int seconds = (int) (gameTimer % 60);
        String timerText = String.format("%02d:%02d", minutes, seconds);
        GlyphLayout timerLayout = new GlyphLayout(bossFont, timerText);
        float timerX = camera.position.x - (timerLayout.width / 2);
        float timerY = camera.position.y + (camera.viewportHeight / 2) - 30;
        bossFont.setColor(0f, 0f, 0f, 0.8f);
        bossFont.draw(batch, timerText, timerX + 2, timerY - 2);
        bossFont.setColor(Color.WHITE);
        bossFont.draw(batch, timerText, timerX, timerY);
        batch.end();
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

        // --- PERBAIKAN POSISI ---
        // HP bar at top (furthest from character)
        // Use Template Method pattern - check if has innate skill
        boolean isPlayer = (character.getInnateSkillCooldown() > 0);
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
        // Use Template Method pattern - check if character has innate skill
        // Characters with no innate skill return 0 for cooldown
        if (character.getInnateSkillCooldown() == 0) {
            return;
        }

        float x = character.getPosition().x;
        float y = character.getPosition().y + character.getVisualHeight() + 19;
        float width = character.getVisualWidth();
        float height = 4;

        float skillTimer = 0f;
        float skillCooldown = 1f; // Default to avoid division by zero

        // Use Template Method pattern - polymorphic call instead of instanceof
        skillTimer = character.getInnateSkillTimer();
        skillCooldown = character.getInnateSkillCooldown();

        // Safety check to avoid division by zero
        if (skillCooldown == 0) {
            skillCooldown = 1f;
        }

        float cooldownPercent = (skillCooldown > 0) ? (skillCooldown - skillTimer) / skillCooldown : 1f;

        // Background (Dark gray)
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.setColor(new Color(1f, 0.8f, 0f, 1f));
        shapeRenderer.rect(x, y, width * cooldownPercent, height);
    }

    private void drawSecondarySkillBar(GameCharacter character) {
        // Only draw if character has secondary skill
        if (!character.hasSecondarySkill()) {
            return;
        }

        float x = character.getPosition().x;
        // Q Skill Bar below E Skill, above XP bar
        // HP at +25 (height 5)
        // E Skill at +19 (height 4)
        // Q Skill at +16 (height 3)
        // XP at +13 (height 4)
        float y = character.getPosition().y + character.getVisualHeight() + 16;

        float width = character.getVisualWidth();
        float height = 3; // Slightly smaller untuk distinguish

        // Get Q skill cooldown data
        float skillTimer = character.getSecondarySkill().getRemainingCooldown();
        float skillCooldown = character.getSecondarySkill().getCooldown();

        // Calculate percentage remaining (skill is ready when timer = 0)
        float cooldownPercent = (skillCooldown > 0) ? (skillCooldown - skillTimer) / skillCooldown : 1f;

        // Background (Dark gray)
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, width, height);

        // Foreground (Blue for Q skill cooldown - different from E skill's orange)
        shapeRenderer.setColor(new Color(0.2f, 0.6f, 1f, 1f)); // Light blue
        shapeRenderer.rect(x, y, width * cooldownPercent, height);
    }

    // Hapus mellee attack yang gak aktif / di luar durasi
    private void updateMeleeAttacks(float delta) {
        // Update player attacks
        Iterator<MeleeAttack> iter = meleeAttacks.iterator();
        while (iter.hasNext()) {
            MeleeAttack m = iter.next();
            m.update(delta);
            if (!m.isActive()) {
                iter.remove();
            }
        }

        // Update boss attacks
        Iterator<MeleeAttack> bossIter = bossMeleeAttacks.iterator();
        while (bossIter.hasNext()) {
            MeleeAttack m = bossIter.next();
            m.update(delta);
            if (!m.isActive()) {
                bossIter.remove();
            }
        }
    }

    private void renderMeleeAttacks() {
        batch.begin();

        // Render Player Attacks
        for (MeleeAttack attack : meleeAttacks) {
            if (attack.isActive()) {
                attack.render(batch);
            }
        }

        // Render Boss Attacks
        for (MeleeAttack attack : bossMeleeAttacks) {
            if (attack.isActive()) {
                attack.render(batch);
            }
        }

        // Render projectiles (must be inside batch block)
        for (Projectile projectile : projectilePool.getActiveProjectiles()) {
            projectile.render(batch);
        }

        for (Projectile proj : bossProjectiles) {
            proj.render(batch);
        }

        batch.end();
    }

    /**
     * Render Verdant Domain zone if active (called separately with ShapeRenderer)
     */
    private void renderVerdantDomain() {
        if (!(player instanceof com.kelompok2.frontend.entities.Aelita)) {
            return;
        }

        com.kelompok2.frontend.entities.Aelita aelita = (com.kelompok2.frontend.entities.Aelita) player;
        com.kelompok2.frontend.skills.VerdantDomainSkill vd = aelita.getVerdantDomain();

        if (!vd.isZoneActive()) {
            return;
        }

        // Render green healing zone
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Vector2 zonePos = vd.getZonePosition();
        float radius = vd.getZoneRadius();

        // Semi-transparent green for healing zone (low alpha so player stays visible)
        shapeRenderer.setColor(0.2f, 0.8f, 0.3f, 0.15f);
        shapeRenderer.circle(zonePos.x, zonePos.y, radius, 50);

        shapeRenderer.end();
    }

    private void renderGlacialBreaths() {
        if (!(player instanceof Isolde))
            return;
        Isolde isolde = (Isolde) player;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (GlacialBreath gb : isolde.getGlacialBreaths()) {
            if (gb.isActive()) {
                gb.render(shapeRenderer);
            }
        }
        shapeRenderer.end();
    }

    // Render Hurricane Bind projectiles untuk Whisperwind
    private void renderHurricaneBinds() {
        if (!(player instanceof Whisperwind))
            return;

        Whisperwind whisperwind = (Whisperwind) player;
        batch.begin();
        for (Projectile hurricaneProjectile : whisperwind.getHurricaneProjectiles()) {
            if (hurricaneProjectile.active) {
                hurricaneProjectile.render(batch);
            }
        }
        batch.end();
    }

    // Render Mind Fracture visual circle untuk Insania
    private void renderMindFracture() {
        if (!(player instanceof Insania))
            return;
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
        if (!(player instanceof Blaze))
            return;
        Blaze blaze = (Blaze) player;
        if (blaze.isPillarActive()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0.5f, 0f, 0.4f);
            shapeRenderer.circle(blaze.getPillarPosition().x, blaze.getPillarPosition().y, blaze.getPillarRadius(), 50);
            shapeRenderer.end();
        }
    }

    // Render Blade Fury skill visual (spinning attack)
    private void renderBladeFury() {
        if (!player.hasSecondarySkill())
            return;

        if (player.getSecondarySkill() instanceof com.kelompok2.frontend.skills.BladeFurySkill) {
            com.kelompok2.frontend.skills.BladeFurySkill bladeFury = (com.kelompok2.frontend.skills.BladeFurySkill) player
                    .getSecondarySkill();

            if (bladeFury.isSkillActive()) {
                // Determine which animation to use based on character type
                String animationType = getCharacterAttackType();

                // Load animation textures from AssetManager
                String spritePath = animationType.equals("scratch") ? "AttackAnimations/Scratch Animation.png"
                        : "AttackAnimations/Slash Animation.png";

                Texture attackTexture = com.kelompok2.frontend.managers.AssetManager.getInstance()
                        .loadTexture(spritePath);
                com.badlogic.gdx.graphics.g2d.TextureRegion attackSprite = new com.badlogic.gdx.graphics.g2d.TextureRegion(
                        attackTexture);

                float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;

                // Render multiple attack sprites in a circular pattern
                batch.begin();
                int numberOfSprites = 8; // 8 attack sprites around character
                float spriteSize = 48f; // Size of each sprite
                float orbitRadius = bladeFury.getRadius() * 0.7f; // Slightly inside the damage radius

                for (int i = 0; i < numberOfSprites; i++) {
                    float angle = (360f / numberOfSprites) * i;
                    float angleRad = (float) Math.toRadians(angle);

                    // Position each sprite around the circle
                    float spriteX = playerCenterX + (float) Math.cos(angleRad) * orbitRadius - spriteSize / 2;
                    float spriteY = playerCenterY + (float) Math.sin(angleRad) * orbitRadius - spriteSize / 2;

                    // Draw with rotation pointing outward from center
                    batch.draw(attackSprite,
                            spriteX, spriteY, // Position
                            spriteSize / 2, spriteSize / 2, // Origin (center)
                            spriteSize, spriteSize, // Size
                            1f, 1f, // Scale
                            angle); // Rotation to point outward
                }
                batch.end();
            }
        }
    }

    private String getCharacterAttackType() {
        return player.getAttackAnimationType();
    }

    // Render Ground Slam skill visual (shockwave)
    private void renderGroundSlam() {
        if (!player.hasSecondarySkill())
            return;

        if (player.getSecondarySkill() instanceof com.kelompok2.frontend.skills.GroundSlamSkill) {
            com.kelompok2.frontend.skills.GroundSlamSkill groundSlam = (com.kelompok2.frontend.skills.GroundSlamSkill) player
                    .getSecondarySkill();

            if (groundSlam.isShockwaveActive()) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

                // Draw expanding shockwave circle
                Vector2 slamPos = groundSlam.getShockwavePosition();
                shapeRenderer.setColor(0.8f, 0.6f, 0.3f, 0.3f); // Brown/orange semi-transparent
                shapeRenderer.circle(slamPos.x, slamPos.y, groundSlam.getRadius(), 50);

                shapeRenderer.end();
            }
        }
    }

    // Render Ice Shield skill visual (protective circle)
    private void renderIceShield() {
        if (!player.hasSecondarySkill())
            return;

        if (player.getSecondarySkill() instanceof com.kelompok2.frontend.skills.IceShieldSkill) {
            com.kelompok2.frontend.skills.IceShieldSkill iceShield = (com.kelompok2.frontend.skills.IceShieldSkill) player
                    .getSecondarySkill();

            if (iceShield.isShieldActive()) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

                // Draw blue shield circle around player
                float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
                float shieldRadius = player.getVisualWidth() / 2 + 10; // Slightly larger than character

                Gdx.gl.glLineWidth(2);
                shapeRenderer.setColor(0.3f, 0.7f, 1f, 0.7f); // Light blue
                shapeRenderer.circle(playerCenterX, playerCenterY, shieldRadius, 50);
                Gdx.gl.glLineWidth(1);

                shapeRenderer.end();
            }
        }
    }

    // Render Boss Skills visuals
    private void renderBossSkills() {
        if (currentBoss == null || currentBoss.isDead())
            return;

        // Render Mind Fracture for BossInsania
        if (currentBoss instanceof BossInsania) {
            BossInsania bossInsania = (BossInsania) currentBoss;
            if (bossInsania.shouldShowMindFractureCircle()) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                float bossCenterX = currentBoss.getPosition().x + currentBoss.getVisualWidth() / 2;
                float bossCenterY = currentBoss.getPosition().y + currentBoss.getVisualHeight() / 2;
                Gdx.gl.glLineWidth(3);
                shapeRenderer.setColor(0.8f, 0.3f, 0.8f, 0.7f);
                shapeRenderer.circle(bossCenterX, bossCenterY, bossInsania.getSkillRadius(), 50);
                Gdx.gl.glLineWidth(1);
                shapeRenderer.end();
            }
        }

        // Render Hellfire Pillar for BossBlaze
        if (currentBoss instanceof BossBlaze) {
            BossBlaze bossBlaze = (BossBlaze) currentBoss;
            if (bossBlaze.isPillarActive()) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(1f, 0.5f, 0f, 0.4f);
                shapeRenderer.circle(bossBlaze.getPillarPosition().x, bossBlaze.getPillarPosition().y,
                        bossBlaze.getPillarRadius(), 50);
                shapeRenderer.end();
            }
        }

        // Render Glacial Breath for BossIsolde
        if (currentBoss instanceof BossIsolde) {
            BossIsolde bossIsolde = (BossIsolde) currentBoss;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (GlacialBreath gb : bossIsolde.getGlacialBreaths()) {
                if (gb.isActive()) {
                    gb.render(shapeRenderer);
                }
            }
            shapeRenderer.end();
        }
    }

    private void spawnEnemies(float delta) {
        // Don't spawn regular enemies during boss fights
        if (currentBoss != null && !currentBoss.isDead()) {
            return;
        }

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
        Rectangle playerBounds = player.getBounds();

        // Boss projectiles vs Player
        for (Projectile proj : bossProjectiles) {
            if (proj.active && proj.getBounds().overlaps(playerBounds)) {
                player.takeDamage(proj.getDamage());
                // Break freeze when damaged
                if (player.isFrozen()) {
                    player.clearFreeze();
                }
                proj.active = false; // Deactivate after hit
                System.out.println("[Boss] Projectile hit player for " + proj.getDamage() + " damage!");
            }
        }

        // Cek Peluru vs Musuh
        for (Projectile p : activeProjectiles) {
            Rectangle pBounds = p.getBounds();
            for (int i = 0; i < activeEnemies.size; i++) {
                DummyEnemy e = activeEnemies.get(i);
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
            if (!m.isActive())
                continue;
            Rectangle mBounds = m.getBounds();
            for (int i = 0; i < activeEnemies.size; i++) {
                DummyEnemy e = activeEnemies.get(i);
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
                if (!gb.isActive())
                    continue;
                for (int i = 0; i < activeEnemies.size; i++) {
                    DummyEnemy e = activeEnemies.get(i);
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

        // Cek Hurricane Bind (Whisperwind's skill) vs Musuh
        if (player instanceof Whisperwind) {
            Whisperwind whisperwind = (Whisperwind) player;
            for (Projectile hurricaneProjectile : whisperwind.getHurricaneProjectiles()) {
                for (int i = 0; i < activeEnemies.size; i++) {
                    DummyEnemy e = activeEnemies.get(i);
                    if (hurricaneProjectile.getBounds().overlaps(e.getBounds())) {
                        e.takeDamage(hurricaneProjectile.getDamage());
                        hurricaneProjectile.active = false; // Deactivate projectile on hit

                        if (e.isDead()) {
                            player.gainXp(e.getXpReward());
                            System.out.println("[Whisperwind] Enemy killed by Hurricane Bind!");
                        }
                    }
                }
            }
        }

        // Cek Hellfire Pillar (Blaze's skill) vs Musuh
        if (player instanceof Blaze) {
            Blaze blaze = (Blaze) player;
            if (blaze.isPillarActive()) {
                Vector2 pillarPos = blaze.getPillarPosition();
                float pillarRadius = blaze.getPillarRadius();
                float pillarDamage = blaze.getArts() * 2.0f;

                // Damage regular enemies
                for (int i = 0; i < activeEnemies.size; i++) {
                    DummyEnemy e = activeEnemies.get(i);
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

                // Damage boss if active
                if (currentBoss != null && !currentBoss.isDead()) {
                    float bossCenterX = currentBoss.getPosition().x + currentBoss.getVisualWidth() / 2;
                    float bossCenterY = currentBoss.getPosition().y + currentBoss.getVisualHeight() / 2;
                    float distance = pillarPos.dst(bossCenterX, bossCenterY);

                    if (distance <= pillarRadius) {
                        currentBoss.takeDamage(pillarDamage * delta);
                        if (currentBoss.isDead()) {
                            handleBossDefeat(currentBoss);
                        }
                    }
                }
            }
        }

        // BOSS MELEE ATTACKS vs PLAYER - Critical collision detection
        if (currentBoss != null && !currentBoss.isDead()) {
            // Reuse playerBounds already declared at the top of checkCollisions()
            if (!player.isDead()) {

                // Get boss melee attacks from collected array
                for (MeleeAttack attack : bossMeleeAttacks) {
                    if (!attack.isActive())
                        continue;

                    // Check if boss attack hits player
                    if (attack.getBounds().overlaps(playerBounds) && attack.canHit(player)) {
                        // Apply damage to player
                        player.takeDamage(attack.getDamage());

                        // Break freeze if player is frozen
                        if (player.isFrozen()) {
                            player.clearFreeze();
                        }

                        // Mark attack as having hit player (prevents multiple hits)
                        attack.markAsHit(player);

                        System.out.println("[Boss] Melee attack hit player! Damage: " + attack.getDamage());
                    }
                }
            }
        }

        // Insania Skill - affects both enemies and boss
        if (player instanceof Insania) {
            Insania insania = (Insania) player;
            if (insania.hasJustUsedMindFracture()) {
                float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
                float skillRadius = insania.getSkillRadius();
                float baseDamage = player.getArts() * 1.2f;

                // Damage regular enemies
                for (int i = 0; i < activeEnemies.size; i++) {
                    DummyEnemy e = activeEnemies.get(i);
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

                // Damage boss if active (Mind Fracture now damages boss)
                if (currentBoss != null && !currentBoss.isDead()) {
                    float bossCenterX = currentBoss.getPosition().x + currentBoss.getVisualWidth() / 2;
                    float bossCenterY = currentBoss.getPosition().y + currentBoss.getVisualHeight() / 2;
                    float dx = bossCenterX - playerCenterX;
                    float dy = bossCenterY - playerCenterY;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                    if (distance <= skillRadius) {
                        currentBoss.makeInsane(3.0f); // Apply insanity
                        currentBoss.takeDamage(baseDamage); // APPLY DAMAGE

                        // Check if boss died from damage
                        if (currentBoss.isDead()) {
                            handleBossDefeat(currentBoss);
                        } else {
                            System.out.println("[Player] Mind Fracture hit boss! Damage: " + baseDamage +
                                    ", Insanity applied for 3s (Boss ATK/ARTS increased by 50%)");
                        }
                    }
                }
            }
        }

        // Cek Musuh vs Player
        for (int i = 0; i < activeEnemies.size; i++) {
            DummyEnemy e = activeEnemies.get(i);
            if (e.getBounds().overlaps(playerBounds)) {
                if (e.canAttack()) {
                    float damage = 10;

                    // Apply Ice Shield damage reduction if active
                    if (player.hasSecondarySkill() &&
                            player.getSecondarySkill() instanceof com.kelompok2.frontend.skills.IceShieldSkill) {
                        com.kelompok2.frontend.skills.IceShieldSkill iceShield = (com.kelompok2.frontend.skills.IceShieldSkill) player
                                .getSecondarySkill();

                        if (iceShield.isShieldActive()) {
                            damage *= iceShield.getDamageReduction(); // 50% reduction
                            System.out.println("[Ice Shield] Blocked damage! Reduced to: " + damage);
                        }
                    }

                    player.takeDamage(damage);
                    e.resetAttackTimer();
                }
            }
        }

        // Friendly Fire (Insane enemies)
        for (int i = 0; i < activeEnemies.size; i++) {
            DummyEnemy insaneEnemy = activeEnemies.get(i);
            if (!insaneEnemy.isInsane())
                continue;
            for (int j = 0; j < activeEnemies.size; j++) {
                if (i == j)
                    continue;
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

        // ========== BOSS COLLISION DETECTION ==========
        if (currentBoss != null && !currentBoss.isDead()) {
            // Player projectiles vs Boss
            for (Projectile p : activeProjectiles) {
                if (p.getBounds().overlaps(currentBoss.getBounds())) {
                    p.active = false;
                    currentBoss.takeDamage(p.getDamage());
                    if (currentBoss.isDead()) {
                        handleBossDefeat(currentBoss);
                        break; // Boss is now null, exit loop
                    }
                }
            }

            // Melee attacks vs Boss
            for (MeleeAttack m : meleeAttacks) {
                if (!m.isActive())
                    continue;
                if (m.getBounds().overlaps(currentBoss.getBounds()) && m.canHit(currentBoss)) {
                    float damage = m.getDamage();
                    currentBoss.takeDamage(damage);
                    m.markAsHit(currentBoss);
                    if (currentBoss.isDead()) {
                        handleBossDefeat(currentBoss);
                        break; // Boss is now null, exit loop
                    }
                }
            }

            // Boss-specific skill damage to player
            // BossInsania Mind Fracture
            if (currentBoss instanceof BossInsania) {
                BossInsania bossInsania = (BossInsania) currentBoss;
                if (bossInsania.hasJustUsedMindFracture()) {
                    float bossCenterX = currentBoss.getPosition().x + currentBoss.getVisualWidth() / 2;
                    float bossCenterY = currentBoss.getPosition().y + currentBoss.getVisualHeight() / 2;
                    float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                    float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;

                    float dx = playerCenterX - bossCenterX;
                    float dy = playerCenterY - bossCenterY;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                    if (distance <= bossInsania.getSkillRadius()) {
                        // Apply insanity to player (reduces damage dealt)
                        player.makeInsane(1.0f); // 1 second insanity (reduced from 5)
                        System.out.println("[BossInsania] Mind Fracture drove player insane!");
                    }
                }
            }

            // BossBlaze Hellfire Pillar
            if (currentBoss instanceof BossBlaze) {
                BossBlaze bossBlaze = (BossBlaze) currentBoss;
                if (bossBlaze.isPillarActive()) {
                    Vector2 pillarPos = bossBlaze.getPillarPosition();
                    float pillarRadius = bossBlaze.getPillarRadius();
                    float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                    float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;

                    float distance = pillarPos.dst(playerCenterX, playerCenterY);
                    if (distance <= pillarRadius) {
                        float pillarDamage = currentBoss.getArts() * 2.0f * delta;
                        player.takeDamage(pillarDamage);
                    }
                }
            }

            // BossIsolde Glacial Breath
            if (currentBoss instanceof BossIsolde) {
                BossIsolde bossIsolde = (BossIsolde) currentBoss;
                for (GlacialBreath gb : bossIsolde.getGlacialBreaths()) {
                    if (!gb.isActive())
                        continue;
                    if (gb.canHit(player)) {
                        player.takeDamage(gb.getDamage());
                        // Break freeze when damaged (after taking damage)
                        if (player.isFrozen()) {
                            player.clearFreeze();
                        }
                        player.freeze(3.0f); // Freeze player for 3 seconds
                        gb.markAsHit(player);
                        System.out.println("[BossIsolde] Glacial Breath froze player!");
                    }
                }
            }

            // Player skills vs Boss (apply status effects)
            if (currentBoss != null && !currentBoss.isDead()) {

                // Glacial Breath from player Isolde - freezes AND DAMAGES boss
                if (player instanceof Isolde) {
                    Isolde isolde = (Isolde) player;
                    for (GlacialBreath gb : isolde.getGlacialBreaths()) {
                        if (!gb.isActive())
                            continue;
                        if (gb.canHit(currentBoss)) {
                            currentBoss.takeDamage(gb.getDamage()); // APPLY DAMAGE

                            // Check if boss died from damage
                            if (currentBoss.isDead()) {
                                handleBossDefeat(currentBoss);
                                break; // Exit loop since boss is defeated
                            } else {
                                currentBoss.freeze(2.0f); // Freeze boss for 2 seconds
                                gb.markAsHit(currentBoss);
                                System.out.println("[Player] Glacial Breath hit boss! Damage: " + gb.getDamage());
                            }
                        }
                    }
                }

                // Hurricane Bind from player Whisperwind - STUNS (freezes) and DAMAGES boss
                if (player instanceof Whisperwind) {
                    Whisperwind whisperwind = (Whisperwind) player;
                    Rectangle bossBounds = currentBoss.getBounds();
                    for (Projectile hurricaneProj : whisperwind.getHurricaneProjectiles()) {
                        if (hurricaneProj.active && hurricaneProj.getBounds().overlaps(bossBounds)) {
                            currentBoss.takeDamage(hurricaneProj.getDamage()); // APPLY DAMAGE
                            hurricaneProj.active = false; // CRITICAL: Prevent multiple hits

                            // Check if boss died from damage
                            if (currentBoss.isDead()) {
                                handleBossDefeat(currentBoss);
                                break; // Exit loop since boss is defeated
                            } else {
                                currentBoss.freeze(1.5f); // Stun (freeze) boss for 1.5 seconds
                                System.out.println(
                                        "[Player] Hurricane Bind hit boss! Damage: " + hurricaneProj.getDamage()
                                                + ", Stunned for 1.5s");
                            }
                        }
                    }
                }

                // Mind Fracture from player Insania - applies insanity to boss
                if (player instanceof Insania) {
                    Insania insania = (Insania) player;
                    if (insania.hasJustUsedMindFracture()) {
                        float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                        float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
                        float bossCenterX = currentBoss.getPosition().x + currentBoss.getVisualWidth() / 2;
                        float bossCenterY = currentBoss.getPosition().y + currentBoss.getVisualHeight() / 2;

                        float dx = bossCenterX - playerCenterX;
                        float dy = bossCenterY - playerCenterY;
                        float distance = (float) Math.sqrt(dx * dx + dy * dy);

                        if (distance <= insania.getSkillRadius()) {
                            currentBoss.makeInsane(3.0f); // Apply insanity once per activation
                            System.out.println("[Player] Mind Fracture hit boss! Boss is now insane.");
                        }
                    }
                }

                // Verdant Domain from player Aelita - healing zone + stat buff
                if (player instanceof com.kelompok2.frontend.entities.Aelita) {
                    com.kelompok2.frontend.entities.Aelita aelita = (com.kelompok2.frontend.entities.Aelita) player;
                    com.kelompok2.frontend.skills.VerdantDomainSkill vd = aelita.getVerdantDomain();

                    if (vd.isZoneActive()) {
                        Vector2 zonePos = vd.getZonePosition();
                        float zoneRadius = vd.getZoneRadius();
                        float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                        float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;

                        float distance = zonePos.dst(playerCenterX, playerCenterY);

                        if (distance <= zoneRadius) {
                            // Apply healing (per frame)
                            float healPerFrame = vd.getHealPerSecond() * delta;
                            player.heal(healPerFrame);

                            // Apply stat buff
                            boolean wasUnbuffed = (aelita.getAtkBuffMultiplier() != vd.getAtkBuff());
                            aelita.applyVerdantBuff(vd.getAtkBuff(), vd.getArtsBuff());

                            if (wasUnbuffed) {
                                System.out.println("[Verdant Domain] Buff applied! ATK: " + aelita.getAtk() +
                                        " (" + (int) (aelita.getAtk() / aelita.getAtkBuffMultiplier()) + " base × "
                                        + vd.getAtkBuff() + "), " +
                                        "Arts: " + aelita.getArts() +
                                        " (" + (int) (aelita.getArts() / aelita.getArtsBuffMultiplier()) + " base × "
                                        + vd.getArtsBuff() + ")");
                            }
                        } else {
                            // Clear buff if outside zone
                            if (aelita.getAtkBuffMultiplier() != 1.0f) {
                                System.out.println("[Verdant Domain] Buff cleared (outside zone). ATK: "
                                        + aelita.getAtk() + ", Arts: " + aelita.getArts());
                            }
                            aelita.clearVerdantBuff();
                        }
                    } else {
                        // Clear buff if zone inactive
                        if (aelita.getAtkBuffMultiplier() != 1.0f) {
                            System.out.println("[Verdant Domain] Buff cleared (zone inactive)");
                        }
                        aelita.clearVerdantBuff();
                    }
                }
            }
        }

        // Check secondary skill collisions
        checkSecondarySkillCollisions();
    }

    private void checkSecondarySkillCollisions() {
        if (!player.hasSecondarySkill())
            return;

        // Blade Fury - AoE damage around player
        if (player.getSecondarySkill() instanceof com.kelompok2.frontend.skills.BladeFurySkill) {
            com.kelompok2.frontend.skills.BladeFurySkill bladeFury = (com.kelompok2.frontend.skills.BladeFurySkill) player
                    .getSecondarySkill();

            if (bladeFury.isSkillActive()) {
                float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
                float radius = bladeFury.getRadius();
                float damage = bladeFury.getCurrentHitDamage();

                // Check all enemies in radius
                Array<DummyEnemy> enemies = enemyPool.getActiveEnemies();
                for (int i = 0; i < enemies.size; i++) {
                    DummyEnemy enemy = enemies.get(i);
                    float enemyCenterX = enemy.getPosition().x + enemy.getWidth() / 2;
                    float enemyCenterY = enemy.getPosition().y + enemy.getHeight() / 2;

                    float distance = (float) Math.sqrt(
                            Math.pow(enemyCenterX - playerCenterX, 2) +
                                    Math.pow(enemyCenterY - playerCenterY, 2));

                    if (distance <= radius) {
                        enemy.takeDamage(damage);

                        // Give XP if enemy dies
                        if (enemy.isDead()) {
                            player.gainXp(enemy.getXpReward());
                            System.out.println("[Blade Fury] Enemy killed! XP gained");
                        }
                    }
                }
            }
        }

        // Ground Slam - Shockwave damage with stun
        if (player.getSecondarySkill() instanceof com.kelompok2.frontend.skills.GroundSlamSkill) {
            com.kelompok2.frontend.skills.GroundSlamSkill groundSlam = (com.kelompok2.frontend.skills.GroundSlamSkill) player
                    .getSecondarySkill();

            if (groundSlam.isShockwaveActive()) {
                Vector2 slamPos = groundSlam.getShockwavePosition();
                float radius = groundSlam.getRadius();
                float damage = groundSlam.getDamage();

                // Check all enemies in radius (only damage once when shockwave first appears)
                // Use indexed loop to avoid ConcurrentModificationException
                Array<DummyEnemy> enemies = enemyPool.getActiveEnemies();
                for (int i = 0; i < enemies.size; i++) {
                    DummyEnemy enemy = enemies.get(i);
                    float enemyCenterX = enemy.getPosition().x + enemy.getWidth() / 2;
                    float enemyCenterY = enemy.getPosition().y + enemy.getHeight() / 2;

                    float distance = (float) Math.sqrt(
                            Math.pow(enemyCenterX - slamPos.x, 2) +
                                    Math.pow(enemyCenterY - slamPos.y, 2));

                    if (distance <= radius) {
                        enemy.takeDamage(damage);

                        // Give XP if enemy dies
                        if (enemy.isDead()) {
                            player.gainXp(enemy.getXpReward());
                            System.out.println("[Ground Slam] Enemy killed! XP gained");
                        }
                        // TODO: Add stun effect when enemy has stun state
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

    // ==================== BOSS SYSTEM METHODS ====================

    /**
     * Update boss spawning timer and spawn boss when ready
     */
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

    /**
     * Spawn a random boss from available pool
     */
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
        float x = player.getPosition().x + MathUtils.cosDeg(angle) * distance;
        float y = player.getPosition().y + MathUtils.sinDeg(angle) * distance;

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

        System.out.println("[Boss] " + bossName + " has spawned! Level: " + playerLevel);
        System.out.println("[Boss] Position: x=" + x + ", y=" + y);

        // Play boss-specific music
        playBossMusic(bossName);

        // Start camera pan to boss
        float bossCenterX = x + currentBoss.getVisualWidth() / 2;
        float bossCenterY = y + currentBoss.getVisualHeight() / 2;
        System.out.println("[Boss] Calling startBossPanSequence to: x=" + bossCenterX + ", y=" + bossCenterY);
        startBossPanSequence(new Vector2(bossCenterX, bossCenterY));
    }

    /**
     * Start camera pan sequence to boss
     */
    private void startBossPanSequence(Vector2 bossPosition) {
        cameraPanningToBoss = true;
        cameraPanTimer = 0f;
        // Don't use isPaused - it blocks rendering. Entities freeze via
        // cameraPanningToBoss check

        // Save original camera state
        originalCameraZoom = camera.zoom;

        // Store original camera position (player position)
        originalCameraPos.set(
                player.getPosition().x + player.getVisualWidth() / 2,
                player.getPosition().y + player.getVisualHeight() / 2);

        // Set target to boss position
        targetCameraPos.set(bossPosition);

        // Freeze player during camera pan (same duration as enemies)
        player.freeze(CAMERA_PAN_DURATION);

        // Freeze all enemies during camera pan (for full duration)
        for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
            enemy.freeze(CAMERA_PAN_DURATION); // Freeze for entire pan duration
        }

        System.out.println("[Camera] Starting boss pan sequence");
        System.out.println(
                "[Camera] From: (" + originalCameraPos.x + ", " + originalCameraPos.y + ") zoom=" + originalCameraZoom);
        System.out
                .println("[Camera] To: (" + targetCameraPos.x + ", " + targetCameraPos.y + ") zoom=" + BOSS_ZOOM_LEVEL);
    }

    /**
     * Update camera panning animation
     */
    private void updateCameraPan(float delta) {
        // Cap delta to prevent instant completion (max 33ms per frame = 30 FPS)
        float cappedDelta = Math.min(delta, 0.033f);

        cameraPanTimer += cappedDelta;

        // Phase 1: Pan to boss (0.0 - 1.0 seconds)
        if (cameraPanTimer < CAMERA_PAN_TO_BOSS_DURATION) {
            float phaseProgress = cameraPanTimer / CAMERA_PAN_TO_BOSS_DURATION;
            // Smooth interpolation (ease-in-out)
            float smoothProgress = phaseProgress * phaseProgress * (3f - 2f * phaseProgress);

            camera.position.x = originalCameraPos.x + (targetCameraPos.x - originalCameraPos.x) * smoothProgress;
            camera.position.y = originalCameraPos.y + (targetCameraPos.y - originalCameraPos.y) * smoothProgress;
            camera.zoom = originalCameraZoom + (BOSS_ZOOM_LEVEL - originalCameraZoom) * smoothProgress;
        }
        // Phase 2: Hold on boss (1.0 - 4.0 seconds)
        else if (cameraPanTimer < CAMERA_PAN_TO_BOSS_DURATION + CAMERA_HOLD_DURATION) {
            // Keep camera focused on boss
            camera.position.x = targetCameraPos.x;
            camera.position.y = targetCameraPos.y;
            camera.zoom = BOSS_ZOOM_LEVEL;
        }
        // Phase 3: Pan back to player (4.0 - 5.0 seconds)
        else if (cameraPanTimer < CAMERA_PAN_DURATION) {
            float phaseProgress = (cameraPanTimer - CAMERA_PAN_TO_BOSS_DURATION - CAMERA_HOLD_DURATION)
                    / CAMERA_PAN_BACK_DURATION;
            // Smooth interpolation (ease-in-out)
            float smoothProgress = phaseProgress * phaseProgress * (3f - 2f * phaseProgress);

            camera.position.x = targetCameraPos.x + (originalCameraPos.x - targetCameraPos.x) * smoothProgress;
            camera.position.y = targetCameraPos.y + (originalCameraPos.y - targetCameraPos.y) * smoothProgress;
            camera.zoom = BOSS_ZOOM_LEVEL + (originalCameraZoom - BOSS_ZOOM_LEVEL) * smoothProgress;
        }
        // Phase 4: Complete
        else {
            cameraPanningToBoss = false;
            camera.position.x = originalCameraPos.x;
            camera.position.y = originalCameraPos.y;
            camera.zoom = originalCameraZoom; // Ensure exact restoration

            // Explicitly unfreeze all entities to prevent lingering freeze effects
            player.clearFreeze();
            for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
                enemy.setFrozen(false); // Force unfreeze
            }

            System.out.println("[Camera] Boss pan sequence complete - all entities unfrozen");
        }

        camera.update();
    }

    /**
     * Play boss-specific music theme
     */
    private void playBossMusic(String bossName) {
        String musicPath;

        switch (bossName) {
            case "Insania":
                musicPath = "Audio/BossThemes/InsaniaBossTheme.mp3";
                break;
            case "Blaze":
                musicPath = "Audio/BossThemes/BlazeBossTheme.mp3";
                break;
            case "Isolde":
                musicPath = "Audio/BossThemes/IsoldeBossTheme.mp3";
                break;
            default:
                musicPath = "Audio/battleThemeA.mp3"; // Fallback
                break;
        }

        AudioManager.getInstance().stopMusic();
        AudioManager.getInstance().playMusic(musicPath, true);
        System.out.println("[Audio] Playing boss music: " + musicPath);
    }

    /**
     * Handle boss defeat - drop ultimate and clear boss
     */
    private void handleBossDefeat(Boss boss) {
        System.out.println("[Boss] " + boss.getBossName() + " defeated!");

        // Create ultimate skill
        Skill ultimate = boss.createUltimateSkill();

        // Set enemy array for ultimate skills (they need access to damage enemies)
        if (ultimate instanceof InsanityBurstSkill) {
            ((InsanityBurstSkill) ultimate).setEnemies(enemyPool.getActiveEnemies());
        } else if (ultimate instanceof InfernoNovaSkill) {
            ((InfernoNovaSkill) ultimate).setEnemies(enemyPool.getActiveEnemies());
        } else if (ultimate instanceof FrozenApocalypseSkill) {
            ((FrozenApocalypseSkill) ultimate).setEnemies(enemyPool.getActiveEnemies());
        }

        // Give ultimate to player
        player.setUltimateSkill(ultimate);

        // Grant large XP reward for defeating boss (500 base XP)
        float bossXP = 500f;
        player.gainXp(bossXP);
        System.out.println("[Player] Gained " + bossXP + " XP from boss kill!");

        // Clear boss
        currentBoss = null;

        // Resume normal BGM
        AudioManager.getInstance().stopMusic();
        AudioManager.getInstance().playMusic("Audio/battleThemeA.mp3", true);
    }

    /**
     * Draw boss health bar above the boss
     * NOTE: Called from within drawHealthBars(), so ShapeRenderer is already active
     */
    private void drawBossHealthBar(Boss boss) {
        if (boss == null || boss.isDead())
            return;

        // Position health bar above the boss
        float x = boss.getPosition().x;
        float y = boss.getPosition().y + boss.getVisualHeight() + 10f; // Above boss
        float barWidth = boss.getVisualWidth() * 2f; // Wider than boss for visibility
        float barHeight = 8f; // Thicker than regular enemy bars

        // Background (dark red)
        shapeRenderer.setColor(new Color(0.3f, 0, 0, 1f));
        shapeRenderer.rect(x, y, barWidth, barHeight);

        // Health fill
        float healthPercent = boss.getHp() / boss.getMaxHp();
        Color healthColor;
        if (healthPercent > 0.5f) {
            healthColor = Color.GOLD; // Gold for bosses
        } else if (healthPercent > 0.25f) {
            healthColor = Color.ORANGE;
        } else {
            healthColor = Color.RED;
        }
        shapeRenderer.setColor(healthColor);
        shapeRenderer.rect(x, y, barWidth * healthPercent, barHeight);
    }

    @Override
    public void dispose() {
        if (isDisposed) {
            System.out.println("[GameScreen] Already disposed, skipping...");
            return;
        }
        isDisposed = true;

        System.out.println("[GameScreen] Disposing resources...");
        if (batch != null)
            batch.dispose();
        if (shapeRenderer != null)
            shapeRenderer.dispose();
        if (bossFont != null)
            bossFont.dispose();
        if (player != null)
            player.dispose(); // Character cleanup (doesn't dispose textures - AssetManager owns those)
        if (projectilePool != null)
            projectilePool.dispose();
        if (enemyPool != null)
            enemyPool.dispose();
        System.out.println("[GameScreen] Dispose complete");
    }

    public void pauseForLevelUp() {
        isPaused = true;
        // PENTING: Passing 'this' (GameScreen instance) ke LevelUpScreen
        // agar LevelUpScreen bisa merender background game yang beku.
        game.setScreen(new LevelUpScreen(game, this, player));
        System.out.println("[GameScreen] Game paused for level-up selection");
    }

    public void resumeFromLevelUp() {
        isPaused = false;
        System.out.println("[GameScreen] Game resumed from level-up");
    }
}
