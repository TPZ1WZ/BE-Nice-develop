package com.proj.webprojrct.notification.entity;

import com.proj.webprojrct.common.entity.BaseEntity;
import com.proj.webprojrct.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * Notification Entity - Thực thể Thông báo
 * 
 * Các loại thông báo (type):
 * - ORDER: Thông báo về đơn hàng (tạo, xác nhận, giao hàng, hoàn thành, hủy)
 * - FAVORITE: Thông báo về sản phẩm yêu thích (giảm giá, có hàng trở lại)
 * - COUPON: Thông báo về mã giảm giá (mã mới, sắp hết hạn)
 * - PRODUCT: Thông báo về sản phẩm (sản phẩm mới, flash sale)
 * - SYSTEM: Thông báo hệ thống (chào mừng, cập nhật chính sách)
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user_id", columnList = "user_id"),
    @Index(name = "idx_notifications_is_read", columnList = "is_read"),
    @Index(name = "idx_notifications_created_at", columnList = "created_at"),
    @Index(name = "idx_notifications_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Dữ liệu bổ sung dạng JSON
     * Ví dụ:
     * - ORDER: {"order_id": 123, "status": "CONFIRMED"}
     * - FAVORITE: {"product_id": 456, "discount": 20}
     * - COUPON: {"coupon_code": "SUMMER2024", "discount": 100000}
     * - PRODUCT: {"product_id": 789, "is_new": true}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    private Map<String, Object> data;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * Đánh dấu thông báo đã đọc
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * Kiểm tra thông báo có liên quan đến đơn hàng không
     */
    public boolean isOrderNotification() {
        return this.type == NotificationType.ORDER;
    }

    /**
     * Kiểm tra thông báo có liên quan đến sản phẩm yêu thích không
     */
    public boolean isFavoriteNotification() {
        return this.type == NotificationType.FAVORITE;
    }

    /**
     * Kiểm tra thông báo có liên quan đến khuyến mãi không
     */
    public boolean isCouponNotification() {
        return this.type == NotificationType.COUPON;
    }

    /**
     * Lấy order_id từ data nếu là thông báo đơn hàng
     */
    public Long getOrderId() {
        if (data != null && data.containsKey("order_id")) {
            Object orderId = data.get("order_id");
            if (orderId instanceof Number) {
                return ((Number) orderId).longValue();
            }
        }
        return null;
    }

    /**
     * Lấy product_id từ data
     */
    public Long getProductId() {
        if (data != null && data.containsKey("product_id")) {
            Object productId = data.get("product_id");
            if (productId instanceof Number) {
                return ((Number) productId).longValue();
            }
        }
        return null;
    }

    /**
     * Lấy coupon_code từ data
     */
    public String getCouponCode() {
        if (data != null && data.containsKey("coupon_code")) {
            return (String) data.get("coupon_code");
        }
        return null;
    }
}
