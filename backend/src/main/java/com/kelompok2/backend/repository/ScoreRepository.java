package com.kelompok2.backend.repository;

import com.kelompok2.backend.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    // 1. Top 20 berdasarkan Waktu (Terlama) - Default
    List<Score> findTop20ByOrderByValueDesc();

    // 2. Top 20 berdasarkan Level (Tertinggi), lalu Waktu
    List<Score> findTop20ByOrderByLevelDescValueDesc();

    // 3. Top 20 Filter Karakter + Sort Waktu
    List<Score> findTop20ByCharacterOrderByValueDesc(String character);

    // 4. Top 20 Filter Karakter + Sort Level
    List<Score> findTop20ByCharacterOrderByLevelDescValueDesc(String character);

    List<Score> findByPlayerNameOrderByValueDesc(String playerName);
}