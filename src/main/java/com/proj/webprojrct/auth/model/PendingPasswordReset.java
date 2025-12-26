package com.proj.webprojrct.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingPasswordReset {
    
    private String email;
    private long otp;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean otpVerified; // Track if OTP has been verified
}
