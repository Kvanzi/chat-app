package com.kvanzi.chatapp.auth.service;

import com.kvanzi.chatapp.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void deleteExpiredTokens() {
        Instant now = Instant.now();
        refreshTokenRepository.deleteByExpiresAtBefore(now);
    }
}
