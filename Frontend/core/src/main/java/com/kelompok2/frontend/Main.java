package com.kelompok2.frontend;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array; // [BARU] Import Array LibGDX
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.models.ScoreData; // [BARU] Import model tadi
import com.kelompok2.frontend.screens.MainMenuScreen;

public class Main extends Game {
    private SpriteBatch batch;
    private String playerName = "Guest";
    private boolean isLoggedIn = false;

    // [BARU] Penyimpanan sementara skor tamu (hilang saat aplikasi ditutup)
    private Array<ScoreData> guestHistory = new Array<>();

    @Override
    public void create() {
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

    // [BARU] Method untuk menambah skor tamu
    public void addGuestScore(String character, int level, int time) {
        // Insert di index 0 agar skor terbaru muncul paling atas
        guestHistory.insert(0, new ScoreData("Guest", character, level, time));
    }

    // [BARU] Getter history tamu
    public Array<ScoreData> getGuestHistory() {
        return guestHistory;
    }

    // [BARU] Reset history tamu (bisa dipanggil saat ganti akun jika perlu)
    public void clearGuestHistory() {
        guestHistory.clear();
    }

    @Override
    public void dispose() {
        if(batch != null) batch.dispose();
        AssetManager.getInstance().dispose();
        AudioManager.getInstance().dispose();
    }
}
