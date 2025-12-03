package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kelompok2.frontend.entities.Ryze;
import com.kelompok2.frontend.utils.InputHandler;

public class GameScreen extends ScreenAdapter {
    private SpriteBatch batch;
    private Ryze player;
    private InputHandler inputHandler;

    @Override
    public void show() {
        batch = new SpriteBatch();

        //Sementara pake Ryze dulu

        // Spawn Ryze di tengah layar
        player = new Ryze(0, 0);
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;

        player.setPosition(centerX - (player.getWidth() / 2), centerY - (player.getHeight() / 2));

        // Sambungkan input ke Ryze
        inputHandler = new InputHandler(player);
    }

    @Override
    public void render(float delta) {
        // 1. UPDATE LOGIC
        inputHandler.update(delta);

        // 2. CLEAR SCREEN
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1); // Warna background abu gelap (sementara)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 3. DRAW
        batch.begin();
        player.render(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        player.dispose();
    }
}
