package com.kelompok2.backend.controller;

import com.kelompok2.backend.model.Score;
import com.kelompok2.backend.repository.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*")
public class LeaderboardController {

    @Autowired
    private ScoreRepository scoreRepository;

    // GET dengan Filter
    // Contoh URL: /api/leaderboard?character=Ryze&sortBy=level
    @GetMapping
    public List<Score> getLeaderboard(
            @RequestParam(required = false, defaultValue = "All") String character,
            @RequestParam(required = false, defaultValue = "time") String sortBy) {

        // Logika Filter
        if (character.equals("All")) {
            // Jika memilih "Semua Karakter"
            if (sortBy.equals("level")) {
                return scoreRepository.findTop20ByOrderByLevelDescValueDesc();
            } else {
                return scoreRepository.findTop20ByOrderByValueDesc();
            }
        } else {
            // Jika memilih Karakter Spesifik (Misal: Ryze)
            if (sortBy.equals("level")) {
                return scoreRepository.findTop20ByCharacterOrderByLevelDescValueDesc(character);
            } else {
                return scoreRepository.findTop20ByCharacterOrderByValueDesc(character);
            }
        }
    }

    @GetMapping("/player")
    public List<Score> getPlayerHistory(@RequestParam String name) {
        return scoreRepository.findByPlayerNameOrderByValueDesc(name);
    }

    @PostMapping
    public Score submitScore(@RequestBody Score score) {
        return scoreRepository.save(score);
    }
}