package com.proj.webprojrct.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderRequest {
    @JsonProperty("receiver_name")
    private String receiverName;   // Tên người nhận hàng
    
    @JsonProperty("shipping_address")
    private String shippingAddress;
    
    @JsonProperty("payment_method")
    private String paymentMethod; // COD, VNPAY, MOMO, PAYPAL
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("coupon_code")
    private String couponCode; // Optional
    
    @JsonProperty("customer_note")
    private String customerNote; // Ghi chú của khách hàng
}