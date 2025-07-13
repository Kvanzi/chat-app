package com.kvanzi.chatapp.auth.service;

import com.kvanzi.chatapp.auth.entity.RefreshToken;
import com.kvanzi.chatapp.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public Optional<RefreshToken> findById(String id) {
        return refreshTokenRepository.findById(id);
    }
}
