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
    private Integer quantity;
    private String phone;
    private String status; // pending, confirmed, shipping, completed, canceled
    private String paymentMethod; // COD, Momo, VNPay, PayPal
    private String shippingAddress;
    private String txnId;       // reference id for payment gateway: vnpay

    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> items;
}
