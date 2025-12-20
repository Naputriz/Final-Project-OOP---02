package com.kelompok2.backend.repository;

import com.kelompok2.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Ini Interface, bukan Class biasa
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}