package com.kvanzi.chatapp.chat.entity.chat;

import com.kvanzi.chatapp.chat.entity.member.ChatMember;
import com.kvanzi.chatapp.chat.enumeration.ChatType;
import com.kvanzi.chatapp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "chats")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public class Chat extends BaseEntity {

    @Column(nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ChatType type;

    private String lastMessageId;

    @OneToMany(
            mappedBy = "chat",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<ChatMember> members = new ArrayList<>();

    public Optional<String> getLastMessageIdOpt() {
        return Optional.ofNullable(lastMessageId);
    }

    public void addMember(ChatMember member) {
        this.getMembers().add(member);
    }

    @PostLoad
    @PostPersist
    private void initializeType() {
        if (this.type == null) {
            if (this instanceof DirectChat) {
                this.type = ChatType.DIRECT;
            } else if (this instanceof GroupChat) {
                this.type = ChatType.GROUP;
            }
        }
    }
}
