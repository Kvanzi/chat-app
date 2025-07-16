package com.kvanzi.chatapp.user.service;

import com.kvanzi.chatapp.user.enumeration.UserStatus;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kvanzi.chatapp.user.enumeration.UserStatus.OFFLINE;
import static com.kvanzi.chatapp.user.enumeration.UserStatus.ONLINE;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatusService {

    // FIXME: ADD OPPORTUNITY TO CREATE MULTIPLY SESSIONS FOR 1 USER

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String USER_SESSION_PREFIX = "user:session";
    private static final String ONLINE_USERS_SET_KEY = "online:users";
    private static final int SESSION_TIMEOUT = 30; // 30sec

    public void userConnected(String userId, String sessionId) {
        String userKey = USER_SESSION_PREFIX + userId;

        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .lastSessionExtend(System.currentTimeMillis())
                .build();

        redisTemplate.opsForValue().set(userKey, session, Duration.ofSeconds(SESSION_TIMEOUT));
        redisTemplate.opsForSet().add(ONLINE_USERS_SET_KEY, userId);

        log.debug("User with id '{}' is online now", userId);
        broadcastUserStatus(userId, ONLINE);
    }

    public void userDisconnected(String userId, String sessionId) {
        String userKey = USER_SESSION_PREFIX + userId;

        redisTemplate.delete(userKey);
        redisTemplate.opsForSet().remove(ONLINE_USERS_SET_KEY, userId);

        log.debug("User with id '{}' is offline now", userId);
        broadcastUserStatus(userId, OFFLINE);
    }

    private void broadcastUserStatus(String userId, UserStatus status) {
        UserStatusEvent event = UserStatusEvent.builder()
                .userId(userId)
                .status(status)
                .build();

        messagingTemplate.convertAndSend("/topic/user-status/" + userId, event);
    }

    public UserStatus getUserStatus(String userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_SET_KEY, userId))
                ? ONLINE
                : OFFLINE;
    }

    public Set<String> getOnlineUsers() {
        return Objects.requireNonNull(redisTemplate.opsForSet().members(ONLINE_USERS_SET_KEY))
                .stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    //FIXME: ADD SCHEDULE SESSION CLEANUP

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class UserSession {
        private String sessionId;
        private long lastSessionExtend;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class UserStatusEvent {
        private String userId;
        private UserStatus status;
    }
}
