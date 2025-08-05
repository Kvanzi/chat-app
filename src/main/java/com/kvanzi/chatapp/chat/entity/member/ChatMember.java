package com.kvanzi.chatapp.chat.entity.member;

import com.kvanzi.chatapp.chat.entity.chat.Chat;
import com.kvanzi.chatapp.common.entity.BaseEntity;
import com.kvanzi.chatapp.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "chat_members")
public class ChatMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    private Integer unreadCount = 0;
    private String lastReadMessageId;
}
