package com.kvanzi.chatapp.user.repository;

import com.kvanzi.chatapp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
//    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String username);
}
