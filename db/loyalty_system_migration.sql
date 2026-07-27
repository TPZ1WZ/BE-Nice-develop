-- ================================================
-- NIKE COIN LOYALTY SYSTEM - DATABASE MIGRATION
-- ================================================
-- Tạo hệ thống tích điểm và checkin hàng ngày

-- 1. Thêm cột loyalty_points vào bảng users
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS loyalty_points INTEGER DEFAULT 0 NOT NULL;

COMMENT ON COLUMN users.loyalty_points IS 'Số điểm Nike Coin tích lũy của user';

CREATE INDEX IF NOT EXISTS idx_users_loyalty_points ON users(loyalty_points DESC);

-- 2. Tạo bảng daily_checkins (lưu lịch sử checkin hàng ngày)
CREATE TABLE IF NOT EXISTS daily_checkins (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    checkin_date DATE NOT NULL DEFAULT CURRENT_DATE,
    reward_amount INTEGER NOT NULL, -- Số coin nhận được
    current_streak INTEGER NOT NULL DEFAULT 1, -- Chuỗi ngày liên tiếp hiện tại
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_date UNIQUE(user_id, checkin_date)
);

COMMENT ON TABLE daily_checkins IS 'Lưu lịch sử checkin hàng ngày của user';
COMMENT ON COLUMN daily_checkins.user_id IS 'ID của user';
COMMENT ON COLUMN daily_checkins.checkin_date IS 'Ngày checkin';
COMMENT ON COLUMN daily_checkins.reward_amount IS 'Số coin nhận được lần này';
COMMENT ON COLUMN daily_checkins.current_streak IS 'Chuỗi ngày liên tiếp tính đến lần checkin này';

CREATE INDEX IF NOT EXISTS idx_daily_checkins_user_id ON daily_checkins(user_id);
CREATE INDEX IF NOT EXISTS idx_daily_checkins_date ON daily_checkins(checkin_date DESC);
CREATE INDEX IF NOT EXISTS idx_daily_checkins_user_date ON daily_checkins(user_id, checkin_date DESC);

-- 3. Tạo bảng loyalty_transactions (lưu lịch sử giao dịch coin)
CREATE TABLE IF NOT EXISTS loyalty_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    transaction_type VARCHAR(50) NOT NULL, -- EARN, SPEND, REFUND
    amount INTEGER NOT NULL, -- Số coin (dương = earn, âm = spend)
    source VARCHAR(50) NOT NULL, -- DAILY_CHECKIN, ORDER, REVIEW, LUCKY_WHEEL, ADMIN, etc.
    description TEXT,
    reference_id BIGINT, -- ID của order/review/spin_history nếu có
    balance_after INTEGER NOT NULL, -- Số dư sau giao dịch
    expiry_date TIMESTAMP, -- Ngày hết hạn của coin (chỉ áp dụng cho EARN transactions)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE loyalty_transactions IS 'Lưu lịch sử giao dịch coin của user';
COMMENT ON COLUMN loyalty_transactions.user_id IS 'ID của user';
COMMENT ON COLUMN loyalty_transactions.transaction_type IS 'EARN (tích), SPEND (tiêu), REFUND (hoàn)';
COMMENT ON COLUMN loyalty_transactions.amount IS 'Số coin (dương = nhận, âm = tiêu)';
COMMENT ON COLUMN loyalty_transactions.source IS 'Nguồn: DAILY_CHECKIN, ORDER, REVIEW, LUCKY_WHEEL, etc.';
COMMENT ON COLUMN loyalty_transactions.reference_id IS 'ID tham chiếu (order_id, review_id, ...)';
COMMENT ON COLUMN loyalty_transactions.balance_after IS 'Số dư sau khi giao dịch';
COMMENT ON COLUMN loyalty_transactions.expiry_date IS 'Ngày hết hạn của coin (áp dụng cho EARN, mặc định +30 ngày)';

CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_user_id ON loyalty_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_type ON loyalty_transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_source ON loyalty_transactions(source);
CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_created ON loyalty_transactions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_expiry ON loyalty_transactions(expiry_date) WHERE expiry_date IS NOT NULL;

