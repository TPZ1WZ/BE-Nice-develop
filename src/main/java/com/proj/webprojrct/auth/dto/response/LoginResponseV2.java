package com.proj.webprojrct.auth.dto.response;

import lombok.*;

/**
 * Simple response for cookie-based authentication
 * No user info returned - tokens are set as HTTP-only cookies
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginResponseV2 {
    private String accessToken;  // For mobile apps
    private String role;
    private boolean success;
    private String message;
}