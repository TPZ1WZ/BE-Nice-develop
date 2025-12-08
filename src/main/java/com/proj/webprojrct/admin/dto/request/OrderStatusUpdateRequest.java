package com.proj.webprojrct.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {
    
    @NotBlank(message = "Trạng thái đơn hàng không được để trống")
    @Pattern(regexp = "PENDING|CONFIRMED|SHIPPING|COMPLETED|CANCELED", 
             message = "Trạng thái đơn hàng không hợp lệ")
    private String status;
    
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String note; // Lý do thay đổi trạng thái
    
    private String trackingNumber; // Mã vận đơn (khi SHIPPING)
    
    @Size(max = 500, message = "Lý do hủy không được vượt quá 500 ký tự")
    private String cancelReason; // Lý do hủy (khi CANCELED)
}