package com.kvanzi.chatapp.user.mapper;

import com.kvanzi.chatapp.user.dto.UserDTO;
import com.kvanzi.chatapp.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User entity);
}
