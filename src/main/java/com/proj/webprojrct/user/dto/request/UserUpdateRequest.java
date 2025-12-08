package com.proj.webprojrct.user.dto.request;

import com.proj.webprojrct.user.entity.UserRole;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserUpdateRequest {

    private String avatarUrl;
    private String fullName;
    private UserRole role;
    private String phoneNumber;
}
