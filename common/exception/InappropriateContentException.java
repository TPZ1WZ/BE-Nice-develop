package com.proj.webprojrct.common.exception;

/**
 * Exception for inappropriate review content detected by AI
 */
public class InappropriateContentException extends RuntimeException {
    
    private final String reason;
    
    public InappropriateContentException(String reason) {
        super("Your review contains inappropriate content and cannot be posted");
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
}
