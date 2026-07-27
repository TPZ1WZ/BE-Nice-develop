-- =====================================================
-- NOTIFICATION SYSTEM MIGRATION
-- Hệ thống Thông báo cho Nike Store App
-- =====================================================

-- Tạo bảng notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL, -- ORDER, FAVORITE, COUPON, PRODUCT, SYSTEM
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    data JSONB, -- Dữ liệu bổ sung (order_id, product_id, coupon_id, etc.)
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tạo index để tăng hiệu suất truy vấn
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;

-- Trigger tự động cập nhật updated_at
CREATE OR REPLACE FUNCTION update_notification_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_notification_timestamp
BEFORE UPDATE ON notifications
FOR EACH ROW
EXECUTE FUNCTION update_notification_timestamp();

-- =====================================================
-- DỮ LIỆU MẪU CHO TESTING
-- =====================================================

-- Thông báo đơn hàng
INSERT INTO notifications (user_id, type, title, message, data) VALUES
(1, 'ORDER', 'Đơn hàng đã được xác nhận', 'Đơn hàng #12345 của bạn đã được xác nhận và đang được xử lý.', '{"order_id": 12345, "status": "CONFIRMED"}'),
(1, 'ORDER', 'Đơn hàng đang giao', 'Đơn hàng #12345 đang trên đường giao đến bạn.', '{"order_id": 12345, "status": "SHIPPING"}'),
(1, 'ORDER', 'Giao hàng thành công', 'Đơn hàng #12345 đã được giao thành công.', '{"order_id": 12345, "status": "DELIVERED"}');

-- Thông báo sản phẩm yêu thích
INSERT INTO notifications (user_id, type, title, message, data) VALUES
(1, 'FAVORITE', 'Giá giảm - Sản phẩm yêu thích', 'Nike Air Max 90 trong danh sách yêu thích của bạn đang giảm giá 20%!', '{"product_id": 1, "discount": 20}'),
(1, 'FAVORITE', 'Hàng về - Sản phẩm yêu thích', 'Nike React Infinity Run đã có hàng trở lại!', '{"product_id": 5, "stock": 50}');

-- Thông báo khuyến mãi
INSERT INTO notifications (user_id, type, title, message, data) VALUES
(1, 'COUPON', 'Mã giảm giá mới', 'Bạn nhận được mã SUMMER2024 giảm 100.000đ cho đơn từ 500.000đ!', '{"coupon_code": "SUMMER2024", "discount": 100000}'),
(1, 'COUPON', 'Mã giảm giá sắp hết hạn', 'Mã FLASH50 của bạn sẽ hết hạn vào 25/12/2025. Sử dụng ngay!', '{"coupon_code": "FLASH50", "expiry": "2025-12-25"}');

-- Thông báo sản phẩm mới
INSERT INTO notifications (user_id, type, title, message, data) VALUES
(1, 'PRODUCT', 'Sản phẩm mới', 'Nike Air Force 1 phiên bản giới hạn vừa ra mắt. Xem ngay!', '{"product_id": 10, "is_new": true}');

-- Thông báo hệ thống
INSERT INTO notifications (user_id, type, title, message, data) VALUES
(1, 'SYSTEM', 'Chào mừng đến Nike Store', 'Cảm ơn bạn đã đăng ký tài khoản. Khám phá hàng nghìn sản phẩm chính hãng!', '{"welcome": true}'),
(1, 'SYSTEM', 'Cập nhật điều khoản', 'Điều khoản dịch vụ được cập nhật. Vui lòng đọc để biết thêm chi tiết.', '{"policy_update": true}');

-- =====================================================
-- CÁC LOẠI THÔNG BÁO (TYPE)
-- =====================================================
-- ORDER: Thông báo về đơn hàng
--   - Đơn hàng được tạo
--   - Đơn hàng được xác nhận
--   - Đơn hàng đang giao
--   - Đơn hàng giao thành công
--   - Đơn hàng bị hủy
--
-- FAVORITE: Thông báo về sản phẩm yêu thích
--   - Sản phẩm yêu thích giảm giá
--   - Sản phẩm yêu thích có hàng trở lại
--   - Sản phẩm yêu thích sắp hết hàng
--
-- COUPON: Thông báo về khuyến mãi
--   - Nhận mã giảm giá mới
--   - Mã giảm giá sắp hết hạn
--   - Chương trình khuyến mãi đặc biệt
--
-- PRODUCT: Thông báo về sản phẩm
--   - Sản phẩm mới ra mắt
--   - Flash sale
--   - Sản phẩm được khuyến nghị
--
-- SYSTEM: Thông báo hệ thống
--   - Thông báo chào mừng
--   - Cập nhật chính sách
--   - Bảo trì hệ thống
