package com.proj.webprojrct.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * SecurityUtil - Utility class to get current user info from Security Context
 */
public class SecurityUtil {

    /**
     * Get current user ID from JWT token
     * 
     * @return user ID or throw exception if not found
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        // If principal is User entity (from JwtAuthenticationFilter)
        if (principal instanceof com.proj.webprojrct.user.entity.User) {
            return ((com.proj.webprojrct.user.entity.User) principal).getId();
        }

        // If principal is JWT token
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            // Get user ID from claim "id", "sub", or "userId"
            Object idClaim = jwt.getClaim("id");
            if (idClaim != null) {
                if (idClaim instanceof Long) {
                    return (Long) idClaim;
                } else if (idClaim instanceof Integer) {
                    return ((Integer) idClaim).longValue();
                } else if (idClaim instanceof String) {
                    return Long.parseLong((String) idClaim);
                }
            }

            String userId = jwt.getClaimAsString("sub");
            if (userId == null) {
                userId = jwt.getClaimAsString("userId");
            }
            if (userId != null) {
                try {
                    return Long.parseLong(userId);
                } catch (NumberFormatException e) {
                    // ignore, try other methods
                }
            }
        }

        // If principal is UserDetails (but not User entity)
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            // Try to parse username as ID (fallback)
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException e) {
                // If username is email, we can't get ID from here without looking up DB
                // But JwtFilter should have put User entity in principal
            }
        }

        // Fallback: get from name
        String name = authentication.getName();
        if (name != null) {
            try {
                return Long.parseLong(name);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Cannot extract user ID from authentication: " + name);
            }
        }

        throw new RuntimeException("Cannot extract user ID from authentication");
    }

    /**
     * Get current username
     * 
     * @return username or null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return authentication.getName();
    }

    /**
     * Get current user email from JWT token
     * @return email or null
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // If principal is JWT token
        if (principal instanceof Jwt jwt) {
            return jwt.getClaimAsString("email");
        }

        // If principal is UserDetails, return username (which is email in our case)
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        // Fallback to authentication name
        return authentication.getName();
    }

    /**
     * Check if user has specific role
     * 
     * @param role role to check
     * @return true if has role
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role) ||
                        auth.getAuthority().equals(role));
    }

    /**
     * Check if user is admin
     * 
     * @return true if admin
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
