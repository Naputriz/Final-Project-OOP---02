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
import com.kelompok2.frontend.entities.Whisperwind;
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
    private boolean isDisposed = false; // Guard against double disposal

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

            // Cek Game Over - but skip if screen is disposed
            if (!isDisposed && player.isDead()) {
                GameManager.getInstance().setGameOver(true);
                int finalLvl = GameManager.getInstance().getCurrentLevel();
                float finalTime = GameManager.getInstance().getGameTime();

                System.out.println("Game over! Switching to GameOverScreen...");
                ((Main) Gdx.app.getApplicationListener()).setScreen(new GameOverScreen(game, selectedCharacter, finalLvl, finalTime));
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

        // Render Hurricane Bind projectiles (Whisperwind)
        renderHurricaneBinds();

        // Render Mind Fracture circle (untuk visual feedback)
        renderMindFracture();
        renderHellfirePillar();

        // Render Secondary Skills visuals
        renderBladeFury();
        renderGroundSlam();
        renderIceShield();

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

        // Render HP Musuh dari pool
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
            }
        }

        // Cek Musuh vs Player
        Rectangle playerBounds = player.getBounds();
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

    @Override
    public void dispose() {
        // Guard against double disposal
        if (isDisposed) {
            System.out.println("[GameScreen] Already disposed, skipping");
            return;
        }
        isDisposed = true;

        System.out.println("[GameScreen] Disposing resources...");
        if (batch != null)
            batch.dispose();
        if (shapeRenderer != null)
            shapeRenderer.dispose();
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
