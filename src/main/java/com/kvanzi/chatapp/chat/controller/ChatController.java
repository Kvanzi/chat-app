package com.kvanzi.chatapp.chat.controller;

import com.kvanzi.chatapp.chat.document.message.Message;
import com.kvanzi.chatapp.chat.dto.ChatDTO;
import com.kvanzi.chatapp.chat.dto.CreateDirectChatRequestDTO;
import com.kvanzi.chatapp.chat.dto.SendMessageDTO;
import com.kvanzi.chatapp.chat.entity.chat.Chat;
import com.kvanzi.chatapp.chat.mapper.ChatMapper;
import com.kvanzi.chatapp.chat.service.ChatService;
import com.kvanzi.chatapp.common.ResponseWrapper;
import com.kvanzi.chatapp.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatService chatService;
    private final ChatMapper chatMapper;

    @GetMapping
    public ResponseEntity<ResponseWrapper<Set<ChatDTO>>> getMyChats(@AuthenticationPrincipal User currentUser) {
        Set<Chat> chats = chatService.getAllChatsByUserId(currentUser.getId());

        return ResponseWrapper.okEntity(chats.stream()
                .map(chatMapper::toDTO)
                .collect(Collectors.toSet())
        );
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<ChatDTO>> createNewDirectChat(
            @RequestBody @Validated CreateDirectChatRequestDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        Chat chat = chatService.getOrCreateDirectChat(currentUser.getId(), dto.getRecipientId());

        return ResponseWrapper.okEntity(chatMapper.toDTO(chat));
    }
}
