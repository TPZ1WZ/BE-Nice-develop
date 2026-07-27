package com.proj.webprojrct.auth;

import com.proj.webprojrct.auth.model.PendingPasswordReset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage pending password reset requests in memory
 */
@Service
@Slf4j
public class PendingPasswordResetService {

    // In-memory storage for pending password resets
    private final Map<String, PendingPasswordReset> pendingResets = new ConcurrentHashMap<>();

    /**
     * Save a pending password reset request
     */
    public void savePending(PendingPasswordReset reset) {
        pendingResets.put(reset.getEmail(), reset);
        log.info("📝 Saved pending password reset for email: {}", reset.getEmail());
    }

    /**
     * Find pending password reset by email
     */
    public Optional<PendingPasswordReset> findByEmail(String email) {
        PendingPasswordReset reset = pendingResets.get(email);
        if (reset != null && reset.getExpiresAt().isAfter(LocalDateTime.now())) {
            return Optional.of(reset);
        }
        return Optional.empty();
    }

    /**
     * Verify OTP for password reset
     */
    public boolean verifyOtp(String email, long otp) {
        Optional<PendingPasswordReset> resetOpt = findByEmail(email);

        if (resetOpt.isEmpty()) {
            log.warn("⚠️ No pending password reset found for email: {}", email);
            return false;
        }

        PendingPasswordReset reset = resetOpt.get();

        // Check if OTP matches
        if (reset.getOtp() != otp) {
            log.warn("⚠️ Invalid OTP for password reset: {}", email);
            return false;
        }

        // Mark as verified
        reset.setOtpVerified(true);
        pendingResets.put(email, reset);
        
        log.info("✅ OTP verified successfully for password reset: {}", email);
        return true;
    }

    /**
     * Check if OTP has been verified for this email
     */
    public boolean isOtpVerified(String email) {
        Optional<PendingPasswordReset> resetOpt = findByEmail(email);
        return resetOpt.isPresent() && resetOpt.get().isOtpVerified();
    }

    /**
     * Remove pending password reset after successful reset
     */
    public void removePending(String email) {
        pendingResets.remove(email);
        log.info("🗑️ Removed pending password reset for email: {}", email);
    }

    /**
     * Clean expired pending password resets every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void cleanExpired() {
        LocalDateTime now = LocalDateTime.now();
        int removed = 0;

        for (Map.Entry<String, PendingPasswordReset> entry : pendingResets.entrySet()) {
            if (entry.getValue().getExpiresAt().isBefore(now)) {
                pendingResets.remove(entry.getKey());
                removed++;
            }
        }

        if (removed > 0) {
            log.info("🧹 Cleaned {} expired pending password resets", removed);
        }
    }
}
