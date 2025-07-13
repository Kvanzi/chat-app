package com.kvanzi.chatapp.auth.repository;

import com.kvanzi.chatapp.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    void deleteByExpiresAtBefore(Instant expiresAtBefore);
}
