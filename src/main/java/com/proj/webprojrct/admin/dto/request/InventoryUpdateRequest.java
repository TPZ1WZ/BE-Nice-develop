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
public class InventoryUpdateRequest {
    
    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;
    
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho phải từ 0 trở lên")
    private Integer stock;
    
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String note; // Lý do cập nhật stock
    
    @Pattern(regexp = "RESTOCK|SOLD|DAMAGED|RETURNED|ADJUSTMENT", 
             message = "Loại cập nhật không hợp lệ")
    private String updateType;
}