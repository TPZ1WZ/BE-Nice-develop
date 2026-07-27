package com.proj.webprojrct.luckywheel.entity;

import com.proj.webprojrct.product.entity.Product;
import com.proj.webprojrct.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ProductView Entity - Theo dõi lượt xem chi tiết sản phẩm để tính lượt quay
 */
@Entity
@Table(name = "product_views", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id", "view_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "view_date", nullable = false)
    private LocalDate viewDate;

    @Column(name = "view_time", nullable = false)
    private LocalDateTime viewTime;

    @PrePersist
    protected void onCreate() {
        viewTime = LocalDateTime.now();
        viewDate = LocalDate.now();
    }
}
