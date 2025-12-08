package com.proj.webprojrct.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderRequest {
    private String shippingAddress;
    private String paymentMethod; // COD, VNPAY, MOMO, PAYPAL
    private String phone;
    private String couponCode; // Optional
}