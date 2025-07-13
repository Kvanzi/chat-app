package com.kvanzi.chatapp.auth.service;

import com.kvanzi.chatapp.auth.entity.RefreshToken;
import com.kvanzi.chatapp.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.access-expiration}")
    private int ACCESS_EXPIRATION;
    @Value("${jwt.refresh-expiration}")
    private int REFRESH_EXPIRATION;

    private final SecretKey secretKey;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtParser verifiedJwtParser;

    public JwtService(
            @Value("${jwt.secret-key}") Resource secretKeyResource,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.secretKey = getSecretKeyFromResource(secretKeyResource);
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.verifiedJwtParser = Jwts.parser()
                .verifyWith(getSigninKey())
                .build();
    }

    public String generateRefreshToken(String userId) {
        return generateRefreshToken(userId, Collections.emptyMap());
    }

    public String generateRefreshToken(String userId, Map<String, Object> extraClaims) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(REFRESH_EXPIRATION, ChronoUnit.SECONDS);

        RefreshToken tokenEntity = RefreshToken.builder()
                .userId(userId)
                .expiresAt(expiresAt)
                .issuedAt(issuedAt)
                .build();

        tokenEntity = refreshTokenRepository.save(tokenEntity);

        String token = generateToken(
                Date.from(issuedAt),
                Date.from(expiresAt),
                userId,
                extraClaims
        );

        tokenEntity.setTokenHash(passwordEncoder.encode(token));
        refreshTokenRepository.save(tokenEntity);

        return token;
    }

    public String generateAccessToken(String userId) {
        return generateAccessToken(userId, Collections.emptyMap());
    }

    public String generateAccessToken(String userId, Map<String, Object> extraClaims) {
        return generateToken(
                Date.from(Instant.now()),
                Date.from(Instant.now().plus(ACCESS_EXPIRATION, ChronoUnit.SECONDS)),
                userId,
                extraClaims
        );
    }

    public String generateToken(Date issuedAt, Date expiresAt, String subject, Map<String, Object> extraClaims) {
        return Jwts.builder()
                .subject(subject)
                .claims(extraClaims)
                .signWith(getSigninKey())
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .compact();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiredAt(token).isBefore(Instant.now());
    }

    public Instant extractExpiredAt(String token) {
        return extractClaim(token, Claims::getExpiration).toInstant();
    }

    public Instant extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt).toInstant();
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractClaims(String token) {
        return this.verifiedJwtParser
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigninKey() {
        return this.secretKey;
    }

    private SecretKey getSecretKeyFromResource(Resource resource) {
        try {
            String secretKey = Files.readString(resource.getFile().toPath()).trim();
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IOException e) {
            throw new RuntimeException("Critical error: can't load jwt secret key", e);
        }
    }
}
