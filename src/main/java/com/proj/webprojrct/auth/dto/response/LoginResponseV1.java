package com.proj.webprojrct.auth.dto.response;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginResponseV1 {

    private String access_token;
    private String refresh_token;
}
