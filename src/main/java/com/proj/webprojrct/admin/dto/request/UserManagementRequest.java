package com.proj.webprojrct.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementRequest {
    
    @Pattern(regexp = "ADMIN|STAFF|CLIENT|GUEST", message = "Role không hợp lệ")
    private String role;
    
    private Boolean isActive;
    
    private LocalDate registeredAfter; // Đăng ký sau ngày
    private LocalDate registeredBefore; // Đăng ký trước ngày
    
    private String keyword; // Tìm kiếm theo email, tên, phone
    
    @Pattern(regexp = "newest|oldest|most_orders|most_spent", message = "Cách sắp xếp không hợp lệ")
    private String sortBy;
    
    @Min(value = 0, message = "Số trang phải từ 0 trở lên")
    @Builder.Default
    private Integer page = 0;
    
    @Min(value = 1, message = "Kích thước trang phải từ 1 trở lên")
    @Max(value = 100, message = "Kích thước trang không được vượt quá 100")
    @Builder.Default
    private Integer size = 20;
}