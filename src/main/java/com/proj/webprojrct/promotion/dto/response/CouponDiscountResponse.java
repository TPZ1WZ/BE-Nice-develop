package com.proj.webprojrct.promotion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponDiscountResponse {
    private Boolean isValid;
    private String message;
    private Double discountAmount;
    private Double finalAmount;
    private CouponResponse coupon;

    public static CouponDiscountResponse invalid(String message) {
        return new CouponDiscountResponse(false, message, 0.0, null, null);
    }

    public static CouponDiscountResponse valid(Double discountAmount, Double finalAmount, CouponResponse coupon) {
        return new CouponDiscountResponse(true, "Áp dụng mã giảm giá thành công", discountAmount, finalAmount, coupon);
    }
}