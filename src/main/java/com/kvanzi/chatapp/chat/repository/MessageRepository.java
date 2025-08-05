package com.kvanzi.chatapp.chat.repository;

import com.kvanzi.chatapp.chat.document.message.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    Page<Message> findByChatIdOrderByCreatedAtDesc(String chatId, Pageable pageable);
}
