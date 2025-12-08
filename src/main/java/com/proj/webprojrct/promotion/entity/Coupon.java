
package com.proj.webprojrct.promotion.entity;

import com.proj.webprojrct.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coupon extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private Double discountValue;

    @Column(nullable = false)
    private Double minOrderAmount = 0.0;

    @Column(nullable = true)
    private Double maxDiscountAmount;

    @Column(nullable = false)
    private Integer usageLimit = 1;

    @Column(nullable = false)
    private Integer usedCount = 0;

    @Column(nullable = true)
    private LocalDateTime startDate;

    @Column(nullable = true)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    public enum DiscountType {
        PERCENTAGE, // Giảm theo phần trăm
        FIXED_AMOUNT // Giảm số tiền cố định
    }

    // Helper methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               now.isAfter(startDate) && 
               now.isBefore(endDate) && 
               usedCount < usageLimit;
    }

    public boolean canApplyToOrder(Double orderAmount) {
        return isValid() && orderAmount >= minOrderAmount;
    }

    public Double calculateDiscount(Double orderAmount) {
        if (!canApplyToOrder(orderAmount)) {
            return 0.0;
        }

        Double discount = 0.0;
        if (discountType == DiscountType.PERCENTAGE) {
            discount = orderAmount * (discountValue / 100);
        } else if (discountType == DiscountType.FIXED_AMOUNT) {
            discount = discountValue;
        }

        // Apply max discount limit
        if (maxDiscountAmount != null && discount > maxDiscountAmount) {
            discount = maxDiscountAmount;
        }

        return discount;
    }
}
