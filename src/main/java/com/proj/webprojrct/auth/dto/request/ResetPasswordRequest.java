package com.proj.webprojrct.auth.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResetPasswordRequest {

    private Long token;
    private String newPassword;
}
