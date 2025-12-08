package com.proj.webprojrct.promotion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplyCouponRequest {
    private String couponCode;
    private Double orderAmount;
}