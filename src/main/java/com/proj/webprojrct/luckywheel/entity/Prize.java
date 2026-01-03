package com.proj.webprojrct.luckywheel.entity;

import com.proj.webprojrct.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Prize Entity - Quản lý các phần thưởng trong vòng quay
 */
@Entity
@Table(name = "prizes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Prize extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Tên phần thưởng

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrizeType type; // VOUCHER, FREESHIP, POINTS, NOTHING

    @Column
    private String description; // Mô tả chi tiết

    @Column
    private Double discountValue; // Giá trị giảm giá (cho VOUCHER)

    @Column
    private Integer pointsValue; // Số điểm thưởng (cho POINTS)

    @Column
    private String couponCode; // Mã voucher/freeship

    @Column(nullable = false)
    private Double probability; // Tỷ lệ trúng (0.0 - 1.0)

    @Column
    private Integer quantity; // Số lượng phần thưởng (null = unlimited)

    @Column(nullable = false)
    private Integer remainingQuantity; // Số lượng còn lại

    @Column(nullable = false)
    private Boolean isActive = true; // Phần thưởng có hoạt động không

    @Column
    private String iconUrl; // URL icon của phần thưởng

    @Column
    private String color; // Màu hiển thị trên vòng quay (hex color)

    public enum PrizeType {
        VOUCHER,    // Voucher giảm giá
        FREESHIP,   // Mã freeship
        POINTS,     // Điểm thưởng
        NOTHING     // Không trúng gì
    }

    /**
     * Giảm số lượng phần thưởng còn lại
     */
    public void decreaseQuantity() {
        if (remainingQuantity != null && remainingQuantity > 0) {
            remainingQuantity--;
        }
    }

    /**
     * Kiểm tra còn phần thưởng không
     */
    public boolean isAvailable() {
        return isActive && (quantity == null || remainingQuantity > 0);
    }
}
