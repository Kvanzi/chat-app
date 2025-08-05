package com.kvanzi.chatapp.chat.exception;

public class SameUserChatException extends RuntimeException {
    public SameUserChatException(String message) {
        super(message);
    }
}
