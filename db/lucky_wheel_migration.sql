-- ================================================
-- LUCKY WHEEL SYSTEM - DATABASE MIGRATION
-- ================================================
-- Tạo hệ thống vòng quay may mắn nhận coin

-- 1. Bảng cấu hình phần thưởng vòng quay (8 ô)
CREATE TABLE IF NOT EXISTS lucky_wheel_rewards (
    id SERIAL PRIMARY KEY,
    position INTEGER NOT NULL UNIQUE, -- Vị trí 0-7 trên vòng quay
    reward_type VARCHAR(50) NOT NULL, -- COIN, COUPON, NOTHING
    coin_amount INTEGER, -- Số coin nếu là COIN
    coupon_id BIGINT REFERENCES coupons(id), -- ID coupon nếu là COUPON
    probability DECIMAL(5,2) NOT NULL, -- Xác suất trúng (%)
    icon_name VARCHAR(100), -- Tên icon để hiển thị
    label VARCHAR(255), -- Nhãn hiển thị
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE lucky_wheel_rewards IS 'Cấu hình phần thưởng cho vòng quay may mắn';
COMMENT ON COLUMN lucky_wheel_rewards.position IS 'Vị trí trên vòng quay (0-7)';
COMMENT ON COLUMN lucky_wheel_rewards.reward_type IS 'COIN (coin), COUPON (mã giảm giá), NOTHING (chúc may mắn lần sau)';
COMMENT ON COLUMN lucky_wheel_rewards.probability IS 'Xác suất trúng thưởng (tổng phải = 100%)';

-- 2. Bảng lịch sử quay thưởng
CREATE TABLE IF NOT EXISTS spin_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reward_id INTEGER REFERENCES lucky_wheel_rewards(id),
    reward_type VARCHAR(50) NOT NULL,
    coin_amount INTEGER, -- Số coin nhận được
    coupon_id BIGINT, -- ID coupon nhận được
    spin_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cost INTEGER DEFAULT 0, -- Chi phí để quay (0 = free, 500 = trả coin)
    CONSTRAINT idx_user_spin_date UNIQUE(user_id, spin_date)
);

COMMENT ON TABLE spin_history IS 'Lịch sử quay thưởng của user';
COMMENT ON COLUMN spin_history.cost IS 'Chi phí quay: 0 = lượt miễn phí, 500 = mất 500 coin';

CREATE INDEX IF NOT EXISTS idx_spin_history_user_id ON spin_history(user_id);
CREATE INDEX IF NOT EXISTS idx_spin_history_date ON spin_history(spin_date DESC);

-- 3. Insert dữ liệu mẫu cho 8 ô vòng quay
INSERT INTO lucky_wheel_rewards (position, reward_type, coin_amount, probability, icon_name, label)
VALUES 
    (0, 'COIN', 1000, 30.00, 'ic_coin', '1,000 Coin'),
    (1, 'NOTHING', NULL, 20.00, 'ic_sad', 'Chúc bạn may mắn lần sau'),
    (2, 'COIN', 2000, 15.00, 'ic_coin_stack', '2,000 Coin'),
    (3, 'COIN', 500, 15.00, 'ic_coin_small', '500 Coin'),
    (4, 'COIN', 5000, 5.00, 'ic_coin_big', '5,000 Coin'),
    (5, 'COIN', 1500, 10.00, 'ic_coin', '1,500 Coin'),
    (6, 'NOTHING', NULL, 4.00, 'ic_sad', 'Chúc bạn may mắn lần sau'),
    (7, 'COIN', 10000, 1.00, 'ic_jackpot', 'JACKPOT 10,000 Coin')
ON CONFLICT (position) DO NOTHING;

-- 4. Function kiểm tra user đã quay miễn phí hôm nay chưa
CREATE OR REPLACE FUNCTION has_free_spin_today(p_user_id BIGINT) 
RETURNS BOOLEAN AS $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM spin_history
    WHERE user_id = p_user_id
      AND DATE(spin_date) = CURRENT_DATE
      AND cost = 0; -- Chỉ đếm lượt free
    
    RETURN v_count > 0;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION has_free_spin_today IS 'Kiểm tra user đã dùng lượt quay miễn phí hôm nay chưa';

-- 5. View thống kê vòng quay
CREATE OR REPLACE VIEW v_spin_statistics AS
SELECT 
    u.id as user_id,
    u.email,
    COUNT(sh.id) as total_spins,
    COUNT(CASE WHEN sh.cost = 0 THEN 1 END) as free_spins,
    COUNT(CASE WHEN sh.cost > 0 THEN 1 END) as paid_spins,
    COALESCE(SUM(sh.coin_amount), 0) as total_coins_won,
    MAX(sh.coin_amount) as max_win,
    MAX(sh.spin_date) as last_spin_date
FROM users u
LEFT JOIN spin_history sh ON u.id = sh.user_id
GROUP BY u.id, u.email;

COMMENT ON VIEW v_spin_statistics IS 'Thống kê vòng quay của từng user';
