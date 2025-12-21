package com.kelompok2.backend.controller;

import com.kelompok2.backend.model.User;
import com.kelompok2.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // [BARU] Endpoint Update Keybinds
    @PostMapping("/update-keys")
    public ResponseEntity<?> updateKeys(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String keyConfig = payload.get("keyConfig");

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setKeyConfig(keyConfig); // Update JSON config
            userRepository.save(user);
            return ResponseEntity.ok().body("Keybinds updated successfully");
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    // [FITUR LAMA] Unlock Character
    @PostMapping("/unlock")
    public ResponseEntity<?> unlockCharacter(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String characterName = payload.get("characterName");

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.addUnlockedCharacter(characterName);
            userRepository.save(user);
            return ResponseEntity.ok().body("Character unlocked: " + characterName);
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    // [FITUR LAMA] Get User Data
    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}