-- ============================================================================
-- LUCKY WHEEL MIGRATION - Updated Logic v2.0
-- Cập nhật logic vòng quay may mắn theo yêu cầu mới:
-- - Giới hạn 1 lượt quay/ngày
-- - Kiếm lượt quay bằng cách xem chi tiết ít nhất 3 sản phẩm
-- - Phần thưởng theo tỉ lệ mới: 35% +100, 25% +200, 15% +500, 10% +1000, 10% 0, 5% +2000
-- - Admin có thể bật/tắt, quản lý phần thưởng và weight
-- ============================================================================

-- 1. Thêm cột weight vào bảng lucky_wheel_rewards
ALTER TABLE lucky_wheel_rewards 
ADD COLUMN IF NOT EXISTS weight INTEGER DEFAULT 10 NOT NULL;

-- 2. Tạo bảng product_views để theo dõi lượt xem sản phẩm
CREATE TABLE IF NOT EXISTS product_views (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    view_date DATE NOT NULL,
    view_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_product_date UNIQUE (user_id, product_id, view_date)
);

CREATE INDEX IF NOT EXISTS idx_product_views_user_date ON product_views(user_id, view_date);
CREATE INDEX IF NOT EXISTS idx_product_views_product ON product_views(product_id);

-- 3. Tạo bảng cấu hình vòng quay
CREATE TABLE IF NOT EXISTS lucky_wheel_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value VARCHAR(255) NOT NULL,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Thêm cấu hình mặc định
INSERT INTO lucky_wheel_config (config_key, config_value, description)
VALUES ('lucky_wheel_enabled', 'true', 'Enable/disable lucky wheel feature')
ON CONFLICT (config_key) DO NOTHING;

-- 5. Xóa dữ liệu cũ và cập nhật phần thưởng mới
DELETE FROM lucky_wheel_rewards;

-- 6. Thêm phần thưởng mới theo tỉ lệ yêu cầu (tổng weight = 200)
INSERT INTO lucky_wheel_rewards (position, reward_type, coin_amount, weight, probability, icon_name, label, is_active, created_at)
VALUES 
    -- 40%: +100 coin (weight = 80)
    (0, 'COIN', 100, 80, 40.00, 'ic_coin', '+100 Coin', true, CURRENT_TIMESTAMP),
    
    -- 25%: +200 coin (weight = 50)
    (1, 'COIN', 200, 50, 25.00, 'ic_coin_stack', '+200 Coin', true, CURRENT_TIMESTAMP),
    
    -- 15%: +300 coin (weight = 30)
    (2, 'COIN', 300, 30, 15.00, 'ic_coin_medium', '+300 Coin', true, CURRENT_TIMESTAMP),
    
    -- 8%: +400 coin (weight = 16)
    (3, 'COIN', 400, 16, 8.00, 'ic_coin_large', '+400 Coin', true, CURRENT_TIMESTAMP),
    
    -- 5%: +500 coin (weight = 10)
    (4, 'COIN', 500, 10, 5.00, 'ic_coin_xlarge', '+500 Coin', true, CURRENT_TIMESTAMP),
    
    -- 1.5%: +1000 coin (weight = 3)
    (5, 'COIN', 1000, 3, 1.50, 'ic_coin_xxlarge', '+1,000 Coin', true, CURRENT_TIMESTAMP),
    
    -- 0.5%: +2000 coin (weight = 1)
    (6, 'COIN', 2000, 1, 0.50, 'ic_jackpot', '+2,000 Coin', true, CURRENT_TIMESTAMP),
    
    -- 5%: Chúc bạn may mắn (weight = 10)
    (7, 'NOTHING', 0, 10, 5.00, 'ic_sad', 'Chúc bạn may mắn lần sau', true, CURRENT_TIMESTAMP);

-- 7. Comment giải thích
COMMENT ON TABLE product_views IS 'Theo dõi lượt xem chi tiết sản phẩm để tính lượt quay';
COMMENT ON TABLE lucky_wheel_config IS 'Cấu hình tính năng vòng quay may mắn';
COMMENT ON COLUMN lucky_wheel_rewards.weight IS 'Trọng số để tính xác suất (tổng weight = 200 cho 100%)';

-- 8. Thông báo hoàn thành
DO $$
BEGIN
    RAISE NOTICE '✅ Lucky Wheel migration completed successfully!';
    RAISE NOTICE '📊 Phần thưởng:';
    RAISE NOTICE '   - 40%%: +100 coin';
    RAISE NOTICE '   - 25%%: +200 coin';
    RAISE NOTICE '   - 15%%: +300 coin';
    RAISE NOTICE '   - 8%%: +400 coin';
    RAISE NOTICE '   - 5%%: +500 coin';
    RAISE NOTICE '   - 1.5%%: +1000 coin';
    RAISE NOTICE '   - 0.5%%: +2000 coin';
    RAISE NOTICE '   - 5%%: Chúc bạn may mắn (0 coin)';
    RAISE NOTICE '🎯 Giới hạn: 1 lượt/ngày, cần xem 3 sản phẩm';
END $$;
