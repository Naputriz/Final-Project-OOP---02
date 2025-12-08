package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kelompok2.frontend.entities.Ryze;
import com.kelompok2.frontend.utils.InputHandler;

public class GameScreen extends ScreenAdapter {
    private SpriteBatch batch;
    private Ryze player;
    private InputHandler inputHandler;
    private OrthographicCamera camera; // Kamera game
    private Texture background; // Background sementara biar kelihatan gerak

    @Override
    public void show() {
        batch = new SpriteBatch();

        // Sementara pake Ryze dulu

        //  Setup Kamera
        camera = new OrthographicCamera();
        // Set ukuran viewport kamera (seberapa luas dunia yang terlihat)
        camera.setToOrtho(false, 1280, 720);

        // Load background (optional, biar kelihatan gerak aja)
        background = new Texture(Gdx.files.internal("libgdx.png")); // Pake gambar logo libgdx dulu gpp buat lantai

        // Spawn Player
        player = new Ryze(0, 0);

        // Taruh player di tengah map
        player.setPosition(0, 0);

        // Setup Input Handler dengan Kamera
        inputHandler = new InputHandler(player, camera);
    }

    @Override
    public void render(float delta) {
        // UPDATE LOGIC
        inputHandler.update(delta);

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
        batch.end();
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
    }
}
