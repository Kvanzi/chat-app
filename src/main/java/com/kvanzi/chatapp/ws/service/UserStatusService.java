package com.kvanzi.chatapp.ws.service;

import com.kvanzi.chatapp.user.enumeration.UserStatus;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.kvanzi.chatapp.user.enumeration.UserStatus.OFFLINE;
import static com.kvanzi.chatapp.user.enumeration.UserStatus.ONLINE;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatusService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String USER_STATUS_PREFIX = "user:status:";
    private static final String ONLINE_USERS_KEY = "online:users";

    public void setUserStatus(String userId, UserStatus status) {
        switch (status) {
            case ONLINE -> setUserOnline(userId);
            case OFFLINE -> setUserOffline(userId);
        }
    }

    private void setUserOnline(String userId) {
        String userKey = USER_STATUS_PREFIX + userId;

        redisTemplate.opsForValue().set(userKey, ONLINE);
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);
        broadcastUserStatus(userId, ONLINE);
    }

    private void setUserOffline(String userId) {
        String userKey = USER_STATUS_PREFIX + userId;

        redisTemplate.delete(userKey);
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);
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
        String statusStr = (String) redisTemplate.opsForValue().get(USER_STATUS_PREFIX + userId);
        return statusStr != null ? UserStatus.valueOf(statusStr) : OFFLINE;
    }

    public Set<String> getOnlineUsers() {
        return Objects.requireNonNull(redisTemplate.opsForSet().members(ONLINE_USERS_KEY))
                .stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    public int getOnlineUsersCount() {
        return getOnlineUsers().size();
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
