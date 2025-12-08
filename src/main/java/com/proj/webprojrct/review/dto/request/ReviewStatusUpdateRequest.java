package com.proj.webprojrct.review.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatusUpdateRequest {    
    @NotNull(message = "Trạng thái duyệt không được để trống")
    private Boolean isApproved; // true = approved, false = rejected
    
    private Boolean isHidden; // true = hidden, false = visible (optional)
    
    @Size(max = 500, message = "Lý do từ chối không được vượt quá 500 ký tự")
    private String rejectionReason; // Lý do nếu reject

    // Custom validation - rejectionReason bắt buộc khi reject
    @AssertTrue(message = "Lý do từ chối là bắt buộc khi không duyệt review")
    public boolean isRejectionReasonValid() {
        if (Boolean.FALSE.equals(isApproved)) {
            return rejectionReason != null && !rejectionReason.trim().isEmpty();
        }
        return true;
    }
    
    // Helper method để set trạng thái
    public void setStatus(Boolean approved, Boolean hidden) {
        this.isApproved = approved;
        this.isHidden = hidden != null ? hidden : false;
    }
}