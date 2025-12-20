package com.kelompok2.backend.controller;

import com.kelompok2.backend.model.User;
import com.kelompok2.backend.repository.UserRepository; // Import repository yg baru dibuat
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
    private UserRepository userRepository; // Koneksi ke Database

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginData) {
        // Cari user di database Neon
        Optional<User> userOpt = userRepository.findByUsername(loginData.getUsername());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Cek password
            if (user.getPassword().equals(loginData.getPassword())) {
                return ResponseEntity.ok("Login Berhasil");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username atau Password salah");
    }

    // --- REGISTER ---
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User newUser) {
        // Cek apakah username sudah ada di Neon
        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username sudah digunakan");
        }

        // Simpan user baru ke Neon
        userRepository.save(newUser);
        return ResponseEntity.ok("Registrasi Berhasil");
    }
}