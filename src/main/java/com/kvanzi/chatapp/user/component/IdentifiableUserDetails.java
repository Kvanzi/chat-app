package com.kvanzi.chatapp.user.component;

import org.springframework.security.core.userdetails.UserDetails;

//@Component
public interface IdentifiableUserDetails extends UserDetails {

    String getId();
}
