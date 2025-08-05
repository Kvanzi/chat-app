package com.kvanzi.chatapp.ws.service;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WebSocketSessionManager {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class SessionInfo {
        private String userId;
        private Long lastExtendedAt;
    }

    private final RedisTemplate<String, Object> redisTemplate;
    private final static Map<String, WebSocketSession> localSessions = new ConcurrentHashMap<>();
    private final long SESSION_DURATION_SECONDS;

    private static final String USER_SESSIONS_KEY_PREFIX = "websocket:sessions:user:";
    private static final String SESSIONS_INFOS_KEY = "websocket:sessions:infos";

    public WebSocketSessionManager(RedisTemplate<String, Object> redisTemplate, @Value("${jwt.access-expiration-sec}") int sessionDurationSeconds) {
        this.redisTemplate = redisTemplate;
        this.SESSION_DURATION_SECONDS = sessionDurationSeconds;
    }

    public void addSession(WebSocketSession session, String userId) {
        if (userId == null || session == null || !session.isOpen()) {
            return;
        }

        String sessionId = session.getId();

        localSessions.put(sessionId, session);
        addSessionToRedis(sessionId, userId);
        extendSession(sessionId, userId);
    }

    public void closeSession(String sessionId, String userId) {
        WebSocketSession storedSession = localSessions.get(sessionId);

        if (storedSession != null && storedSession.isOpen()) {
            try {
                storedSession.close();
            } catch (IOException e) {
                log.error("Error occurred while trying to close session with id '{}'", sessionId, e);
            }
        }

        localSessions.remove(sessionId);
        removeSessionFromRedis(sessionId, userId);
        deleteSessionInfo(sessionId);
    }

    public int getUserSessionsCount(String userId) {
        return Objects.requireNonNullElse(
                redisTemplate
                        .opsForSet()
                        .members(USER_SESSIONS_KEY_PREFIX + userId),
                Collections.emptySet()
        ).size();
    }

    @Scheduled(fixedRate = 150_000) // every 2.5 mins
    public void cleanUpExpiredSessions() {
        AtomicInteger totalClosed = new AtomicInteger();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(SESSIONS_INFOS_KEY);

        long expirationThreshold = SESSION_DURATION_SECONDS * 1000;
        entries.forEach((key, value) -> {
            String sessionId = (String) key;
            SessionInfo info = (SessionInfo) value;

            Long now = System.currentTimeMillis();
            Long lastExtendedAt = info.getLastExtendedAt();

            if (now - lastExtendedAt > expirationThreshold) {
                closeSession(sessionId, info.getUserId());
                totalClosed.getAndIncrement();
            }
        });

        if (totalClosed.get() != 0) {
            log.debug("Total closed {} sessions", totalClosed);
        }
    }

    public int getTotalUsersOnline() {
        int total = 0;

        ScanOptions options = ScanOptions.scanOptions()
                .match(USER_SESSIONS_KEY_PREFIX + "*")
                .count(500)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                cursor.next();
                total++;
            }
        }

        return total;
    }

    public void extendSession(String sessionId, String userId) {
        SessionInfo sessionInfo = SessionInfo.builder()
                .userId(userId)
                .lastExtendedAt(System.currentTimeMillis())
                .build();
        redisTemplate.opsForHash().put(SESSIONS_INFOS_KEY, sessionId, sessionInfo);
    }

    private void deleteSessionInfo(String sessionId) {
        redisTemplate.opsForHash().delete(SESSIONS_INFOS_KEY, sessionId);
    }

    private void addSessionToRedis(String sessionId, String userId) {
        redisTemplate.opsForSet().add(USER_SESSIONS_KEY_PREFIX + userId, sessionId);
    }

    private void removeSessionFromRedis(String sessionId, String userId) {
        redisTemplate.opsForSet().remove(USER_SESSIONS_KEY_PREFIX + userId, sessionId);
    }

    private Set<WebSocketSession> getUserSessions(String userId) {
        return Objects.requireNonNullElse(
                        redisTemplate
                                .opsForSet()
                                .members(USER_SESSIONS_KEY_PREFIX + userId),
                        Collections.emptySet()
                )
                .stream()
                .map(sessionId -> localSessions.get((String) sessionId))
                .collect(Collectors.toSet());
    }
}