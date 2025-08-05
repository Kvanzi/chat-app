package com.kvanzi.chatapp.ws.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
public class WebSocketGlobalExceptionHandler {

    @MessageExceptionHandler
    public void handleError(RuntimeException e) {
        log.debug("Unexpected error caught, please add exception handler", e);
    }
}
