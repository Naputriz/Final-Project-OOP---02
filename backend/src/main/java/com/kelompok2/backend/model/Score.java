package com.kelompok2.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "scores")
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;

    private String character; // <--- 1. TAMBAHKAN INI

    private int level;
    private int value;

    public Score() {}

    // Update Constructor (Opsional, tapi bagus untuk kerapihan)
    public Score(String playerName, String character, int level, int value) {
        this.playerName = playerName;
        this.character = character;
        this.level = level;
        this.value = value;
    }

    // --- 2. TAMBAHKAN GETTER & SETTER ---
    public String getCharacter() { return character; }
    public void setCharacter(String character) { this.character = character; }

    // ... Getter Setter yang lama (playerName, level, value) biarkan saja ...
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}