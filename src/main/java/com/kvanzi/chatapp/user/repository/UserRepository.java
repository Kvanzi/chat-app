package com.kvanzi.chatapp.user.repository;

import com.kvanzi.chatapp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
