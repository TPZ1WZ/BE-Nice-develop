package com.proj.webprojrct.common.config.security;

import com.proj.webprojrct.auth.JwtService;
import com.proj.webprojrct.common.util.CookieUtil;
import com.proj.webprojrct.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository, CookieUtil cookieUtil) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.cookieUtil = cookieUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            if (shouldNotFilter(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            String jwt = parseJwt(request);

            if (jwt != null) {
                if (jwtService.validateJwtToken(jwt)) {
                    String username = jwtService.extractUsername(jwt);

                    UserDetails userDetails = userRepository.findByEmail(username).orElse(null);

                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception e) {
            log.error("? [JWT-FILTER] Cannot set user authentication", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Parse JWT token from request - Priority: Cookie > Authorization Header
     */
    private String parseJwt(HttpServletRequest request) {
        // ?? Priority 1: Check cookies first (more secure)
        String jwtFromCookie = cookieUtil.getAccessTokenFromCookies(request).orElse(null);
        if (jwtFromCookie != null) {
            return jwtFromCookie;
        }

        // ?? Priority 2: Fallback to Authorization header (for API compatibility)
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean shouldSkip = path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/register") || path.startsWith("/api/v1/auth/forgot-password") || path.startsWith("/api/v1/auth/verify") || path.equals("/") || path.equals("/home") || path.equals("/login") || path.equals("/register") || path.equals("/test") || path.contains("/error") || path.startsWith("/WEB-INF/jsp") || path.startsWith("/static/") || path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/img/") || path.startsWith("/images/") || path.startsWith("/resources/");
        return shouldSkip;
    }
}
