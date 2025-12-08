package com.proj.webprojrct.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateResponse {
    
    private Long orderId;
    private String orderNumber;
    private String oldStatus;
    private String newStatus;
    private String note;
    private String trackingNumber;
    private String cancelReason;
    private Long updatedBy; // Admin ID
    private String updatedByName;
    private LocalDateTime updatedAt;
    
    // === THÔNG TIN KHÁCH HÀNG ===
    private String customerName;
    private String customerEmail;
    
    // === TRẠNG THÁI TIẾP THEO ===
    private String nextPossibleStatus; // Trạng thái có thể chuyển tiếp
    private Boolean canCancel; // Có thể hủy không
    private Boolean needsTracking; // Cần mã vận đơn không
}