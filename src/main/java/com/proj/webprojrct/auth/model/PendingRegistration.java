package com.proj.webprojrct.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Temporary storage for user registration data before OTP verification
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingRegistration {
    private String email;
    private String fullName;
    private String phone;
    private String passwordHash;
    private long otp;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
