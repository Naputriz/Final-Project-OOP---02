package com.kelompok2.frontend;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.screens.MainMenuScreen;

public class Main extends Game {
    private SpriteBatch batch;
    private String playerName = "Guest";
    private boolean isLoggedIn = false; // Status Login

    @Override
    public void create() {
        // Langsung ke Main Menu, logika login ada di dalam Main Menu
        this.setScreen(new MainMenuScreen(this));
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void dispose() {
        if(batch != null) batch.dispose();
        AssetManager.getInstance().dispose();
        AudioManager.getInstance().dispose();
    }
}
