package com.proj.webprojrct.user.dto.response;

import com.proj.webprojrct.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateResponse {

    private long id;
    private String fullName;
    private String email;
    private UserRole role;
    private Boolean isActive;
    private String avatarUrl;
}
