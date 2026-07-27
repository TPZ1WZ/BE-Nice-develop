
package com.proj.webprojrct.promotion.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("minOrderValue")
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
        // Handle null dates: if startDate is null, assume it's always valid from start
        // if endDate is null, assume it never expires
        boolean startDateValid = (startDate == null) || now.isAfter(startDate) || now.isEqual(startDate);
        boolean endDateValid = (endDate == null) || now.isBefore(endDate) || now.isEqual(endDate);
        
        return isActive && 
               startDateValid && 
               endDateValid && 
               usedCount < usageLimit;
    }

    public boolean canApplyToOrder(Double orderAmount) {
        boolean valid = isValid();
        boolean minOrderMet = orderAmount >= minOrderAmount;
        System.out.println("🔍 [COUPON canApplyToOrder] Code: " + code);
        System.out.println("  - isValid: " + valid);
        System.out.println("  - orderAmount: " + orderAmount);
        System.out.println("  - minOrderAmount: " + minOrderAmount);
        System.out.println("  - orderAmount >= minOrderAmount: " + minOrderMet);
        System.out.println("  - Final result: " + (valid && minOrderMet));
        return valid && minOrderMet;
    }

    public Double calculateDiscount(Double orderAmount) {
        System.out.println("💰 [COUPON calculateDiscount] Starting for code: " + code);
        System.out.println("  - orderAmount: " + orderAmount);
        
        if (!canApplyToOrder(orderAmount)) {
            System.out.println("  - canApplyToOrder returned FALSE, returning 0.0");
            return 0.0;
        }

        System.out.println("  - canApplyToOrder returned TRUE, calculating discount...");
        
        Double discount = 0.0;
        if (discountType == DiscountType.PERCENTAGE) {
            discount = orderAmount * (discountValue / 100);
            System.out.println("  - PERCENTAGE calculation: " + orderAmount + " * " + discountValue + "% = " + discount);
        } else if (discountType == DiscountType.FIXED_AMOUNT) {
            discount = discountValue;
            System.out.println("  - FIXED_AMOUNT: " + discount);
        }

        // Apply max discount limit
        if (maxDiscountAmount != null && discount > maxDiscountAmount) {
            System.out.println("  - Applying maxDiscountAmount cap: " + discount + " -> " + maxDiscountAmount);
            discount = maxDiscountAmount;
        }

        // Giảm giá không được vượt quá giá trị đơn hàng (không bao gồm phí ship)
        // Đảm bảo giá trị sau giảm giá không bị âm
        if (discount > orderAmount) {
            System.out.println("  - Discount exceeds order amount: " + discount + " -> " + orderAmount);
            discount = orderAmount;
        }

        System.out.println("  - Final calculated discount: " + discount);
        return discount;
    }
}
