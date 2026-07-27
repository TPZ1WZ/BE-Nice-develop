package com.proj.webprojrct.notification.entity;

/**
 * NotificationType - Enum các loại thông báo
 */
public enum NotificationType {
    /**
     * ORDER - Thông báo về đơn hàng
     * Sự kiện tạo thông báo:
     * - Đơn hàng được tạo mới
     * - Đơn hàng được xác nhận
     * - Đơn hàng đang được vận chuyển
     * - Đơn hàng giao thành công
     * - Đơn hàng bị hủy
     * - Đơn hàng hoàn trả
     */
    ORDER("Đơn hàng"),

    /**
     * FAVORITE - Thông báo về sản phẩm yêu thích
     * Sự kiện tạo thông báo:
     * - Sản phẩm yêu thích giảm giá
     * - Sản phẩm yêu thích có hàng trở lại
     * - Sản phẩm yêu thích sắp hết hàng
     * - Sản phẩm yêu thích ngừng kinh doanh
     */
    FAVORITE("Yêu thích"),

    /**
     * COUPON - Thông báo về mã giảm giá
     * Sự kiện tạo thông báo:
     * - Nhận được mã giảm giá mới
     * - Mã giảm giá sắp hết hạn (3 ngày, 1 ngày)
     * - Mã giảm giá đã được sử dụng
     * - Chương trình khuyến mãi đặc biệt
     */
    COUPON("Khuyến mãi"),

    /**
     * PRODUCT - Thông báo về sản phẩm
     * Sự kiện tạo thông báo:
     * - Sản phẩm mới ra mắt
     * - Flash sale đang diễn ra
     * - Sản phẩm được đề xuất cho người dùng
     * - Sản phẩm hot trend
     */
    PRODUCT("Sản phẩm"),

    /**
     * SYSTEM - Thông báo hệ thống
     * Sự kiện tạo thông báo:
     * - Chào mừng người dùng mới
     * - Cập nhật điều khoản dịch vụ
     * - Bảo trì hệ thống
     * - Cập nhật ứng dụng
     * - Thông báo quan trọng từ admin
     */
    SYSTEM("Hệ thống");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
