package com.kvanzi.chatapp.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateDirectChatRequestDTO {

    @NotBlank(message = "Recipient id cannot be blank or null")
    private String recipientId;
}
