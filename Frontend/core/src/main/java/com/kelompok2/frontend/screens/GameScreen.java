package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.entities.Ryze;
import com.kelompok2.frontend.utils.InputHandler;

import java.util.Iterator;

public class GameScreen extends ScreenAdapter {
    private SpriteBatch batch;
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
        checkCollisions(); // Cek tabrakan
        spawnEnemies(delta); // Spawn musuh baru

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
        batch.begin();
        // Gambar background (tile sederhana agar kelihatan kita bergerak)
        // Kita gambar grid 10x10 logo libgdx sebagai lantai
        for(int x = -5; x < 5; x++) {
            for(int y = -5; y < 5; y++) {
                batch.draw(background, x * 500, y * 500, 500, 500);
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
        if (spawnTimer > 2f) { // Spawn tiap 2 detik
            // Spawn di posisi random sekitar player (radius 500-700 pixel)
            float angle = MathUtils.random(360);
            float distance = MathUtils.random(500, 700);
            float x = player.getPosition().x + MathUtils.cosDeg(angle) * distance;
            float y = player.getPosition().y + MathUtils.sinDeg(angle) * distance;

            enemies.add(new DummyEnemy(x, y, player));
            spawnTimer = 0;
        }
    }

    private void checkCollisions() {
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
                    eIter.remove();   // Hapus musuh (mati instan)
                    System.out.println("Enemy Killed!");
                    break; // Satu peluru cuma kena 1 musuh (biar gak nembus)
                }
            }
        }

        // Cek Musuh vs Player
        Rectangle playerBounds = player.getBounds();
        for (DummyEnemy e : enemies) {
            if (e.getBounds().overlaps(playerBounds)) {
                System.out.println("Player took damage!");
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
        player.dispose();
        if (background != null) background.dispose();
        // Dispose texture peluru jika ada
        for (Projectile p : projectiles) {
            p.dispose();
        }
    }
}
