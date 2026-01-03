-- Migration script for user_coupons table
-- Tạo bảng lưu voucher trong ví của user

CREATE TABLE IF NOT EXISTS user_coupons (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    spin_history_id BIGINT NOT NULL REFERENCES spin_history(id) ON DELETE CASCADE,
    coupon_code VARCHAR(50) NOT NULL,
    prize_name VARCHAR(255) NOT NULL,
    prize_description TEXT,
    discount_value DECIMAL(10,2),
    prize_type VARCHAR(20) NOT NULL CHECK (prize_type IN ('VOUCHER', 'FREESHIP', 'POINTS', 'NOTHING')),
    is_used BOOLEAN NOT NULL DEFAULT false,
    used_at TIMESTAMP,
    order_id BIGINT,
    expires_at TIMESTAMP NOT NULL,
    saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tạo indexes để tăng performance
CREATE INDEX idx_user_coupons_user_id ON user_coupons(user_id);
CREATE INDEX idx_user_coupons_coupon_code ON user_coupons(coupon_code);
CREATE INDEX idx_user_coupons_spin_history_id ON user_coupons(spin_history_id);
CREATE INDEX idx_user_coupons_is_used ON user_coupons(is_used);
CREATE INDEX idx_user_coupons_expires_at ON user_coupons(expires_at);

-- Unique constraint để đảm bảo không lưu trùng voucher
CREATE UNIQUE INDEX idx_user_coupons_unique ON user_coupons(spin_history_id, user_id);

-- Trigger để tự động update updated_at
CREATE OR REPLACE FUNCTION update_user_coupons_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_update_user_coupons_updated_at 
    BEFORE UPDATE ON user_coupons 
    FOR EACH ROW 
    EXECUTE FUNCTION update_user_coupons_updated_at();

-- Comment cho bảng và các cột
COMMENT ON TABLE user_coupons IS 'Ví voucher của user từ vòng quay may mắn';
COMMENT ON COLUMN user_coupons.coupon_code IS 'Mã voucher (VCE9B90107, FSxxxxxxxx)';
COMMENT ON COLUMN user_coupons.prize_name IS 'Tên phần thưởng (Giảm 10%, Free Ship)';
COMMENT ON COLUMN user_coupons.prize_type IS 'Loại phần thưởng: VOUCHER, FREESHIP, POINTS, NOTHING';
COMMENT ON COLUMN user_coupons.is_used IS 'Đã sử dụng voucher chưa';
COMMENT ON COLUMN user_coupons.expires_at IS 'Thời gian hết hạn (mặc định 30 ngày)';
COMMENT ON COLUMN user_coupons.saved_at IS 'Thời gian lưu vào ví';
