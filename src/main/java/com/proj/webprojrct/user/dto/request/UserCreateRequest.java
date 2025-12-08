package com.proj.webprojrct.user.dto.request;

import com.proj.webprojrct.user.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = "passwordHash") // Exclude sensitive data from logs
public class UserCreateRequest {

    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Full name is required")
    private String fullName;
    @NotBlank(message = "Password is required")
    private String passwordHash;
    @NotBlank(message = "Role is required")
    private UserRole role;
    @Builder.Default
    private Boolean isActive = true;
    private String phoneNumber;
    private String address;
}
