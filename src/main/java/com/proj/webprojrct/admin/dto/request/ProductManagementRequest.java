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
public class ProductManagementRequest {
    
    private Long categoryId;
    
    @Pattern(regexp = "ACTIVE|INACTIVE|OUT_OF_STOCK", message = "Trạng thái sản phẩm không hợp lệ")
    private String status;
    
    @Min(value = 0, message = "Giá tối thiểu phải từ 0")
    private Double minPrice;
    
    @Min(value = 0, message = "Giá tối đa phải từ 0")
    private Double maxPrice;
    
    @Min(value = 0, message = "Số lượng tồn kho tối thiểu phải từ 0")
    private Integer minStock;
    
    private String keyword; // Tìm kiếm theo tên, mô tả
    
    @Pattern(regexp = "newest|oldest|price_asc|price_desc|stock_asc|stock_desc|best_seller", 
             message = "Cách sắp xếp không hợp lệ")
    private String sortBy;
    
    @Min(value = 0, message = "Số trang phải từ 0 trở lên")
    @Builder.Default
    private Integer page = 0;
    
    @Min(value = 1, message = "Kích thước trang phải từ 1 trở lên")
    @Max(value = 100, message = "Kích thước trang không được vượt quá 100")
    @Builder.Default
    private Integer size = 20;
}