package com.kvanzi.chatapp.user.mapper;

import com.kvanzi.chatapp.user.dto.UserDTO;
import com.kvanzi.chatapp.user.dto.UserProfileDTO;
import com.kvanzi.chatapp.user.entity.User;
import com.kvanzi.chatapp.user.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "status", ignore = true)
    UserProfileDTO profileToDTO(UserProfile entity);

    UserDTO userToDTO(User entity);

}
