package com.kelompok2.backend.controller;

import com.kelompok2.backend.model.User;
import com.kelompok2.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginData) {
        Optional<User> userOpt = userRepository.findByUsername(loginData.getUsername());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(loginData.getPassword())) {
                // [PENTING] Mengembalikan Object User lengkap (JSON)
                // Ini memungkinkan frontend membaca keyConfig & unlockedCharacters
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username atau Password salah");
    }

    // --- REGISTER ---
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User newUser) {
        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username sudah digunakan");
        }
        userRepository.save(newUser);
        return ResponseEntity.ok("Registrasi Berhasil");
    }
}