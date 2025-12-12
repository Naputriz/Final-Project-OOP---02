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
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.entities.Ryze;
import com.kelompok2.frontend.utils.InputHandler;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.GameManager;
import com.kelompok2.frontend.pools.ProjectilePool;
import com.kelompok2.frontend.pools.EnemyPool;
import com.kelompok2.frontend.factories.EnemyFactory;
import com.kelompok2.frontend.factories.EnemyType;

import java.util.Iterator;

public class GameScreen extends ScreenAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Ryze player;
    private InputHandler inputHandler;
    private OrthographicCamera camera; // Kamera game
    private Texture background; // Background sementara biar kelihatan gerak

    private ProjectilePool projectilePool; // Object Pool untuk projectiles (Object Pool Pattern)
    private Array<MeleeAttack> meleeAttacks; // List untuk menampung semua melee attacks yang sedang aktif
    private EnemyPool enemyPool; // Object Pool untuk enemies (Object Pool Pattern)

    private float spawnTimer = 0; // Timer buat spawn musuh tiap detik

    @Override
    public void show() {
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

        // Spawn Player
        player = new Ryze(0, 0);

        // Taruh player di tengah map
        player.setPosition(0, 0);

        // Inisialisasi EnemyPool setelah player dibuat
        enemyPool = new EnemyPool(player, 30); // Pool 30 enemies

        // Inisialisasi GameManager untuk game baru
        GameManager.getInstance().startNewGame("Ryze"); // TODO: Nanti diganti dari character selection

        // Setup Input Handler dengan Kamera, ProjectilePool, dan MeleeAttacks array
        inputHandler = new InputHandler(player, camera, projectilePool, meleeAttacks);
    }

    @Override
    public void render(float delta) {
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
            System.out.println("Game over! Final Level: " + GameManager.getInstance().getCurrentLevel() +
                    ", Time: " + String.format("%.1f", GameManager.getInstance().getGameTime()) + "s");
            // TODO: Kirim data ke backend di sini (Level, Character Name, Time)
            // Restart screen sederhana
            ((Main) Gdx.app.getApplicationListener())
                    .setScreen(new MainMenuScreen((Main) Gdx.app.getApplicationListener()));
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

        drawHealthBars();
    }

    private void drawHealthBars() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Render HP Player
        drawBar(player);

        // Render XP player
        drawXpBar(player);

        // Render HP Musuh dari pool
        for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
            drawBar(enemy);
        }

        shapeRenderer.end();
    }

    private void drawXpBar(GameCharacter character) {
        float x = character.getPosition().x;
        // XP Bar tepat di atas kepala (+2 pixel)
        float y = character.getPosition().y + character.getVisualHeight() + 2;

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
        // Jika karakter adalah Player (Ryze), HP bar harus lebih tinggi
        // karena ada XP bar di bawahnya.
        // XP bar mulai di +2 dengan tinggi 4 (total +6). Beri jarak 2px lagi -> +8.
        float offset = (character instanceof Ryze) ? 8 : 5;

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

    // Box dulu, diganti animasi ntar
    private void renderMeleeAttacks() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (MeleeAttack m : meleeAttacks) {
            if (m.isActive()) {
                m.render(shapeRenderer);
            }
        }
        shapeRenderer.end();
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
                    e.takeDamage(m.getDamage());
                    m.markAsHit(e);

                    if (e.isDead()) {
                        // Pool akan auto-free enemy yang mati
                        player.gainXp(e.getXpReward());
                        System.out.println("Enemy Killed by Melee!");
                    }
                }
            }
        }

        // Cek Musuh vs Player (collision damage)
        Rectangle playerBounds = player.getBounds();
        for (DummyEnemy e : activeEnemies) {
            if (e.getBounds().overlaps(playerBounds)) {
                if (e.canAttack()) {
                    player.takeDamage(10);
                    e.resetAttackTimer();
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

}
