package com.kvanzi.chatapp.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserStatusService {

//    private final RedisTemplate<String, Object> redisTemplate;
//    private final SimpMessagingTemplate messagingTemplate;

    private static final String USER_SESSION_PREFIX = "user:session:";
    private static final String ONLINE_USERS_SET = "online:users";
    private static final int SESSION_TIMEOUT = 30;


}
