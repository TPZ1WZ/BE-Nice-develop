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
     * @return user ID or throw exception if not found
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        
        // If principal is JWT token
        if (principal instanceof Jwt jwt) {
            // Get user ID from claim "sub" or "userId"
            String userId = jwt.getClaimAsString("sub");
            if (userId == null) {
                userId = jwt.getClaimAsString("userId");
            }
            if (userId != null) {
                return Long.parseLong(userId);
            }
        }
        
        // If principal is UserDetails
        if (principal instanceof UserDetails userDetails) {
            // Assume username is user ID
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException e) {
                // If username is not a number, may need different logic
            }
        }
        
        // Fallback: get from name
        String name = authentication.getName();
        if (name != null) {
            try {
                return Long.parseLong(name);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Cannot extract user ID from authentication");
            }
        }
        
        throw new RuntimeException("Cannot extract user ID from authentication");
    }

    /**
     * Get current username
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
     * Get current user email from JWT token or UserDetails
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
     * @return true if admin
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
