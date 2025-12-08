package com.proj.webprojrct.common.config.security;

import java.io.IOException;
import java.util.Optional;

import com.proj.webprojrct.auth.dto.response.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

    private final ObjectMapper mapper;

    // FIX lỗi NullPointerException
    public CustomAuthenticationEntryPoint() {
        this.mapper = new ObjectMapper(); // khởi tạo mapper đúng cách
    }

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException e) throws IOException {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());

        String message = Optional.ofNullable(e.getCause())
                .map(Throwable::getMessage)
                .orElse(e.getMessage());
        res.setMessage(message);

        res.setMessage("Token không hợp lệ (hết hạn, không đúng định dạng, hoặc không truyền JWT ở header)...");

        mapper.writeValue(response.getWriter(), res);;
    }

}
