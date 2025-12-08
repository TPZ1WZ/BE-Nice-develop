package com.proj.webprojrct.common.config.security;

import java.util.Collections;

import com.proj.webprojrct.user.service.UserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component("userDetailsService")
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserService userService;

    public UserDetailServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 [LOAD USER] Loading user: " + username);
        
        com.proj.webprojrct.user.entity.User user = this.userService.getUserByUserName(username);
        if (user == null) {
            System.out.println("❌ [LOAD USER] User not found: " + username);
            throw new UsernameNotFoundException("Username/Password not valid");
        }

        System.out.println("✅ [LOAD USER] Found - Email: " + user.getEmail() + " | Role: " + user.getRole() + " | Active: " + user.getIsActive());
        System.out.println("🔐 [LOAD USER] Password hash: " + (user.getPasswordHash() != null ? user.getPasswordHash().substring(0, 30) + "..." : "NULL"));
        
        // ✅ TRẢ VỀ USER ENTITY TRỰC TIẾP
        // User entity implements UserDetails và đã có:
        // - getPassword() → returns passwordHash
        // - getAuthorities() → returns role
        // - isEnabled() → returns isActive
        return user;
    }
}