-- 4. Cấu hình reward cho daily checkin (7 ngày)
CREATE TABLE IF NOT EXISTS daily_checkin_rewards (
    id SERIAL PRIMARY KEY,
    day_number INTEGER NOT NULL UNIQUE, -- 1-7
    reward_amount INTEGER NOT NULL, -- Số coin
    is_bonus BOOLEAN DEFAULT FALSE, -- Có phải ngày đặc biệt không
    description VARCHAR(255)
);

COMMENT ON TABLE daily_checkin_rewards IS 'Cấu hình phần thưởng cho mỗi ngày checkin';

-- Insert default rewards (6 ngày 1000 coin, ngày 7 bonus 4000 coin)
INSERT INTO daily_checkin_rewards (day_number, reward_amount, is_bonus, description)
VALUES 
    (1, 1000, FALSE, 'Day 1 reward'),
    (2, 1000, FALSE, 'Day 2 reward'),
    (3, 1000, FALSE, 'Day 3 reward'),
    (4, 1000, FALSE, 'Day 4 reward'),
    (5, 1000, FALSE, 'Day 5 reward'),
    (6, 1000, FALSE, 'Day 6 reward'),
    (7, 4000, TRUE, 'Day 7 bonus reward')
ON CONFLICT (day_number) DO NOTHING;

-- 5. Tạo function để tính current streak của user
CREATE OR REPLACE FUNCTION get_user_current_streak(p_user_id BIGINT) 
RETURNS INTEGER AS $$
DECLARE
    v_streak INTEGER := 0;
    v_last_checkin_date DATE;
    v_check_date DATE;
BEGIN
    -- Lấy ngày checkin gần nhất
    SELECT checkin_date INTO v_last_checkin_date
    FROM daily_checkins
    WHERE user_id = p_user_id
    ORDER BY checkin_date DESC
    LIMIT 1;
    
    -- Nếu chưa từng checkin
    IF v_last_checkin_date IS NULL THEN
        RETURN 0;
    END IF;
    
    -- Nếu ngày cuối cùng checkin là hôm nay hoặc hôm qua
    IF v_last_checkin_date >= CURRENT_DATE - INTERVAL '1 day' THEN
        -- Đếm ngược từ ngày cuối cùng
        v_check_date := v_last_checkin_date;
        
        LOOP
            -- Kiểm tra có checkin vào ngày này không
            IF NOT EXISTS (
                SELECT 1 FROM daily_checkins 
                WHERE user_id = p_user_id AND checkin_date = v_check_date
            ) THEN
                EXIT;
            END IF;
            
            v_streak := v_streak + 1;
            v_check_date := v_check_date - INTERVAL '1 day';
            
            -- Tối đa 7 ngày
            IF v_streak >= 7 THEN
                EXIT;
            END IF;
        END LOOP;
    END IF;
    
    RETURN v_streak;
END;
$$ LANGUAGE plpgsql;

-- 6. Tạo function để check user đã checkin hôm nay chưa
CREATE OR REPLACE FUNCTION has_checked_in_today(p_user_id BIGINT) 
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM daily_checkins
        WHERE user_id = p_user_id AND checkin_date = CURRENT_DATE
    );
END;
$$ LANGUAGE plpgsql;

-- 7. Trigger để tự động cập nhật loyalty_points khi có transaction
CREATE OR REPLACE FUNCTION update_user_loyalty_points()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE users
    SET loyalty_points = NEW.balance_after
    WHERE id = NEW.user_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_loyalty_points
AFTER INSERT ON loyalty_transactions
FOR EACH ROW
EXECUTE FUNCTION update_user_loyalty_points();

-- ================================================
-- TEST DATA (Optional - comment out nếu không cần)
-- ================================================

-- Update existing users với random loyalty points
-- UPDATE users 
-- SET loyalty_points = (RANDOM() * 10000)::INTEGER
-- WHERE loyalty_points = 0;

COMMIT;
