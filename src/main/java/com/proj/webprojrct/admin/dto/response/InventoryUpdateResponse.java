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
public class InventoryUpdateResponse {
    
    private Long productId;
    private String productName;
    private Integer oldStock;
    private Integer newStock;
    private Integer stockChange; // Thay đổi (+/-)
    private String updateType; // RESTOCK, SOLD, DAMAGED, RETURNED, ADJUSTMENT
    private String note;
    private Long updatedBy; // Admin ID
    private String updatedByName;
    private LocalDateTime updatedAt;
    
    // === TRẠNG THÁI MỚI ===
    private String stockStatus; // LOW_STOCK, OUT_OF_STOCK, IN_STOCK
    private Boolean needsRestock; // Cần nhập hàng
}