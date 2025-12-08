package com.proj.webprojrct.order.dto;

import com.proj.webprojrct.promotion.entity.Coupon.DiscountType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponOrderDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private DiscountType discountType;
    private Double discountValue;
    private Double minOrderAmount;
    private Double maxDiscountAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;

    // ✅ Optional fields derived from helper methods
    private Boolean validNow;         // true nếu đang trong thời gian hiệu lực
    private Double remainingUses;     // số lần sử dụng còn lại
}
