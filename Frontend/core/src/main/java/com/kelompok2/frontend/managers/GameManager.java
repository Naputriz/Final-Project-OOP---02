package com.kelompok2.frontend.managers;

import java.util.HashSet;
import java.util.Set;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.Preferences;

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
        // Default to Guest or generic load if no user yet (though usually empty until
        // login)
        loadUnlocks("Guest");

        System.out.println("[GameManager] Instance created (Singleton)");
    }

    private void loadUnlocks(String username) {
        if (username == null || username.isEmpty())
            username = "Guest";

        unlockedCharacters.clear();
        Preferences prefs = Gdx.app.getPreferences("MaestraTrialsSave");
        String key = "unlockedCharacters_" + username;
        String unlocksString = prefs.getString(key, "");

        if (!unlocksString.isEmpty()) {
            String[] chars = unlocksString.split(",");
            for (String c : chars) {
                if (!c.trim().isEmpty()) {
                    unlockedCharacters.add(c.trim());
                }
            }
        }

        // Ensure defaults are always present
        addDefaultUnlocks();

        // If it was empty (new user/save), save the defaults immediately to disk
        if (unlocksString.isEmpty()) {
            saveUnlocks();
        }
    }

    private void addDefaultUnlocks() {
        boolean changed = false;
        if (unlockedCharacters.add("Ryze"))
            changed = true;
        // Isolde is a boss, locked by default
        // Insania is a boss, locked by default
        // Blaze is a boss, locked by default
        if (unlockedCharacters.add("Whisperwind"))
            changed = true;
        if (unlockedCharacters.add("Aelita"))
            changed = true;
        if (unlockedCharacters.add("Aegis"))
            changed = true;
        if (unlockedCharacters.add("Lumi"))
            changed = true;
        if (unlockedCharacters.add("Alice"))
            changed = true;
        if (unlockedCharacters.add("Kei"))
            changed = true;

        // Note: We don't call saveUnlocks() here to avoid recursion loop or unnecessary
        // writes during tight loops,
        // but 'loadUnlocks' handles the initial save check.
    }

    private void saveUnlocks() {
        String username = currentUsername != null ? currentUsername : "Guest";
        Preferences prefs = Gdx.app.getPreferences("MaestraTrialsSave");
        StringBuilder sb = new StringBuilder();
        for (String c : unlockedCharacters) {
            sb.append(c).append(",");
        }
        prefs.putString("unlockedCharacters_" + username, sb.toString());
        prefs.flush();
        System.out.println("[GameManager] Saved unlocks for [" + username + "]: " + sb.toString());
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

        // 1. Load Local Unlocks for this user first
        loadUnlocks(username);

        // 2. If server provided data, merge it (Server is authority + Local progress)
        // Usually server > local, but if playing offline we might have new local
        // unlocks.
        // For now, we simply ADD server unlocks to local.
        if (serverUnlockedChars != null && !serverUnlockedChars.isEmpty()) {
            boolean changed = false;
            for (String charName : serverUnlockedChars) {
                if (unlockedCharacters.add(charName)) {
                    changed = true;
                }
            }
            if (changed) {
                saveUnlocks(); // Sync back to local disk
            }
        }

        System.out.println("[GameManager] User logged in: " + username);
        System.out.println("[GameManager] Final Unlocked chars: " + unlockedCharacters);
    }

    public void resetProgress() {
        String username = currentUsername != null ? currentUsername : "Guest";
        Preferences prefs = Gdx.app.getPreferences("MaestraTrialsSave");
        // Clear specific user key
        prefs.remove("unlockedCharacters_" + username);
        prefs.flush();

        System.out.println("[GameManager] Progress reset for: " + username);

        // Reload defaults
        loadUnlocks(username);
    }

    public boolean isCharacterUnlocked(String charName) {
        return unlockedCharacters.contains(charName);
    }

    public void unlockCharacter(String charName) {
        if (!unlockedCharacters.contains(charName)) {
            unlockedCharacters.add(charName);
            saveUnlocks(); // Persistence
            System.out.println("[GameManager] Character UNLOCKED locally: " + charName);
            // Panggil API Backend di sini atau via event listener terpisah
            syncUnlockToBackend(charName);
        }
    }

    private void syncUnlockToBackend(String charName) {
        if (currentUsername == null)
            return;

        // 1. Setup URL (Ganti localhost dengan IP server jika running di device
        // berbeda/Android)
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
