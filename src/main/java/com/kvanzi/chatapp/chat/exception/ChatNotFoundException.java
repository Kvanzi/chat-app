package com.kvanzi.chatapp.chat.exception;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException(String chatId) {
        super("Chat with id '%s' not found".formatted(chatId));
    }
}
