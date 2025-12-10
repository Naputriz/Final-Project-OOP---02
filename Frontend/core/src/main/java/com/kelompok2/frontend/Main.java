package com.kelompok2.frontend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kelompok2.frontend.screens.GameScreen;
import com.kelompok2.frontend.screens.MainMenuScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() {
        setScreen(new MainMenuScreen(this)); // Langsung masuk ke game screen
    }

    public void dispose() {
        super.dispose();
        if (batch != null) batch.dispose();
    }
}
