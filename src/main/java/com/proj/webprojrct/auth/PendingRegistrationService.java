package com.proj.webprojrct.auth;

import com.proj.webprojrct.auth.model.PendingRegistration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage pending user registrations in memory before OTP
 * verification
 */
@Service
@Slf4j
public class PendingRegistrationService {

    // In-memory storage for pending registrations
    private final Map<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();

    /**
     * Save a pending registration
     */
    public void savePending(PendingRegistration registration) {
        pendingRegistrations.put(registration.getEmail(), registration);
        log.info("📝 Saved pending registration for email: {}", registration.getEmail());
    }

    /**
     * Find pending registration by email
     */
    public Optional<PendingRegistration> findByEmail(String email) {
        PendingRegistration registration = pendingRegistrations.get(email);
        if (registration != null && registration.getExpiresAt().isAfter(LocalDateTime.now())) {
            return Optional.of(registration);
        }
        return Optional.empty();
    }

    /**
     * Verify OTP and return registration data if valid
     */
    public Optional<PendingRegistration> verifyAndGet(String email, long otp) {
        Optional<PendingRegistration> pendingOpt = findByEmail(email);

        if (pendingOpt.isEmpty()) {
            log.warn("⚠️ No pending registration found for email: '{}'", email);
            log.warn("⚠️ Available pending emails: {}", pendingRegistrations.keySet());
            return Optional.empty();
        }

        PendingRegistration pending = pendingOpt.get();
        
        log.info("🔍 Comparing OTP - Stored: {}, Provided: {}", pending.getOtp(), otp);

        // Check if OTP matches
        if (pending.getOtp() != otp) {
            log.warn("⚠️ Invalid OTP for email: '{}' - Expected: {}, Got: {}", 
                    email, pending.getOtp(), otp);
            return Optional.empty();
        }

        log.info("✅ OTP verified successfully for email: '{}'", email);
        return Optional.of(pending);
    }

    /**
     * Remove pending registration after successful verification
     */
    public void removePending(String email) {
        pendingRegistrations.remove(email);
        log.info("🗑️ Removed pending registration for email: {}", email);
    }

    /**
     * Clean expired pending registrations every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void cleanExpired() {
        LocalDateTime now = LocalDateTime.now();
        int removed = 0;

        for (Map.Entry<String, PendingRegistration> entry : pendingRegistrations.entrySet()) {
            if (entry.getValue().getExpiresAt().isBefore(now)) {
                pendingRegistrations.remove(entry.getKey());
                removed++;
            }
        }

        if (removed > 0) {
            log.info("🧹 Cleaned {} expired pending registrations", removed);
        }
    }
}
