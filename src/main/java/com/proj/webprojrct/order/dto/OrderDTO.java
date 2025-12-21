package com.proj.webprojrct.order.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private Double totalAmount;
    private Double totalDiscount;
    private Double finalAmount;
    private Double shippingFee;
    private Integer quantity;
    private String receiverName;  // Tên người nhận hàng
    private String phone;
    private String status;
    private String paymentMethod;
    private String shippingAddress;
    private String txnId;
    private String customerNote;
    private String adminNote;
    private List<OrderItemDTO> items;
    private CouponOrderDTO coupon;
    private LocalDateTime createdAt;
    private UserOrderDTO user;
}
