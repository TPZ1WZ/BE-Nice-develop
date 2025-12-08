package com.proj.webprojrct.user.dto.response;

import com.proj.webprojrct.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private UserRole role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
