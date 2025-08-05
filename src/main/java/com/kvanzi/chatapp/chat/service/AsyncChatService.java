package com.kvanzi.chatapp.chat.service;

import com.kvanzi.chatapp.chat.document.message.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncChatService {

    private final SimpMessagingTemplate messagingTemplate;

    @Async
    public void broadcastMessage(Set<String> receiversIds, Message message) {
        receiversIds.forEach(receiverId -> {
            try {
                messagingTemplate.convertAndSendToUser(receiverId, "/queue/messages", message);
            } catch (MessagingException e) {
                log.error("Can't send message to user queue", e);
            }
        });
    }
}
