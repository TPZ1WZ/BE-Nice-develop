
package com.proj.webprojrct.order.entity;

import com.proj.webprojrct.product.entity.Product;
import com.proj.webprojrct.user.entity.User;
import jakarta.persistence.*;
import com.proj.webprojrct.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Snapshot thông tin sản phẩm tại thời điểm mua
    private String productName;
    private String productImage;
    
    private Integer quantity;
    private Double productPrice; // Đơn giá tại thời điểm mua
    private Double totalPrice;   // Thành tiền = productPrice * quantity
    private String size;
}

