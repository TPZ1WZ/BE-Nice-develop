package com.proj.webprojrct.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class for handling HTTP-only cookies for JWT tokens
 * Provides secure cookie management with proper security attributes
 */
@Component
@Slf4j
public class CookieUtil {

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${jwt.access-token-expiration.ms:3600000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration.ms:86400000}")
    private long refreshTokenExpiration;

    // Cookie names
    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    /**
     * Set access token cookie
     */
    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = createSecureCookie(ACCESS_TOKEN_COOKIE, token, (int) (accessTokenExpiration / 1000));
        response.addCookie(cookie);
        log.info("🍪 [COOKIE] Access token cookie set - Expires in: {} seconds", accessTokenExpiration / 1000);
    }

    /**
     * Set refresh token cookie
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = createSecureCookie(REFRESH_TOKEN_COOKIE, token, (int) (refreshTokenExpiration / 1000));
        response.addCookie(cookie);
        log.info("🍪 [COOKIE] Refresh token cookie set - Expires in: {} seconds", refreshTokenExpiration / 1000);
    }

    /**
     * Get access token from cookies
     */
    public Optional<String> getAccessTokenFromCookies(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE);
    }

    /**
     * Get refresh token from cookies
     */
    public Optional<String> getRefreshTokenFromCookies(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    /**
     * Clear all authentication cookies
     */
    public void clearAuthCookies(HttpServletResponse response) {
        clearCookie(response, ACCESS_TOKEN_COOKIE);
        clearCookie(response, REFRESH_TOKEN_COOKIE);
        log.info("🍪 [COOKIE] All authentication cookies cleared");
    }

    /**
     * Clear specific cookie
     */
    public void clearCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(false);
        cookie.setSecure(cookieSecure); // Use configurable secure setting
        cookie.setPath("/");
        cookie.setMaxAge(0); // Delete immediately
        response.addCookie(cookie);
        log.debug("🍪 [COOKIE] Cookie cleared: {}", cookieName);
    }

    /**
     * Create a secure cookie with proper security attributes
     */
    private Cookie createSecureCookie(String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        
        // Security settings
        cookie.setHttpOnly(true);           // Prevent XSS attacks
        cookie.setSecure(cookieSecure);     // HTTPS only (configurable for dev/prod)
        // Note: SameSite not directly supported in Jakarta Servlet, handled by Spring Security
        cookie.setPath("/");                // Available for entire application
        cookie.setMaxAge(maxAgeSeconds);    // Set expiration time
        
        log.debug("🍪 [COOKIE] Secure cookie created - Name: {} | MaxAge: {}s | HttpOnly: true | Secure: {}", 
                  name, maxAgeSeconds, cookieSecure);
        
        return cookie;
    }

    /**
     * Extract cookie value from request
     */
    private Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            log.debug("🍪 [COOKIE] No cookies found in request");
            return Optional.empty();
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                String value = cookie.getValue();
                if (value != null && !value.trim().isEmpty()) {
                    log.debug("🍪 [COOKIE] Found cookie: {} | Length: {}", cookieName, value.length());
                    return Optional.of(value);
                }
            }
        }
        
        log.debug("🍪 [COOKIE] Cookie not found: {}", cookieName);
        return Optional.empty();
    }
}