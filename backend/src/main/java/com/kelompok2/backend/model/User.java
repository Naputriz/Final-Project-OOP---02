package com.kelompok2.backend.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity // Menandakan ini tabel database
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false) // Username tidak boleh kembar
    private String username;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_unlocked_characters", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "character_name")
    private Set<String> unlockedCharacters = new HashSet<>();

    public User() {
        // default char
        this.unlockedCharacters.add("Ryze"); // default char
        this.unlockedCharacters.add("Whisperwind");
        this.unlockedCharacters.add("Aelita");
        this.unlockedCharacters.add("Aegis");
        this.unlockedCharacters.add("Lumi");
        this.unlockedCharacters.add("Alice");
        this.unlockedCharacters.add("Kei");
        // Karakter Boss (Blaze, Insania, Isolde) TIDAK ditambahkan di sini
        // karena mereka harus di-unlock lewat gameplay.
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<String> getUnlockedCharacters() {
        return unlockedCharacters;
    }

    public void setUnlockedCharacters(Set<String> unlockedCharacters) {
        this.unlockedCharacters = unlockedCharacters;
    }

    public void addUnlockedCharacter(String characterName) {
        this.unlockedCharacters.add(characterName);
    }
}