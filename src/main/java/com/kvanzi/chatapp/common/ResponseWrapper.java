package com.kvanzi.chatapp.common;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.*;

@Getter
public class ResponseWrapper<T> {

    private final int statusCode;
    private final String message;
    private final Instant timestamp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String reasonPhrase;

    @JsonIgnore
    private final Map<String, Object> additionalFields = new LinkedHashMap<>();

    @JsonIgnore
    private final HttpStatus status;

    @Builder
    private ResponseWrapper(
            HttpStatus status,
            String message,
            T data,
            Instant timestamp
    ) {
        this.status = status;
        this.statusCode = status.value();
        this.message = Objects.requireNonNull(message, "Message cannot be null");
        this.data = data;
        this.reasonPhrase = status.getReasonPhrase();
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static <T> ResponseWrapper<T> success(HttpStatus status, String message, T data) {
        validateSuccessStatus(status);
        return ResponseWrapper.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ResponseWrapper<T> success(HttpStatus status, String message) {
        return success(status, message, null);
    }

    public static <T> ResponseWrapper<T> success(T data) {
        return success(HttpStatus.OK, "Success", data);
    }

    public static ResponseWrapper<Void> success() {
        return success(HttpStatus.OK, "Success", null);
    }

    public static <T> ResponseWrapper<T> error(HttpStatus status, String message) {
        validateErrorStatus(status);
        return ResponseWrapper.<T>builder()
                .status(status)
                .message(message)
                .build();
    }

    public static <T> ResponseWrapper<T> error(HttpStatus status, String message, T errorData) {
        validateErrorStatus(status);
        return ResponseWrapper.<T>builder()
                .status(status)
                .message(message)
                .data(errorData)
                .build();
    }

    public static <T> ResponseWrapper<T> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }

    public static <T> ResponseWrapper<T> badRequest(String message, T errorData) {
        return error(HttpStatus.BAD_REQUEST, message, errorData);
    }

    public static <T> ResponseWrapper<T> unauthorized(String message) {
        return error(HttpStatus.UNAUTHORIZED, message);
    }

    public static <T> ResponseWrapper<T> unauthorized(String message, T errorData) {
        return error(HttpStatus.UNAUTHORIZED, message, errorData);
    }

    public static <T> ResponseWrapper<T> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, message);
    }

    public static <T> ResponseWrapper<T> notFound(String message, T errorData) {
        return error(HttpStatus.NOT_FOUND, message, errorData);
    }

    public static <T> ResponseWrapper<T> internalServerError(String message) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static <T> ResponseWrapper<T> internalServerError(String message, T errorData) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message, errorData);
    }

    public ResponseWrapper<T> addField(String key, Object value) {
        Objects.requireNonNull(key, "Key cannot be null");
        this.additionalFields.put(key, value);
        return this;
    }

    public ResponseWrapper<T> addFields(Map<String, Object> fields) {
        if (fields != null) {
            this.additionalFields.putAll(fields);
        }
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalFields() {
        return Collections.unmodifiableMap(additionalFields);
    }

    public static <T> Optional<T> extractData(ResponseEntity<ResponseWrapper<T>> response) {
        if (response == null || response.getBody() == null) {
            return Optional.empty();
        }

        ResponseWrapper<T> body = response.getBody();
        return response.getStatusCode().is2xxSuccessful() && body.getData() != null
                ? Optional.of(body.getData())
                : Optional.empty();
    }

    public ResponseEntity<ResponseWrapper<T>> toResponseEntity() {
        return ResponseEntity.status(this.status.value()).body(this);
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> okEntity(T data) {
        return ResponseWrapper.success(data).toResponseEntity();
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> okEntity(String message, T data) {
        return ResponseWrapper.success(HttpStatus.OK, message, data).toResponseEntity();
    }

    public static ResponseEntity<ResponseWrapper<Void>> okEntity() {
        return ResponseWrapper.success().toResponseEntity();
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> badRequestEntity(String message) {
        return ResponseWrapper.<T>badRequest(message).toResponseEntity();
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> badRequestEntity(String message, T errorData) {
        return ResponseWrapper.badRequest(message, errorData).toResponseEntity();
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> unauthorizedEntity(String message) {
        return ResponseWrapper.<T>unauthorized(message).toResponseEntity();
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> unauthorizedEntity(String message, T errorData) {
        return ResponseWrapper.unauthorized(message, errorData).toResponseEntity();
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> notFoundEntity(String message) {
        return ResponseWrapper.<T>notFound(message).toResponseEntity();
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> notFoundEntity(String message, T errorData) {
        return ResponseWrapper.notFound(message, errorData).toResponseEntity();
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> internalServerErrorEntity(String message) {
        return ResponseWrapper.<T>internalServerError(message).toResponseEntity();
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> internalServerErrorEntity(String message, T errorData) {
        return ResponseWrapper.internalServerError(message, errorData).toResponseEntity();
    }

    public boolean isSuccess() {
        return status.is2xxSuccessful();
    }

    public boolean isError() {
        return !status.is2xxSuccessful();
    }

    private static void validateSuccessStatus(HttpStatus status) {
        if (!status.is2xxSuccessful()) {
            throw new IllegalArgumentException("Status code must be 2xx for success responses, got: " + status);
        }
    }

    private static void validateErrorStatus(HttpStatus status) {
        if (status.is2xxSuccessful()) {
            throw new IllegalArgumentException("Status code must not be 2xx for error responses, got: " + status);
        }
    }

    @Override
    public String toString() {
        return "ResponseWrapper{statusCode=%d, message='%s', hasData=%s, timestamp=%s}"
                .formatted(status.value(), message, data != null, timestamp);
    }
}