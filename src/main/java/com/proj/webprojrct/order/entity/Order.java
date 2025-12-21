package com.proj.webprojrct.order.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.proj.webprojrct.common.entity.BaseEntity;
import com.proj.webprojrct.promotion.entity.Coupon;
import com.proj.webprojrct.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Double totalAmount;
    private Double totalDiscount;
    private Double finalAmount;
    private Double shippingFee = 0.0; // Phí ship cố định 0đ (sẽ phát triển sau)
    private Integer quantity;
    private String receiverName; // Tên người nhận hàng
    private String phone;
    private String status; // pending, confirmed, shipping, completed, canceled
    private String paymentMethod; // COD, Momo, VNPay, PayPal
    private String shippingAddress;
    private String txnId;       // reference id for payment gateway: vnpay
    
    @Column(length = 1000)
    private String customerNote; // Ghi chú của khách hàng
    
    @Column(length = 1000)
    private String adminNote;    // Ghi chú của admin

    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> items;
}
