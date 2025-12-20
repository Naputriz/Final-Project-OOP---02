package com.kelompok2.frontend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.screens.GameScreen;
import com.kelompok2.frontend.screens.MainMenuScreen;
import com.kelompok2.frontend.screens.LoginScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    private SpriteBatch batch;
    private Texture image;
    private String playerName = "Player";

    @Override
    public void create() {
        this.setScreen(new LoginScreen(this)); // input username dulu
    }

    // Getter dan Setter untuk Player Name
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void dispose() {
        if(batch != null){
            batch.dispose();
        }
        AssetManager.getInstance().dispose();
        AudioManager.getInstance().dispose();
    }
}
