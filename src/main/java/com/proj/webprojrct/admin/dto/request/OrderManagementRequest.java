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
public class OrderManagementRequest {
    
    @Pattern(regexp = "PENDING|CONFIRMED|SHIPPING|COMPLETED|CANCELED", 
             message = "Trạng thái đơn hàng không hợp lệ")
    private String status;
    
    @Pattern(regexp = "COD|MOMO|VNPAY|PAYPAL", message = "Phương thức thanh toán không hợp lệ")
    private String paymentMethod;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Min(value = 0, message = "Giá trị đơn hàng tối thiểu phải từ 0")
    private Double minTotal;
    
    @Min(value = 0, message = "Giá trị đơn hàng tối đa phải từ 0")
    private Double maxTotal;
    
    private String customerEmail; // Tìm theo email khách hàng
    private String customerPhone; // Tìm theo phone khách hàng
    
    @Pattern(regexp = "newest|oldest|total_asc|total_desc", message = "Cách sắp xếp không hợp lệ")
    private String sortBy;
    
    @Min(value = 0, message = "Số trang phải từ 0 trở lên")
    @Builder.Default
    private Integer page = 0;
    
    @Min(value = 1, message = "Kích thước trang phải từ 1 trở lên")
    @Max(value = 100, message = "Kích thước trang không được vượt quá 100")
    @Builder.Default
    private Integer size = 20;
}