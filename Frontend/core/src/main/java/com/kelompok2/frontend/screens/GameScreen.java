package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
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
    private GameCharacter player; // Changed to GameCharacter untuk support multiple characters
    private InputHandler inputHandler;
    private OrthographicCamera camera; // Kamera game
    private Texture background; // Background sementara biar kelihatan gerak

    private ProjectilePool projectilePool; // Object Pool untuk projectiles (Object Pool Pattern)
    private Array<MeleeAttack> meleeAttacks; // List untuk menampung semua melee attacks yang sedang aktif
    private EnemyPool enemyPool; // Object Pool untuk enemies (Object Pool Pattern)

    private float spawnTimer = 0; // Timer buat spawn musuh tiap detik

    private Main game; // Reference to the main game class
    private String selectedCharacter; // The character selected by the player

    private boolean isPaused = false; // Flag untuk pause state (saat level-up screen)

    public GameScreen(Main game, String selectedCharacter) {
        this.game = game;
        this.selectedCharacter = selectedCharacter;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Inisialisasi Object Pools (Object Pool Pattern)
        projectilePool = new ProjectilePool(50); // Pool 50 projectiles
        meleeAttacks = new Array<>();

        // Setup Kamera
        camera = new OrthographicCamera();
        // Set ukuran viewport kamera (seberapa luas dunia yang terlihat)
        camera.setToOrtho(false, 1280, 720);

        // Load background melalui AssetManager (Singleton Pattern)
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
                // Default to Isolde if unknown
                player = new Isolde(0, 0);
                selectedCharacter = "Isolde";
                break;
        }

        // Taruh player di tengah map
        player.setPosition(0, 0);

        // Inisialisasi EnemyPool setelah player dibuat
        enemyPool = new EnemyPool(player, 30); // Pool 30 enemies

        // Inisialisasi GameManager untuk game baru
        GameManager.getInstance().startNewGame(selectedCharacter);

        // Setup Input Handler dengan Kamera, ProjectilePool, dan MeleeAttacks array
        inputHandler = new InputHandler(player, camera, projectilePool, meleeAttacks);

        // Play battle BGM
        AudioManager.getInstance().playMusic("Audio/battleThemeA.mp3", true);
    }

    @Override
    public void render(float delta) {
        // Cek level-up pending, jika ya tampilkan LevelUpScreen
        if (player.isLevelUpPending() && !isPaused) {
            pauseForLevelUp();
            return;
        }

        // Skip update jika game di-pause
        if (isPaused) {
            return;
        }

        // UPDATE LOGIC
        player.update(delta);
        inputHandler.update(delta);

        // Update pools
        projectilePool.update(delta); // Auto-frees inactive projectiles
        updateMeleeAttacks(delta);
        enemyPool.update(delta); // Auto-frees dead enemies

        checkCollisions(delta); // Cek tabrakan
        spawnEnemies(delta); // Spawn musuh baru

        // Update GameManager state
        GameManager.getInstance().updateGameTime(delta);

        // Cek Game Over
        if (player.isDead()) {
            GameManager.getInstance().setGameOver(true);

            int finalLvl = GameManager.getInstance().getCurrentLevel();
            float finalTime = GameManager.getInstance().getGameTime();

            System.out.println("Game over! Switching to GameOverScreen...");

            // --- PERUBAHAN DI SINI ---
            // Panggil GameOverScreen alih-alih MainMenuScreen
            ((Main) Gdx.app.getApplicationListener())
                    .setScreen(new GameOverScreen(game, selectedCharacter, finalLvl, finalTime));

            return; // Stop render frame ini
        }

        // Update posisi kamera agar selalu mengikuti player
        camera.position.set(
                player.getPosition().x + player.getVisualWidth() / 2,
                player.getPosition().y + player.getVisualHeight() / 2,
                0);
        camera.update(); // Apply perubahan kamera

        // 2. CLEAR SCREEN
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 3. DRAW
        // Set projection matrix agar batch menggambar sesuai pandangan kamera
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.begin();

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
        player.render(batch);

        // Render menggunakan pools
        projectilePool.render(batch);
        enemyPool.render(batch);

        batch.end();

        // Render melee attack hitboxes (untuk visual feedback)
        renderMeleeAttacks();

        // Render Glacial Breath cones (untuk visual feedback)
        renderGlacialBreaths();

        // Render Hurricane Bind projectiles (Whisperwind)
        renderHurricaneBinds();

        // Render Mind Fracture circle (untuk visual feedback)
        renderMindFracture();

        // Render Hellfire Pillar circle (untuk visual feedback)
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
        // XP Bar at bottom (closest to character)
        float y = character.getPosition().y + character.getVisualHeight() + 13;

        float width = character.getVisualWidth();
        float height = 4; // Tinggi XP bar

        // Cek dividen 0 untuk mencegah crash (ArithmeticException/NaN)
        float maxXp = character.getXpToNextLevel();
        float xpPercent = (maxXp > 0) ? character.getCurrentXp() / maxXp : 0;

        // Background (Abu-abu Tua)
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, width, height);

        // Isi XP (Cyan/Biru Muda)
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
        // ------------------------

        float width = character.getVisualWidth();
        float height = 5;

        float hpPercent = character.getHp() / character.getMaxHp();

        // Background Merah
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, y, width, height);

        // Foreground Hijau
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
        // Skill Cooldown Bar in middle (below HP, above XP)
        // HP at +13 (height 5) ends at +18, Skill starts at +19
        float y = character.getPosition().y + character.getVisualHeight() + 19;

        float width = character.getVisualWidth();
        float height = 4; // Same height as XP bar

        // Get skill cooldown data from player
        float skillTimer = 0f;
        float skillCooldown = 1f; // Default to avoid division by zero


        // Use Template Method pattern - polymorphic call instead of instanceof
        skillTimer = character.getInnateSkillTimer();
        skillCooldown = character.getInnateSkillCooldown();

        // Safety check to avoid division by zero
        if (skillCooldown == 0) {
            skillCooldown = 1f;
        }

        // Calculate percentage remaining (skill is ready when timer = 0)
        float cooldownPercent = (skillCooldown > 0) ? (skillCooldown - skillTimer) / skillCooldown : 1f;


        // Background (Dark gray)
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, width, height);

        // Foreground (Yellow/Orange for skill cooldown)
        shapeRenderer.setColor(new Color(1f, 0.8f, 0f, 1f)); // Yellow-orange
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

    // Render melee attacks dengan sprite animations
    private void renderMeleeAttacks() {
        batch.begin();
        for (MeleeAttack m : meleeAttacks) {
            if (m.isActive()) {
                m.render(batch);
            }
        }
        batch.end();
    }

    // Render Glacial Breath cones untuk visual feedback
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

            // Get player center position
            float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
            float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;

            // Draw semi-transparent purple circle outline (thicker for visibility)
            Gdx.gl.glLineWidth(3); // Thicker line
            shapeRenderer.setColor(0.8f, 0.3f, 0.8f, 0.7f); // Purple semi-transparent outline
            shapeRenderer.circle(playerCenterX, playerCenterY, insania.getSkillRadius(), 50);
            Gdx.gl.glLineWidth(1); // Reset line width

            shapeRenderer.end();
        }
    }

    // Render Hellfire Pillar visual circle untuk Blaze
    private void renderHellfirePillar() {
        if (!(player instanceof Blaze))
            return;

        Blaze blaze = (Blaze) player;
        if (blaze.isPillarActive()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            // Draw orange semi-transparent circle
            shapeRenderer.setColor(1f, 0.5f, 0f, 0.4f); // Orange, semi-transparent
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
                String spritePath = animationType.equals("scratch") ? "AttackAnimations/Scratch Animation.png" : "AttackAnimations/Slash Animation.png";

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
        if (spawnTimer > 1.5f) { // Spawn tiap 1.5 detik
            // Spawn di posisi random sekitar player (radius 600-800 pixel)
            float angle = MathUtils.random(360);
            float distance = MathUtils.random(600, 800);
            float x = player.getPosition().x + MathUtils.cosDeg(angle) * distance;
            float y = player.getPosition().y + MathUtils.sinDeg(angle) * distance;

            // Gunakan Factory Method untuk mendapatkan tipe enemy berdasarkan level
            int currentLevel = GameManager.getInstance().getCurrentLevel();
            EnemyType type = EnemyFactory.getRandomEnemyType(currentLevel);

            // Dapatkan enemy dari pool
            // Factory type akan digunakan saat ada lebih banyak tipe enemy
            DummyEnemy enemy = enemyPool.obtain(x, y);

            spawnTimer = 0;

        }
    }

    private void checkCollisions(float delta) {
        // Ambil active arrays dari pools
        Array<Projectile> activeProjectiles = projectilePool.getActiveProjectiles();
        Array<DummyEnemy> activeEnemies = enemyPool.getActiveEnemies();

        // Cek Peluru vs Musuh
        for (Projectile p : activeProjectiles) {
            Rectangle pBounds = p.getBounds();

            for (DummyEnemy e : activeEnemies) {
                if (pBounds.overlaps(e.getBounds())) {
                    p.active = false; // Matikan peluru (pool akan auto-free)
                    e.takeDamage(p.getDamage());
                    if (e.isDead()) {
                        // Pool akan auto-free enemy yang mati di update()
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
            for (DummyEnemy e : activeEnemies) {
                if (mBounds.overlaps(e.getBounds()) && m.canHit(e)) {
                    float damage = m.getDamage();

                    // Insania deals bonus damage to insane enemies
                    if (player instanceof Insania && e.isInsane()) {
                        damage *= 1.5f; // 50% bonus damage
                        System.out.println("[Insania] Bonus damage to insane enemy! " + damage);
                    }

                    e.takeDamage(damage);
                    m.markAsHit(e);

                    if (e.isDead()) {
                        // Pool akan auto-free enemy yang mati
                        player.gainXp(e.getXpReward());
                        System.out.println("Enemy Killed by Melee!");
                    }
                }
            }
        }

        // Cek Glacial Breath (Isolde's skill) vs Musuh
        if (player instanceof Isolde) {
            Isolde isolde = (Isolde) player;
            for (GlacialBreath gb : isolde.getGlacialBreaths()) {
                if (!gb.isActive())
                    continue;

                for (DummyEnemy e : activeEnemies) {
                    if (gb.canHit(e)) {
                        e.takeDamage(gb.getDamage());
                        e.freeze(); // Apply freeze status!
                        gb.markAsHit(e);

                        if (e.isDead()) {
                            player.gainXp(e.getXpReward());
                            System.out.println("Enemy Killed by Glacial Breath!");
                        } else {
                            System.out.println("Enemy frozen for 3 seconds!");
                        }
                    }
                }
            }
        }

        // Cek Hurricane Bind (Whisperwind's skill) vs Mus uh
        if (player instanceof Whisperwind) {
            Whisperwind whisperwind = (Whisperwind) player;
            for (Projectile hurricaneProjectile : whisperwind.getHurricaneProjectiles()) {
                for (DummyEnemy e : activeEnemies) {
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
                float pillarDamage = blaze.getArts() * 2.0f; // High Arts-scaled damage per second

                for (DummyEnemy e : activeEnemies) {
                    // Check if enemy is within pillar radius
                    float enemyX = e.getBounds().x + e.getBounds().width / 2;
                    float enemyY = e.getBounds().y + e.getBounds().height / 2;
                    float distance = pillarPos.dst(enemyX, enemyY);

                    if (distance <= pillarRadius) {
                        e.takeDamage(pillarDamage * delta); // Continuous damage (DPS)

                        if (e.isDead()) {
                            player.gainXp(e.getXpReward());
                            System.out.println("[Blaze] Enemy killed by Hellfire Pillar!");
                        }
                    }
                }
            }
        }

        // Cek Mind Fracture (Insania's skill) - AoE Insanity debuff + Damage
        if (player instanceof Insania) {
            Insania insania = (Insania) player;
            if (insania.hasJustUsedMindFracture()) {
                // Get player center position
                float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
                float skillRadius = insania.getSkillRadius();

                // Calculate damage berdasarkan Arts scaling
                // Low damage karena Insania adalah physical attacker, ini lebih ke utility
                // skill
                float baseDamage = player.getArts() * 0.2f; // 20% Arts scaling untuk Mind Fracture

                int enemiesAffected = 0;

                // Apply Insanity + Damage to all enemies in radius
                // TODO: Bikin ini buat semua musuh, bukan DummyEnemy doang
                for (DummyEnemy e : activeEnemies) {
                    float enemyCenterX = e.getPosition().x + e.getVisualWidth() / 2;
                    float enemyCenterY = e.getPosition().y + e.getVisualHeight() / 2;

                    // Calculate distance to enemy
                    float dx = enemyCenterX - playerCenterX;
                    float dy = enemyCenterY - playerCenterY;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                    // Apply Insanity + Damage jika dalam radius
                    if (distance <= skillRadius) {
                        e.applyInsanity();
                        e.takeDamage(baseDamage);
                        enemiesAffected++;

                        if (e.isDead()) {
                            player.gainXp(e.getXpReward());
                            System.out.println("[Mind Fracture] Enemy killed by Mind Fracture!");
                        }
                    }
                }

                System.out.println("[Mind Fracture] " + enemiesAffected + " enemies affected! Damage: " + baseDamage);
            }
        }

        // Cek Musuh vs Player (collision damage)
        Rectangle playerBounds = player.getBounds();
        for (DummyEnemy e : activeEnemies) {
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

        // Cek Friendly Fire - Insane enemies damage other enemies
        for (int i = 0; i < activeEnemies.size; i++) {
            DummyEnemy insaneEnemy = activeEnemies.get(i);

            // Skip jika enemy ini tidak insane
            if (!insaneEnemy.isInsane())
                continue;

            // Check collision dengan musuh lain
            for (int j = 0; j < activeEnemies.size; j++) {
                if (i == j)
                    continue; // Skip self

                DummyEnemy targetEnemy = activeEnemies.get(j);

                // Check collision
                if (insaneEnemy.getBounds().overlaps(targetEnemy.getBounds())) {
                    if (insaneEnemy.canAttack()) {
                        // Friendly fire damage (use insane enemy's buffed ATK)
                        float damage = insaneEnemy.getAtk();
                        targetEnemy.takeDamage(damage);
                        insaneEnemy.resetAttackTimer();

                        System.out.println("[Friendly Fire] Insane enemy dealt " + damage + " damage!");

                        if (targetEnemy.isDead()) {
                            // Enemy killed by friendly fire
                            player.gainXp(targetEnemy.getXpReward());
                            System.out.println("[Friendly Fire] Enemy killed by friendly fire!");
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
                for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
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
                for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
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
        // Update ukuran kamera jika window di-resize
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        player.dispose();

        // Dispose pools
        projectilePool.dispose();
        enemyPool.dispose();

    }

    /**
     * Pause game dan tampilkan LevelUpScreen.
     */
    public void pauseForLevelUp() {
        isPaused = true;
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
