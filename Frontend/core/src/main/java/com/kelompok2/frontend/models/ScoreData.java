package com.kelompok2.frontend.models;

public class ScoreData {
    public String playerName;
    public String character;
    public int level;
    public int value; // Waktu dalam detik

    public ScoreData(String playerName, String character, int level, int value) {
        this.playerName = playerName;
        this.character = character;
        this.level = level;
        this.value = value;
    }
}
