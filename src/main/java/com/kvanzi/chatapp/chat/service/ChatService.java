package com.kvanzi.chatapp.chat.service;

import com.kvanzi.chatapp.chat.document.message.Message;
import com.kvanzi.chatapp.chat.entity.chat.Chat;
import com.kvanzi.chatapp.chat.entity.chat.DirectChat;
import com.kvanzi.chatapp.chat.entity.member.ChatMember;
import com.kvanzi.chatapp.chat.enumeration.MessageStatus;
import com.kvanzi.chatapp.chat.exception.ChatNotFoundException;
import com.kvanzi.chatapp.chat.exception.NotChatMemberException;
import com.kvanzi.chatapp.chat.exception.SameUserChatException;
import com.kvanzi.chatapp.chat.repository.ChatRepository;
import com.kvanzi.chatapp.chat.repository.MessageRepository;
import com.kvanzi.chatapp.user.entity.User;
import com.kvanzi.chatapp.user.exception.UserNotFoundException;
import com.kvanzi.chatapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final AsyncChatService asyncChatService;

    public Set<Chat> getAllChatsByUserId(String userId) {
        return chatRepository.findChatsByUserId(userId);
    }

    @Transactional
    public Chat getOrCreateDirectChat(String userId1, String userId2) {
        if (userId1.equals(userId2)) {
            throw new SameUserChatException("Cannot create direct chat with the same user");
        }

        Optional<Chat> existsChat = findDirectChat(userId1, userId2);

        if (existsChat.isPresent()) {
            return existsChat.get();
        }

        DirectChat chat = new DirectChat();

        List.of(userId1, userId2).forEach(userId -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new UserNotFoundException("User with id '%s' not found"
                                    .formatted(userId))
                    );

            ChatMember member = ChatMember.builder()
                    .user(user)
                    .chat(chat)
                    .build();

            chat.addMember(member);
        });

        return chatRepository.save(chat);
    }

    public Optional<Chat> findDirectChat(String userId1, String userId2) {
        return chatRepository.findChatByTwoUsers(userId1, userId2);
    }

    @Transactional
    public Message sendMessage(String senderId, String chatId, String text) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));

        boolean isMember = chat.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(senderId));

        if (!isMember) {
            throw new NotChatMemberException();
        }

        Message message = messageRepository.save(Message.builder()
                .chatId(chatId)
                .senderId(senderId)
                .text(text)
                .status(MessageStatus.SENT)
                .build());

        chatRepository.updateLastMessage(chatId, message.getId());

        Set<String> receiversIds = chat.getMembers().stream()
                .map(ChatMember::getUser)
                .map(User::getId)
                .filter(id -> !id.equals(senderId))
                .collect(Collectors.toSet());

        asyncChatService.broadcastMessage(receiversIds, message);

        return message;
    }

    public boolean isChatMember(String chatId, String userId) {
        return chatRepository.existsByChatIdAndUserId(chatId, userId);
    }
}

