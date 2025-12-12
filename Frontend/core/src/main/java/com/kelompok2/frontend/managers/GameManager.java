package com.kelompok2.frontend.managers;

public class GameManager {
    private static GameManager instance;

    // State game yang dilacak
    private int currentLevel; // Level player
    private float gameTime; // Total waktu bermain (dalam detik)
    private String currentCharacterName; // Nama karakter yang dipilih
    private boolean isGameOver; // Flag untuk status game over

    private GameManager() {
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
    public boolean isGameOver() {
        return isGameOver;
    }
}
