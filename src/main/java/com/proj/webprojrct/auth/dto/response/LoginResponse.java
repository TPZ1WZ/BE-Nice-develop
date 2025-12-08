package com.proj.webprojrct.auth.dto.response;

import com.proj.webprojrct.user.entity.UserRole;
import org.springframework.http.ResponseCookie;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginResponse {

    private String accessToken;
    private UserLogin user;
    @JsonIgnore
    private ResponseCookie springCookie;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class UserLogin {

        private long id;
        private String email;
        private String userName;
        private UserRole role;
    }
}
