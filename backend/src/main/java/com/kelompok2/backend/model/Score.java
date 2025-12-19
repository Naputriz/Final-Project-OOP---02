package com.kelompok2.backend.model; // Sesuaikan package

public class Score {
    private String playerName;
    private int value;

    public Score() {}

    public Score(String playerName, int value) {
        this.playerName = playerName;
        this.value = value;
    }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}