package com.kvanzi.chatapp.chat.controller;

import com.kvanzi.chatapp.chat.document.message.Message;
import com.kvanzi.chatapp.chat.dto.SendMessageDTO;
import com.kvanzi.chatapp.chat.service.ChatService;
import com.kvanzi.chatapp.chat.service.MessageService;
import com.kvanzi.chatapp.common.ResponseWrapper;
import com.kvanzi.chatapp.user.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/chats/{chatId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ResponseWrapper<Message>> sendMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String chatId,
            @Valid @RequestBody SendMessageDTO dto
    ) {
        Message message = chatService.sendMessage(currentUser.getId(), chatId, dto.getText());

        return ResponseWrapper.okEntity(message);
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<Set<Message>>> getMessages(
            @PathVariable String chatId,

            @Valid
            @Min(value = 1, message = "pageSize cannot be less than 1")
            @Max(value = 50, message = "pageSize cannot be more than 50")
            @RequestParam(required = false, defaultValue = "20") int pageSize,

            @Valid
            @Min(value = 0, message = "pageSize cannot be less than 0")
            @RequestParam int pageNum
    ) {
        Pageable pageable = PageRequest.of(
                pageNum,
                pageSize,
                Sort.by(DESC, "createdAt")
        );
        Page<Message> messagesPage = messageService.getMessages(chatId, pageable);
        Set<Message> messages = new LinkedHashSet<>(messagesPage.getContent());

        return ResponseWrapper.success(messages)
                .addField("totalPages", messagesPage.getTotalPages())
                .addField("totalElements", messagesPage.getTotalElements())
                .toResponseEntity();
    }
}
