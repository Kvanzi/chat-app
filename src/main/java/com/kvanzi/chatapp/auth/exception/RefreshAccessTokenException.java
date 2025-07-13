package com.kvanzi.chatapp.auth.exception;

public class RefreshAccessTokenException extends RuntimeException {
    public RefreshAccessTokenException(String message) {
        super(message);
    }
}
