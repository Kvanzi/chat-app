package com.kvanzi.chatapp.auth.service;

import com.kvanzi.chatapp.auth.entity.RefreshToken;
import com.kvanzi.chatapp.auth.enumeration.TokenType;
import com.kvanzi.chatapp.auth.exception.RefreshAccessTokenException;
import com.kvanzi.chatapp.auth.repository.RefreshTokenRepository;
import com.kvanzi.chatapp.user.entity.User;
import com.kvanzi.chatapp.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.access-expiration-sec}")
    private int ACCESS_EXPIRATION;
    @Value("${jwt.refresh-expiration-sec}")
    private int REFRESH_EXPIRATION;
    public final static String BEARER_PREFIX = "Bearer ";

    private final SecretKey secretKey;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtParser verifiedJwtParser;


    public JwtService(
            @Value("${jwt.secret-key}") Resource secretKeyResource,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository
    ) {
        this.secretKey = getSecretKeyFromResource(secretKeyResource);
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.verifiedJwtParser = Jwts.parser()
                .verifyWith(getSigninKey())
                .build();
        this.userRepository = userRepository;
    }

    public String refreshAccessToken(HttpServletRequest request) {
        return refreshAccessToken(extractJWTToken(request));
    }

    public String refreshAccessToken(String token) {
        if (token == null) {
            throw new RefreshAccessTokenException("Refresh token cannot be null");
        }

        try {
            Claims claims = extractClaims(token);
            TokenType tokenType = extractTokenType(claims);
            String tokenId = extractTokenId(claims);
            String userId = extractUserId(claims);

            if (tokenType == null || tokenId == null || userId == null) {
                throw new RefreshAccessTokenException("Invalid refresh token");
            }

            if (extractExpiredAt(claims).isBefore(Instant.now())) {
                refreshTokenRepository.findById(tokenId).ifPresent(refreshTokenRepository::delete);
                throw new RefreshAccessTokenException("Refresh token expired");
            }

            if (tokenType != TokenType.REFRESH) {
                throw new RefreshAccessTokenException("Invalid token type. Expected REFRESH token for access token refresh");
            }

            RefreshToken refreshTokenEntity = refreshTokenRepository.findById(tokenId)
                    .orElseThrow(() -> new RefreshAccessTokenException("Invalid refresh token"));

            if (refreshTokenEntity.getIsRevoked()) {
                throw new RefreshAccessTokenException("Invalid refresh token");
            }

            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                refreshTokenRepository.delete(refreshTokenEntity);
                throw new RefreshAccessTokenException("Invalid refresh token");
            }

            User user = userOpt.get();

            if (user.getLastPasswordChangedAtOpt().isPresent()
                    && user.getLastPasswordChangedAt().isAfter(refreshTokenEntity.getIssuedAt())) {
                throw new RefreshAccessTokenException("Invalid refresh token. Re login please");
            }

            return generateAccessToken(userId);
        } catch (ExpiredJwtException e) {
            throw new RefreshAccessTokenException("Refresh token expired");
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            throw new RefreshAccessTokenException("Invalid token. Re login please");
        } catch (JwtException e) {
            throw new RefreshAccessTokenException("Unexpected token validation exception. Re login please");
        }
    }

    public String generateRefreshToken(String userId) {
        return generateRefreshToken(userId, new HashMap<>());
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

        extraClaims.put("token_type", TokenType.REFRESH);
        String token = generateToken(
                Date.from(issuedAt),
                Date.from(expiresAt),
                userId,
                extraClaims,
                tokenEntity.getId()
        );

        tokenEntity.setTokenHash(passwordEncoder.encode(token));
        refreshTokenRepository.save(tokenEntity);

        return token;
    }

    public String generateAccessToken(String userId) {
        return generateAccessToken(userId, new HashMap<>());
    }

    public String generateAccessToken(String userId, Map<String, Object> extraClaims) {
        extraClaims.put("token_type", TokenType.ACCESS);
        return generateToken(
                Date.from(Instant.now()),
                Date.from(Instant.now().plus(ACCESS_EXPIRATION, ChronoUnit.SECONDS)),
                userId,
                extraClaims,
                null
        );
    }

    public String generateToken(Date issuedAt, Date expiresAt, String subject, Map<String, Object> extraClaims, String id) {
        return Jwts.builder()
                .id(id)
                .subject(subject)
                .claims(extraClaims)
                .signWith(getSigninKey())
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .compact();
    }

    public String extractTokenId(String token) {
        Claims claims = extractClaims(token);
        return extractClaim(claims, Claims::getId);
    }

    public String extractTokenId(Claims claims) {
        return extractClaim(claims, Claims::getId);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiredAt(token).isBefore(Instant.now());
    }

    public boolean isTokenExpired(Claims claims) {
        return extractClaim(claims, Claims::getExpiration).toInstant().isBefore(Instant.now());
    }

    public TokenType extractTokenType(String token) {
        Claims claims = extractClaims(token);
        return TokenType.valueOf(claims.get("token_type", String.class));
    }

    public TokenType extractTokenType(Claims claims) {
        return TokenType.valueOf(claims.get("token_type", String.class));
    }

    public Instant extractExpiredAt(String token) {
        return extractClaim(token, Claims::getExpiration).toInstant();
    }

    public Instant extractExpiredAt(Claims claims) {
        return extractClaim(claims, Claims::getExpiration).toInstant();
    }

    public Instant extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt).toInstant();
    }

    public Instant extractIssuedAt(Claims claims) {
        return extractClaim(claims, Claims::getIssuedAt).toInstant();
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(Claims claims) {
        return extractClaim(claims, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }

    public <T> T extractClaim(Claims claims, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(claims);
    }

    public Claims extractClaims(String token) throws JwtException {
        return this.verifiedJwtParser
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractJWTToken(HttpServletRequest request) {
        if (request.getRequestURI().equals("/ws")) {
            String authParameter = request.getParameter(HttpHeaders.AUTHORIZATION);
            if (authParameter != null && !authParameter.isBlank()) {
                return authParameter;
            }
            return null;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Auth header is null or it's not start from '{}'", BEARER_PREFIX);
            return null;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty()) {
            log.debug("Token empty");
            return null;
        }

        return token;
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
