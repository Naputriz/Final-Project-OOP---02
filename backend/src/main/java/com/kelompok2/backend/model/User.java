package com.kelompok2.backend.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // [BARU] Menyimpan konfigurasi tombol sebagai JSON String
    // Menggunakan TEXT agar muat menampung string JSON yang panjang
    @Column(columnDefinition = "TEXT")
    private String keyConfig;

    // [FITUR LAMA ANDA - TETAP DIPERTAHANKAN]
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_unlocked_characters", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "character_name")
    private Set<String> unlockedCharacters = new HashSet<>();

    public User() {
        // [FITUR LAMA] Default characters
        this.unlockedCharacters.add("Ryze");
        this.unlockedCharacters.add("Whisperwind");
        this.unlockedCharacters.add("Aelita");
        this.unlockedCharacters.add("Aegis");
        this.unlockedCharacters.add("Lumi");
        this.unlockedCharacters.add("Alice");
        this.unlockedCharacters.add("Kei");

        this.keyConfig = ""; // Default config kosong
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        // Inisialisasi default characters juga di constructor ini agar user baru punya karakter
        this.unlockedCharacters.add("Ryze");
        this.unlockedCharacters.add("Whisperwind");
        this.unlockedCharacters.add("Aelita");
        this.unlockedCharacters.add("Aegis");
        this.unlockedCharacters.add("Lumi");
        this.unlockedCharacters.add("Alice");
        this.unlockedCharacters.add("Kei");

        this.keyConfig = ""; // Default config kosong
    }

    // --- GETTER & SETTER BARU (KEYCONFIG) ---
    public String getKeyConfig() { return keyConfig; }
    public void setKeyConfig(String keyConfig) { this.keyConfig = keyConfig; }

    // --- GETTER & SETTER LAMA ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<String> getUnlockedCharacters() { return unlockedCharacters; }
    public void setUnlockedCharacters(Set<String> unlockedCharacters) { this.unlockedCharacters = unlockedCharacters; }

    public void addUnlockedCharacter(String characterName) {
        this.unlockedCharacters.add(characterName);
    }
}