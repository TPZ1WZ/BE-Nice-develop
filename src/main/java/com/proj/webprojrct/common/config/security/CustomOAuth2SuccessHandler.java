package com.proj.webprojrct.common.config.security;

import com.proj.webprojrct.user.entity.UserRole;
import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.user.repository.UserRepository;
import com.proj.webprojrct.auth.JwtService;
import com.proj.webprojrct.common.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CookieUtil cookieUtil;

    public CustomOAuth2SuccessHandler(UserRepository userRepository, JwtService jwtService, CookieUtil cookieUtil) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.cookieUtil = cookieUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        
        log.info("🔐 [OAUTH2] OAuth2 login successful - Email: {}", email);

        // Load user từ database
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("🆕 [OAUTH2] Creating new OAuth2 user - Email: {}", email);
                    // Nếu chưa có thì tạo mới user OAuth2
                    User newUser = User.builder()
                            .email(email)
                            .fullName(oAuth2User.getAttribute("name"))
                            .avatarUrl(oAuth2User.getAttribute("avatar_url"))
                            .passwordHash("") // không cần mật khẩu
                            .role(UserRole.MEMBER)
                            .isActive(true)
                            .build();
                    return userRepository.save(newUser);
                });

        String jwtAccessToken = jwtService.generateAccessToken(user);
        String jwtRefreshToken = jwtService.generateRefreshToken(user);

        // 🍪 Set tokens as HTTP-only cookies (SECURE)
        cookieUtil.setAccessTokenCookie(response, jwtAccessToken);
        cookieUtil.setRefreshTokenCookie(response, jwtRefreshToken);

        // Lưu refresh token vào DB
        user.setRefreshToken(jwtRefreshToken);
        userRepository.save(user);

        log.info("✅ [OAUTH2] JWT tokens set as cookies for user: {} | Role: {}", user.getEmail(), user.getRole());

        // 🔄 Redirect về frontend thay vì trả JSON
        response.sendRedirect("/home?oauth2=success");
    }
}
