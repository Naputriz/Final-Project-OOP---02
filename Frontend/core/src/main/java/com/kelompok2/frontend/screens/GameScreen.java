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
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.entities.Ryze;
import com.kelompok2.frontend.utils.InputHandler;

import java.util.Iterator;

public class GameScreen extends ScreenAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Ryze player;
    private InputHandler inputHandler;
    private OrthographicCamera camera; // Kamera game
    private Texture background; // Background sementara biar kelihatan gerak

    private Array<Projectile> projectiles; // List untuk menampung semua peluru yang sedang terbang
    private Array<DummyEnemy> enemies;

    private float spawnTimer = 0; // Timer buat spawn musuh tiap detik

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        projectiles = new Array<>();
        enemies = new Array<>();

        // Sementara pake Ryze dulu

        //  Setup Kamera
        camera = new OrthographicCamera();
        // Set ukuran viewport kamera (seberapa luas dunia yang terlihat)
        camera.setToOrtho(false, 1280, 720);

        // Load background (optional, biar kelihatan gerak aja)
        background = new Texture(Gdx.files.internal("FireflyPlaceholder.jpg")); // Pake gambar logo libgdx dulu gpp buat lantai

        // Spawn Player
        player = new Ryze(0, 0);

        // Taruh player di tengah map
        player.setPosition(0, 0);

        // Setup Input Handler dengan Kamera
        inputHandler = new InputHandler(player, camera, projectiles);
    }

    @Override
    public void render(float delta) {
        // UPDATE LOGIC
        inputHandler.update(delta);
        updateProjectiles(delta);
        updateEnemies(delta);
        checkCollisions(delta); // Cek tabrakan
        spawnEnemies(delta); // Spawn musuh baru

        // Cek Game Over
        if (player.isDead()) {
            System.out.println("Game over lmao skill issue. Restarting...");
            // Restart screen sederhana
            ((Main)Gdx.app.getApplicationListener()).setScreen(new GameScreen());
            return; // Stop render frame ini
        }


        // Update posisi kamera agar selalu mengikuti player
        camera.position.set(
            player.getPosition().x + player.getWidth() / 2,
            player.getPosition().y + player.getHeight() / 2,
            0
        );
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
        int startX = (int)(camera.position.x - camera.viewportWidth/2) / (int)bgSize - 1;
        int startY = (int)(camera.position.y - camera.viewportHeight/2) / (int)bgSize - 1;
        int endX = (int)(camera.position.x + camera.viewportWidth/2) / (int)bgSize + 1;
        int endY = (int)(camera.position.y + camera.viewportHeight/2) / (int)bgSize + 1;

        for(int x = startX; x <= endX; x++) {
            for(int y = startY; y <= endY; y++) {
                batch.draw(background, x * bgSize, y * bgSize, bgSize, bgSize);
            }
        }
        player.render(batch);
        // Render Projectiles
        for (Projectile p : projectiles) {
            p.render(batch);
        }

        for (DummyEnemy enemy : enemies) {
            enemy.render(batch);
        }

        batch.end();
        drawHealthBars();
    }

    private void drawHealthBars() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Render HP Player
        drawBar(player);

        // Render XP player
        drawXpBar(player);

        // Render HP Musuh
        for (DummyEnemy enemy : enemies) {
            drawBar(enemy);
        }

        shapeRenderer.end();
    }

    private void drawXpBar(GameCharacter character) {
        float x = character.getPosition().x;
        // XP Bar tepat di atas kepala (+2 pixel)
        float y = character.getPosition().y + character.getHeight() + 2;

        float width = character.getWidth();
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

        float y = character.getPosition().y + character.getHeight() + offset;
        // ------------------------

        float width = character.getWidth();
        float height = 5;

        float hpPercent = character.getHp() / character.getMaxHp();

        // Background Merah
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, y, width, height);

        // Foreground Hijau
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(x, y, width * hpPercent, height);
    }

    private void updateProjectiles(float delta) {
        Iterator<Projectile> iter = projectiles.iterator();
        while(iter.hasNext()){
            Projectile p = iter.next();
            p.update(delta);
            if(!p.active){
                iter.remove();
            }
        }
    }

    private void updateEnemies(float delta) {
        for (DummyEnemy enemy : enemies) {
            enemy.update(delta);
        }
    }

    private void spawnEnemies(float delta) {
        spawnTimer += delta;
        if (spawnTimer > 1.5f) { // Spawn tiap 2 detik
            // Spawn di posisi random sekitar player (radius 500-700 pixel)
            float angle = MathUtils.random(360);
            float distance = MathUtils.random(600, 800);
            float x = player.getPosition().x + MathUtils.cosDeg(angle) * distance;
            float y = player.getPosition().y + MathUtils.sinDeg(angle) * distance;

            enemies.add(new DummyEnemy(x, y, player));
            spawnTimer = 0;
        }
    }

    private void checkCollisions(float delta) {
        // Cek Peluru vs Musuh
        Iterator<Projectile> pIter = projectiles.iterator();
        while (pIter.hasNext()) {
            Projectile p = pIter.next();
            Rectangle pBounds = p.getBounds();

            Iterator<DummyEnemy> eIter = enemies.iterator();
            while (eIter.hasNext()) {
                DummyEnemy e = eIter.next();

                if (pBounds.overlaps(e.getBounds())) {
                    p.active = false; // Matikan peluru (akan dihapus di updateProjectiles)
                    e.takeDamage(25);
                    if (e.isDead()) {
                        eIter.remove();
                        player.gainXp(e.getXpReward());
                        System.out.println("Enemy Killed!");
                    }
                    break;
                }
            }
        }

        // Cek Musuh vs Player
        Rectangle playerBounds = player.getBounds();
        for (DummyEnemy e : enemies) {
            if (e.getBounds().overlaps(playerBounds)) {
                player.takeDamage(50 * delta);
                // Nanti disini kurangi HP player (Untuk skrg blm, testing enemy dlu)
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
        if (background != null) background.dispose();
        // Dispose texture peluru jika ada
        for (Projectile p : projectiles) {
            p.dispose();
        }
        for (DummyEnemy e : enemies) e.dispose();
    }
}
