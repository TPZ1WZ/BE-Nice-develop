package com.proj.webprojrct.user.dto.response;

import com.proj.webprojrct.user.entity.UserRole;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserUpdateResponse {

    private Long id;
    private String avatarUrl;
    private String fullName;
    private String email;
    private UserRole role;
    private Boolean isActive;
}
