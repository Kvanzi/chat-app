package com.kvanzi.chatapp.chat.repository;

import com.kvanzi.chatapp.chat.entity.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String> {

    @Query("""
            SELECT c FROM Chat c
            WHERE c.id IN (
                SELECT cm1.chat.id FROM ChatMember cm1
                WHERE cm1.user.id = :userId1
            )
            AND c.id IN (
                SELECT cm2.chat.id FROM ChatMember cm2
                WHERE cm2.user.id = :userId2
            )
            AND (
                SELECT COUNT(cm3) FROM ChatMember cm3
                WHERE cm3.chat.id = c.id
            ) = 2
            """)
    Optional<Chat> findChatByTwoUsers(@Param("userId1") String userId1, @Param("userId2") String userId2);

    @Query("""
            SELECT c FROM Chat c
            JOIN c.members cm
            WHERE cm.user.id = :userId
            """)
    Set<Chat> findChatsByUserId(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE Chat c SET c.lastMessageId = :messageId WHERE c.id = :chatId")
    void updateLastMessage(@Param("chatId") String chatId, @Param("messageId") String messageId);

    @Query("SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END " +
            "FROM ChatMember cm WHERE cm.chat.id = :chatId AND cm.user.id = :userId")
    boolean existsByChatIdAndUserId(@Param("chatId") String chatId, @Param("userId") String userId);
}
