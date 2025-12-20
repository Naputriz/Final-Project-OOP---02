package com.kelompok2.backend.model;

import jakarta.persistence.*;

@Entity // <--- WAJIB ADA
@Table(name = "scores") // <--- WAJIB ADA
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;
    private String character; // Pastikan ini ada (sesuai update terakhir)
    private int level;        // Pastikan ini ada
    private int value;        // Waktu

    public Score() {}

    public Score(String playerName, String character, int level, int value) {
        this.playerName = playerName;
        this.character = character;
        this.level = level;
        this.value = value;
    }

    // ... Getter dan Setter Wajib Ada untuk semua field di atas ...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getCharacter() { return character; }
    public void setCharacter(String character) { this.character = character; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}