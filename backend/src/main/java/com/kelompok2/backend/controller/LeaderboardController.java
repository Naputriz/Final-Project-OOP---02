package com.kelompok2.backend.controller; // Sesuaikan package

import com.kelompok2.backend.model.Score; // Import Score yang dibuat diatas
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*") // PENTING: Agar game bisa akses (CORS)
public class LeaderboardController {

    private List<Score> scores = new ArrayList<>();

    public LeaderboardController() {
        // Data dummy awal agar leaderboard tidak kosong saat dites
        scores.add(new Score("Player1", 100));
        scores.add(new Score("Player2", 250));
    }

    @GetMapping
    public List<Score> getLeaderboard() {
        // Urutkan dari value terbesar (waktu terlama) ke terkecil
        return scores.stream()
                .sorted(Comparator.comparingInt(Score::getValue).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    @PostMapping
    public Score submitScore(@RequestBody Score score) {
        scores.add(score);
        return score;
    }
}