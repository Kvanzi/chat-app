package com.kvanzi.chatapp.chat.dto;

import com.kvanzi.chatapp.chat.enumeration.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatDTO {

    private String id;
    private ChatType type;
    private String lastMessageId;
    private Set<ChatMemberDTO> members;
}
