package com.proj.webprojrct.luckywheel.entity;

import com.proj.webprojrct.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * WheelConfig Entity - Cấu hình vòng quay
 */
@Entity
@Table(name = "wheel_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WheelConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String configKey; // Khóa cấu hình duy nhất

    @Column(nullable = false)
    private Boolean isActive = true; // Vòng quay có hoạt động không

    @Column(nullable = false)
    private Integer maxSpinsPerDay = 1; // Số lần quay tối đa mỗi ngày

    @Column(nullable = false)
    private Integer maxSpinsPerWeek = 7; // Số lần quay tối đa mỗi tuần

    @Column(nullable = false)
    private Boolean requiresLogin = true; // Yêu cầu đăng nhập

    @Column(nullable = false)
    private Boolean requiresOrder = false; // Yêu cầu có đơn hàng

    @Column
    private Integer minOrderCount = 0; // Số đơn hàng tối thiểu

    @Column
    private String description; // Mô tả hoặc thông báo

    // ==== Quản lý sự kiện ====
    @Column
    private String eventName; // Tên sự kiện (ví dụ: "Tết Nguyên Đán 2026", "Black Friday")

    @Column
    private LocalDateTime startDate; // Ngày bắt đầu sự kiện

    @Column
    private LocalDateTime endDate; // Ngày kết thúc sự kiện

    @Column(nullable = false)
    private Boolean isTimeRestricted = false; // Có giới hạn theo thời gian không

    /**
     * Kiểm tra vòng quay có đang trong thời gian hoạt động không
     */
    public boolean isCurrentlyActive() {
        if (!isActive) {
            return false;
        }

        if (!isTimeRestricted) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();

        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }

        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }

        return true;
    }

    /**
     * Lấy thông báo trạng thái
     */
    public String getStatusMessage() {
        if (!isActive) {
            return "Vòng quay đang tạm ngưng";
        }

        if (!isTimeRestricted) {
            return "Vòng quay đang hoạt động";
        }

        LocalDateTime now = LocalDateTime.now();

        if (startDate != null && now.isBefore(startDate)) {
            return "Sự kiện chưa bắt đầu";
        }

        if (endDate != null && now.isAfter(endDate)) {
            return "Sự kiện đã kết thúc";
        }

        return "Sự kiện đang diễn ra";
    }
}
