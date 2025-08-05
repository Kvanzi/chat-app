package com.kvanzi.chatapp.chat.service;

import com.kvanzi.chatapp.chat.document.message.Message;
import com.kvanzi.chatapp.chat.exception.NotChatMemberException;
import com.kvanzi.chatapp.chat.repository.MessageRepository;
import com.kvanzi.chatapp.user.component.IdentifiableUserDetails;
import com.kvanzi.chatapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatService chatService;
    private final UserService userService;

    public Page<Message> getMessages(String chatId, Pageable pageable) {
        IdentifiableUserDetails currentUserDetails = userService.getCurrentUserDetails();
        String currentUserId = currentUserDetails.getId();

        if (!chatService.isChatMember(chatId, currentUserId)) {
            throw new NotChatMemberException();
        }

        return messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);
    }
}
