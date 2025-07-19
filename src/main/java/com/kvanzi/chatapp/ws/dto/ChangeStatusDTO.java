package com.kvanzi.chatapp.ws.dto;

import com.kvanzi.chatapp.user.enumeration.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeStatusDTO {

    private UserStatus status;
}
