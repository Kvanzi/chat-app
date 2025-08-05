package com.kvanzi.chatapp.chat.exception;

public class NotChatMemberException extends RuntimeException {
    public NotChatMemberException(String message) {
        super(message);
    }
    public NotChatMemberException() {
        super("User not authorized for this chat");
    }
}
