package com.kvanzi.chatapp.ws.controller;

import com.kvanzi.chatapp.user.enumeration.UserStatus;
import com.kvanzi.chatapp.ws.dto.ChangeStatusDTO;
import com.kvanzi.chatapp.ws.service.UserStatusService;
import com.kvanzi.chatapp.ws.utils.WebSocketAuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketUserController {

    private final UserStatusService userStatusService;

    @MessageMapping("/profile.setStatus")
    public void changeUserStatus(
            @Payload ChangeStatusDTO dto,
            StompHeaderAccessor accessor
    ) {
        String userId = WebSocketAuthUtils.extractSimpUserId(accessor);
        UserStatus currentStatus = userStatusService.getUserStatus(userId);
        UserStatus requestedStatus = dto.getStatus();
        if (currentStatus != requestedStatus) {
            userStatusService.setUserStatus(userId, requestedStatus);
        }
    }
}
