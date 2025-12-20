package com.kelompok2.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "scores")
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;

    private int level; // <--- TAMBAHKAN INI

    private int value; // Ini adalah waktu dalam detik

    public Score() {}

    // Update Constructor
    public Score(String playerName, int level, int value) {
        this.playerName = playerName;
        this.level = level;
        this.value = value;
    }

    // Getter & Setter untuk Level
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    // ... Getter Setter lain (playerName, value, id) biarkan saja
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}