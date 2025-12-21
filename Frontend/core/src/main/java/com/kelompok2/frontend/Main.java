package com.kelompok2.frontend;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.models.ScoreData;
import com.kelompok2.frontend.screens.MainMenuScreen;
import com.kelompok2.frontend.utils.KeyBindings; // [BARU]

public class Main extends Game {
    private SpriteBatch batch;
    private String playerName = "Guest";
    private boolean isLoggedIn = false;

    // History sementara untuk guest
    private Array<ScoreData> guestHistory = new Array<>();

    // [BARU] Simpan konfigurasi tombol di sini
    private KeyBindings keyBindings = new KeyBindings();

    @Override
    public void create() {
        this.setScreen(new MainMenuScreen(this));
    }

    // [BARU] Getter KeyBindings
    public KeyBindings getKeys() {
        return keyBindings;
    }

    // [BARU] Load dari JSON String (Dipanggil saat Login sukses)
    public void loadKeyBindings(String jsonString) {
        if (jsonString != null && !jsonString.isEmpty() && !jsonString.equals("null")) {
            try {
                Json json = new Json();
                this.keyBindings = json.fromJson(KeyBindings.class, jsonString);
                System.out.println("Keybindings loaded from server.");
            } catch (Exception e) {
                Gdx.app.error("Main", "Gagal load keybind, reset ke default", e);
                this.keyBindings.resetToDefault();
            }
        } else {
            this.keyBindings.resetToDefault();
        }
    }

    // [BARU] Simpan ke Server (Dipanggil saat tombol Save di Settings ditekan)
    public void saveKeyBindingsToServer() {
        if (playerName.equals("Guest")) return; // Guest tidak simpan

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl("http://localhost:8080/api/user/update-keys");
        request.setHeader("Content-Type", "application/json");

        Json json = new Json();
        // Serialisasi object KeyBindings ke String JSON dan escape quote
        String configJson = json.toJson(keyBindings).replace("\"", "\\\"");

        String payload = "{ \"username\": \"" + playerName + "\", \"keyConfig\": \"" + configJson + "\" }";
        request.setContent(payload);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override public void handleHttpResponse(Net.HttpResponse httpResponse) {
                System.out.println("Keybinds saved to server!");
            }
            @Override public void failed(Throwable t) {
                System.out.println("Failed to save keybinds to server.");
            }
            @Override public void cancelled() { }
        });
    }

    public boolean isLoggedIn() { return isLoggedIn; }
    public void setLoggedIn(boolean loggedIn) { isLoggedIn = loggedIn; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public void addGuestScore(String character, int level, int time) {
        guestHistory.insert(0, new ScoreData("Guest", character, level, time));
    }
    public Array<ScoreData> getGuestHistory() { return guestHistory; }
    public void clearGuestHistory() { guestHistory.clear(); }

    @Override
    public void dispose() {
        if(batch != null) batch.dispose();
        AssetManager.getInstance().dispose();
        AudioManager.getInstance().dispose();
    }
}
