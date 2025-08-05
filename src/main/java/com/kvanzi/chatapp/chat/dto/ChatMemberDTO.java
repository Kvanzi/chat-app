package com.kvanzi.chatapp.chat.dto;

import com.kvanzi.chatapp.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMemberDTO {

    private Instant updatedAt;
    private Instant createdAt;
    private Integer unreadCount = 0;
    private String lastReadMessageId;
    private UserDTO user;
}