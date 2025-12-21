package com.kelompok2.frontend.managers;

import java.util.HashSet;
import java.util.Set;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import java.util.HashMap;
import java.util.Map;

public class GameManager {
    private static GameManager instance;

    // State game yang dilacak
    private int currentLevel; // Level player
    private float gameTime; // Total waktu bermain (dalam detik)
    private String currentCharacterName; // Nama karakter yang dipilih
    private boolean isGameOver; // Flag untuk status game over
    private String currentUsername; // Menyimpan siapa yang login
    private Set<String> unlockedCharacters; // Cache lokal karakter yang terbuka

    private GameManager() {
        unlockedCharacters = new HashSet<>();
        // Default unlocked characters
        unlockedCharacters.add("Ryze");
        unlockedCharacters.add("Whisperwind");
        unlockedCharacters.add("Aelita");
        unlockedCharacters.add("Aegis");
        unlockedCharacters.add("Lumi");
        unlockedCharacters.add("Alice");
        unlockedCharacters.add("Kei");

        reset();
        System.out.println("[GameManager] Instance created (Singleton)");
    }

    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void startNewGame(String characterName) {
        reset();
        this.currentCharacterName = characterName;
        System.out.println("[GameManager] New game started with character: " + characterName);
    }

    public void incrementLevel() {
        currentLevel++;
        System.out.println("[GameManager] Level up! Current level: " + currentLevel);
    }

    public void updateGameTime(float delta) {
        gameTime += delta;
    }

    public void loginUser(String username, Set<String> serverUnlockedChars) {
        this.currentUsername = username;
        if (serverUnlockedChars != null && !serverUnlockedChars.isEmpty()) {
            this.unlockedCharacters = serverUnlockedChars;
        } else {
            this.unlockedCharacters.add("Ryze");
            this.unlockedCharacters.add("Whisperwind");
            this.unlockedCharacters.add("Aelita");
            this.unlockedCharacters.add("Aegis");
            this.unlockedCharacters.add("Lumi");
            this.unlockedCharacters.add("Alice");
            this.unlockedCharacters.add("Kei");
        }
        System.out.println("[GameManager] User logged in: " + username);
        System.out.println("[GameManager] Unlocked chars: " + unlockedCharacters);
    }

    public boolean isCharacterUnlocked(String charName) {
        return unlockedCharacters.contains(charName);
    }

    public void unlockCharacter(String charName) {
        if (!unlockedCharacters.contains(charName)) {
            unlockedCharacters.add(charName);
            System.out.println("[GameManager] Character UNLOCKED locally: " + charName);
            // Panggil API Backend di sini atau via event listener terpisah
            syncUnlockToBackend(charName);
        }
    }

    private void syncUnlockToBackend(String charName) {
        if (currentUsername == null) return;

        // 1. Setup URL (Ganti localhost dengan IP server jika running di device berbeda/Android)
        String url = "http://localhost:8080/api/user/unlock";

        // 2. Buat JSON Body
        Map<String, String> data = new HashMap<>();
        data.put("username", currentUsername);
        data.put("characterName", charName);

        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String requestBody = json.toJson(data);

        // 3. Konfigurasi Request POST
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(url);
        request.setHeader("Content-Type", "application/json");
        request.setContent(requestBody);
        request.setTimeOut(10000); // 10 detik timeout

        // 4. Kirim Request dan Handle Response
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                if (statusCode == 200) {
                    System.out.println("[Backend Sync] Success unlocking: " + charName);
                } else {
                    System.err.println("[Backend Sync] Server error: " + statusCode);
                }
            }

            @Override
            public void failed(Throwable t) {
                System.err.println("[Backend Sync] Connection failed: " + t.getMessage());
            }

            @Override
            public void cancelled() {
                // Request cancelled
            }
        });
    }

    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
        if (gameOver) {
            System.out.println("[GameManager] Game Over! Final level: " + currentLevel +
                    ", Time: " + String.format("%.1f", gameTime) + "s");
        }
    }

    public void reset() {
        this.currentLevel = 1;
        this.gameTime = 0f;
        this.currentCharacterName = "";
        this.isGameOver = false;
        System.out.println("[GameManager] State reset");
    }

    // Getter

    public int getCurrentLevel() {
        return currentLevel;
    }
    public float getGameTime() {
        return gameTime;
    }
    public String getCurrentCharacterName() {
        return currentCharacterName;
    }
    public String getCurrentUsername() {
        return currentUsername;
    }
    public boolean isGameOver() {
        return isGameOver;
    }
}
