package com.kvanzi.chatapp.user.mapper;

import com.kvanzi.chatapp.user.dto.UserDTO;
import com.kvanzi.chatapp.user.dto.UserProfileDTO;
import com.kvanzi.chatapp.user.entity.User;
import com.kvanzi.chatapp.user.entity.UserProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO userToDTO(User entity);
    UserProfileDTO profileToDTO(UserProfile entity);
}
