-- Migration cho bảng coupons nâng cấp
-- Chạy lệnh này để cập nhật cấu trúc bảng coupons

-- Drop bảng cũ nếu tồn tại (chỉ cho development)
-- DROP TABLE IF EXISTS coupons;

-- Tạo bảng coupons mới với cấu trúc đầy đủ
CREATE TABLE IF NOT EXISTS coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    discount_type VARCHAR(20) NOT NULL CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    discount_value DECIMAL(10,2) NOT NULL CHECK (discount_value > 0),
    min_order_amount DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (min_order_amount >= 0),
    max_discount_amount DECIMAL(10,2) CHECK (max_discount_amount > 0),
    usage_limit INTEGER NOT NULL DEFAULT 1 CHECK (usage_limit >= 1),
    used_count INTEGER NOT NULL DEFAULT 0 CHECK (used_count >= 0),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_delete BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_coupon_dates CHECK (end_date > start_date),
    CONSTRAINT chk_usage_count CHECK (used_count <= usage_limit),
    CONSTRAINT chk_percentage_value CHECK (
        (discount_type = 'PERCENTAGE' AND discount_value >= 1 AND discount_value <= 100) OR
        (discount_type = 'FIXED_AMOUNT' AND discount_value > 0)
    )
);

-- Tạo indexes để tối ưu performance
CREATE INDEX IF NOT EXISTS idx_coupons_code ON coupons(code);
CREATE INDEX IF NOT EXISTS idx_coupons_active ON coupons(is_active);
CREATE INDEX IF NOT EXISTS idx_coupons_dates ON coupons(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_coupons_valid ON coupons(is_active, start_date, end_date, used_count, usage_limit);

-- Trigger để tự động update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_coupons_updated_at BEFORE UPDATE
    ON coupons FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert dữ liệu mẫu
DELETE FROM coupons;
INSERT INTO coupons (code, name, description, discount_type, discount_value, min_order_amount, max_discount_amount, usage_limit, used_count, start_date, end_date, is_active, created_at, updated_at) VALUES
('WELCOME10', 'Chào mừng khách hàng mới', 'Giảm 10% cho khách hàng mới đăng ký', 'PERCENTAGE', 10.0, 500000.0, 100000.0, 100, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SAVE50K', 'Tiết kiệm 50K', 'Giảm ngay 50,000đ cho đơn hàng từ 1 triệu', 'FIXED_AMOUNT', 50000.0, 1000000.0, 50000.0, 50, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '15 days', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('FLASH20', 'Flash Sale 20%', 'Giảm 20% trong thời gian có hạn', 'PERCENTAGE', 20.0, 200000.0, 200000.0, 200, 45, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '7 days', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('FREESHIP', 'Miễn phí vận chuyển', 'Giảm 30,000đ phí vận chuyển', 'FIXED_AMOUNT', 30000.0, 0.0, 30000.0, 1000, 234, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '60 days', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('VIP15', 'Thành viên VIP', 'Giảm 15% cho thành viên VIP', 'PERCENTAGE', 15.0, 800000.0, 150000.0, 25, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '90 days', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Comment bảng và cột
COMMENT ON TABLE coupons IS 'Bảng quản lý mã giảm giá/coupon';
COMMENT ON COLUMN coupons.code IS 'Mã coupon (unique)';
COMMENT ON COLUMN coupons.name IS 'Tên hiển thị của coupon';
COMMENT ON COLUMN coupons.description IS 'Mô tả chi tiết coupon';
COMMENT ON COLUMN coupons.discount_type IS 'Loại giảm giá: PERCENTAGE hoặc FIXED_AMOUNT';
COMMENT ON COLUMN coupons.discount_value IS 'Giá trị giảm (% hoặc số tiền)';
COMMENT ON COLUMN coupons.min_order_amount IS 'Giá trị đơn hàng tối thiểu để áp dụng';
COMMENT ON COLUMN coupons.max_discount_amount IS 'Số tiền giảm tối đa';
COMMENT ON COLUMN coupons.usage_limit IS 'Số lần sử dụng tối đa';
COMMENT ON COLUMN coupons.used_count IS 'Số lần đã sử dụng';
COMMENT ON COLUMN coupons.start_date IS 'Ngày bắt đầu hiệu lực';
COMMENT ON COLUMN coupons.end_date IS 'Ngày hết hiệu lực';
COMMENT ON COLUMN coupons.is_active IS 'Trạng thái kích hoạt';