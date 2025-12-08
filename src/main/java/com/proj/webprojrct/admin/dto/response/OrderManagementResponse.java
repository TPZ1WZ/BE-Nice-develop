package com.proj.webprojrct.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderManagementResponse {
    
    private Long id;
    private String orderNumber; // Mã đơn hàng
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal totalAmount;
    private String status; // PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELED
    private String paymentMethod; // COD, MOMO, VNPAY, PAYPAL
    private String paymentStatus; // PENDING, PAID, FAILED, REFUNDED
    private String shippingAddress;
    private String trackingNumber;
    private LocalDateTime orderDate;
    private LocalDateTime updatedAt;
    
    // === CHI TIẾT ĐƠN HÀNG ===
    private List<OrderItemData> items;
    
    // === THÔNG TIN BỔ SUNG ===
    private String note; // Ghi chú của admin
    private String cancelReason; // Lý do hủy (nếu có)
    private Integer totalItems; // Tổng số sản phẩm
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemData {
        private Long productId;
        private String productName;
        private String productImage;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }
}