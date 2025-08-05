package com.kvanzi.chatapp.chat.mapper;

import com.kvanzi.chatapp.chat.dto.ChatDTO;
import com.kvanzi.chatapp.chat.entity.chat.Chat;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    ChatDTO toDTO(Chat entity);
}
