package com.kvanzi.chatapp.chat.entity.chat;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("DIRECT")
public class DirectChat extends Chat {
}
