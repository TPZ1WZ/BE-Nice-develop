package com.proj.webprojrct.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponse {
    
    private Long id;
    private String fullName;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role; // ADMIN, STAFF, CLIENT, GUEST
    private Boolean isActive;
    private String avatarUrl;
    private String address;
    private String gender;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // === THỐNG KÊ KHÁCH HÀNG ===
    private Integer totalOrders; // Tổng số đơn hàng
    private BigDecimal totalSpent; // Tổng số tiền đã chi
    private LocalDateTime lastOrderDate; // Đơn hàng gần nhất
    private String customerTier; // VIP, REGULAR, NEW (dựa trên tổng chi tiêu)
    
    // === HOẠT ĐỘNG GẦN ĐÂY ===
    private LocalDateTime lastLoginAt;
    private String loginProvider; // LOCAL, GOOGLE, FACEBOOK
}