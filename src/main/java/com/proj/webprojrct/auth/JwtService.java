package com.proj.webprojrct.auth;

import com.proj.webprojrct.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * JWT Service với logging chi tiết cho debugging
 * Xử lý generation và validation của Access Token & Refresh Token
 */
@Component
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration.ms}")
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration.ms}")
    private long refreshTokenExpiration;

    /**
     * Generate Access Token (short-lived)
     */
    public String generateAccessToken(User user) {
        log.info("🔐 [JWT] Generating ACCESS token for user: {}", user.getEmail());
        log.debug("Access token expiration: {}ms ({}min)", accessTokenExpiration, accessTokenExpiration / 60000);
        
        String token = generateToken(user, accessTokenExpiration, "access");
        
        log.info("✅ [JWT] ACCESS token generated successfully - User: {} | Length: {} chars", 
                 user.getEmail(), token.length());
        log.debug("Token preview: {}...{}", token.substring(0, Math.min(20, token.length())), 
                  token.substring(Math.max(0, token.length() - 20)));
        
        return token;
    }

    /**
     * Generate Refresh Token (long-lived)
     */
    public String generateRefreshToken(User user) {
        log.info("🔄 [JWT] Generating REFRESH token for user: {}", user.getEmail());
        log.debug("Refresh token expiration: {}ms ({}hours)", refreshTokenExpiration, refreshTokenExpiration / 3600000);
        
        String token = generateToken(user, refreshTokenExpiration, "refresh");
        
        log.info("✅ [JWT] REFRESH token generated successfully - User: {} | Length: {} chars", 
                 user.getEmail(), token.length());
        
        return token;
    }

    /**
     * Core token generation logic
     */
    private String generateToken(User user, long expirationMillis, String type) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(System.currentTimeMillis() + expirationMillis);
        
        log.debug("📝 [JWT] Building {} token - Issued: {} | Expires: {} | Duration: {}ms",
                  type.toUpperCase(), issuedAt, expiresAt, expirationMillis);
        
        try {
            String token = Jwts.builder()
                    .setSubject(user.getEmail())
                    .claim("id", user.getId())
                    .claim("email", user.getEmail())
                    .claim("fullName", user.getFullName())
                    .claim("role", user.getRole().name())
                    .claim("isActive", user.isActive())
                    .claim("type", type) // "access" or "refresh"
                    .setIssuedAt(issuedAt)
                    .setExpiration(expiresAt)
                    .signWith(SignatureAlgorithm.HS256, getSigningKey())
                    .compact();
            
            log.debug("🔑 [JWT] Token signed with HS256 algorithm");
            return token;
            
        } catch (Exception e) {
            log.error("❌ [JWT] Failed to generate {} token for user: {} - Error: {}", 
                      type, user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * Extract all claims from token with error handling
     */
    public Claims extractAllClaims(String token) {
        log.debug("📖 [JWT] Extracting claims from token");
        
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();
            
            log.debug("✅ [JWT] Claims extracted - Subject: {} | Expiration: {}", 
                      claims.getSubject(), claims.getExpiration());
            return claims;
            
        } catch (ExpiredJwtException e) {
            log.warn("⏰ [JWT] Token expired - Expired at: {}", e.getClaims().getExpiration());
            throw e;
        } catch (SignatureException e) {
            log.error("🔴 [JWT] Invalid signature - Token tampered or wrong secret key");
            throw e;
        } catch (MalformedJwtException e) {
            log.error("🔴 [JWT] Malformed token - Invalid JWT structure");
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("🔴 [JWT] Unsupported token format");
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("🔴 [JWT] Token claims string is empty");
            throw e;
        } catch (Exception e) {
            log.error("❌ [JWT] Unexpected error extracting claims: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Extract specific claim - Role
     */
    public String extractRole(String token) {
        String role = (String) extractAllClaims(token).get("role");
        log.debug("👤 [JWT] Extracted role: {}", role);
        return role;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            boolean expired = expiration.before(new Date());
            
            if (expired) {
                log.warn("⏰ [JWT] Token is EXPIRED - Expired at: {} | Now: {}", expiration, new Date());
            } else {
                log.debug("✅ [JWT] Token is VALID - Expires at: {} | Time remaining: {}ms", 
                          expiration, expiration.getTime() - System.currentTimeMillis());
            }
            
            return expired;
        } catch (Exception e) {
            log.error("❌ [JWT] Error checking token expiration: {}", e.getMessage());
            return true; // Treat as expired if error
        }
    }

    /**
     * Validate JWT token with comprehensive logging
     */
    public boolean validateJwtToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("⚠️ [JWT] Token is null or empty");
            return false;
        }
        
        try {
            Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            String tokenType = (String) claims.get("type");
            Date expiration = claims.getExpiration();
            return true;
            
        } catch (ExpiredJwtException e) {
            log.warn("⏰ [JWT] Token validation FAILED - Token expired at: {}", 
                     e.getClaims().getExpiration());
            return false;
        } catch (SignatureException e) {
            log.error("🔴 [JWT] Token validation FAILED - Invalid signature");
            return false;
        } catch (MalformedJwtException e) {
            log.error("🔴 [JWT] Token validation FAILED - Malformed token");
            return false;
        } catch (Exception e) {
            log.error("❌ [JWT] Token validation FAILED - Error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract token type (access/refresh)
     */
    public String extractTokenType(String token) {
        String type = (String) extractAllClaims(token).get("type");
        return type;
    }

    /**
     * Check if token is Access Token
     */
    public boolean isAccessToken(String token) {
        boolean isAccess = "access".equals(extractTokenType(token));
        return isAccess;
    }

    /**
     * Check if token is Refresh Token
     */
    public boolean isRefreshToken(String token) {
        boolean isRefresh = "refresh".equals(extractTokenType(token));
        return isRefresh;
    }

    /**
     * Extract username (email) from token
     */
    public String extractUsername(String jwt) {
        String username = (String) extractAllClaims(jwt).get("email");
        return username;
    }

    /**
     * Get signing key for JWT
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

