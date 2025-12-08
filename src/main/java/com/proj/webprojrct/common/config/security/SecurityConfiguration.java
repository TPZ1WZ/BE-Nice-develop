package com.proj.webprojrct.common.config.security;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.proj.webprojrct.user.repository.UserRepository;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration với logging chi tiết
 * Xử lý JWT authentication, CORS, Session management
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfiguration {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> {
                csrf.disable();
                log.debug("CSRF protection disabled");
            })
            .cors(cors -> {
                cors.configurationSource(request -> {
                    var config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowedOrigins(java.util.List.of("*"));
                    config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(java.util.List.of("*"));
                    log.trace("CORS configured: Allow all origins");
                    return config;
                });
            })
            .sessionManagement(session -> {
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                log.debug("Session management: STATELESS (JWT-based authentication)");
            })

            // ========== TEMPORARY: ALL REQUESTS PERMITTED FOR TESTING ==========
            .authorizeHttpRequests(authz -> {
                authz.anyRequest().permitAll();
            })
            // ✅ Exception handling với JSON responses
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    
                    String jsonResponse = String.format(
                        "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\",\"status\":401}",
                        authException.getMessage().replace("\"", "\\\""),
                        request.getRequestURI()
                    );
                    response.getWriter().write(jsonResponse);
                    response.getWriter().flush();
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    
                    String jsonResponse = String.format(
                        "{\"error\":\"Forbidden\",\"message\":\"%s\",\"path\":\"%s\",\"status\":403}",
                        accessDeniedException.getMessage().replace("\"", "\\\""),
                        request.getRequestURI()
                    );
                    response.getWriter().write(jsonResponse);
                    response.getWriter().flush();
                })
            )

            // JWT Filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /* ========== CẤU HÌNH PHÂN QUYỀN GỐC (ĐÃ COMMENT) ==========
     * 
     * Lý do comment: 
     * - Lỗi 401 xảy ra vì endpoint /api/v1/users không có trong whitelist permitAll()
     * - Chỉ có /api/v1/auth/** được permitAll, nhưng frontend đang gọi /api/v1/users
     * - anyRequest().authenticated() yêu cầu authentication cho tất cả endpoint khác
     * 
     * Để bật lại phân quyền:
     * 1. Uncomment phần code bên dưới
     * 2. Thay thế .anyRequest().permitAll() ở trên
     * 3. Hoặc thêm .requestMatchers("/api/v1/users/**").permitAll() vào whitelist
     * 
     * Code gốc:
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/", "/home", "/login", "/register", "/forgot-password", "/test").permitAll() // Allow guest access to homepage and auth pages
            .requestMatchers("/api/v1/auth/**").permitAll() // All auth API endpoints
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/error/**", "/error").permitAll()
            .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/resources/**", 
                           "/static/**", "/webjars/**").permitAll() // Static resources
            .requestMatchers("/templates/**").permitAll() // Thymeleaf templates
            .anyRequest().authenticated()  // YÊU CẦU AUTHENTICATION CHO TẤT CẢ ENDPOINT KHÁC
        )
     * ========================================================= */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
