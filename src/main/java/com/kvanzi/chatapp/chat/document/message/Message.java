package com.kvanzi.chatapp.chat.document.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kvanzi.chatapp.chat.enumeration.MessageStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Message {

    @Id
    private String id;

    @Indexed
    @NotBlank(message = "Chat id can't be null")
    private String chatId;

    @NotBlank(message = "Sender id can't be null")
    private String senderId;

    @Indexed
    @Builder.Default
    private Instant createdAt = Instant.now();
    private Instant updatedAt;

    @NotBlank(message = "Text can't be null")
    @Size(min = 1, max = 4096, message = "Text can't be more than 4096 symbols")
    private String text;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    @Builder.Default
    private Boolean isEdited = false;
    private Instant editedAt;

    @JsonIgnore
    public Optional<Instant> getUpdatedAtOpt() {
        return Optional.ofNullable(updatedAt);
    }

    @JsonIgnore
    public Optional<Instant> getEditedAtOpt() {
        return Optional.ofNullable(editedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Message message)) return false;
        return Objects.equals(getId(), message.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
