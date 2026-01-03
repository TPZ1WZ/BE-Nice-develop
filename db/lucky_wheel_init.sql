-- ============================================
-- LUCKY WHEEL - PRIZES INITIALIZATION
-- ============================================
-- Script để tạo bảng và seed dữ liệu phần thưởng vòng quay may mắn
-- Run this script after init.sql

-- Tạo bảng prizes nếu chưa có
CREATE TABLE IF NOT EXISTS prizes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,  -- VOUCHER, FREESHIP, POINTS, NOTHING
    description TEXT,
    discount_value DOUBLE PRECISION,
    points_value INTEGER,
    probability DOUBLE PRECISION NOT NULL,
    icon_url VARCHAR(255),
    color VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Tạo bảng spin_history nếu chưa có
CREATE TABLE IF NOT EXISTS spin_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    prize_id BIGINT,
    prize_code VARCHAR(100),
    spin_date TIMESTAMP NOT NULL DEFAULT NOW(),
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    used_date TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (prize_id) REFERENCES prizes(id) ON DELETE SET NULL
);

-- Xóa dữ liệu cũ (nếu có)
TRUNCATE TABLE spin_history CASCADE;
TRUNCATE TABLE prizes CASCADE;

-- ============================================
-- SEED PRIZES DATA
-- ============================================

-- 1. Voucher Giảm 10%
INSERT INTO prizes (name, type, description, discount_value, probability, icon_url, color, is_active)
VALUES (
    'Giảm 10%',
    'VOUCHER',
    'Giảm 10% cho đơn hàng từ 500,000đ',
    10.0,
    0.25,  -- 25% tỷ lệ trúng
    '/images/prizes/discount-10.png',
    '#FF6B6B',
    TRUE
);

-- 2. Voucher Giảm 20%
INSERT INTO prizes (name, type, description, discount_value, probability, icon_url, color, is_active)
VALUES (
    'Giảm 20%',
    'VOUCHER',
    'Giảm 20% cho đơn hàng từ 1,000,000đ',
    20.0,
    0.15,  -- 15% tỷ lệ trúng
    '/images/prizes/discount-20.png',
    '#4ECDC4',
    TRUE
);

-- 3. Freeship
INSERT INTO prizes (name, type, description, discount_value, probability, icon_url, color, is_active)
VALUES (
    'Freeship',
    'FREESHIP',
    'Miễn phí vận chuyển cho mọi đơn hàng',
    0.0,
    0.20,  -- 20% tỷ lệ trúng
    '/images/prizes/freeship.png',
    '#FFE66D',
    TRUE
);

-- 4. Điểm thưởng
INSERT INTO prizes (name, type, description, points_value, probability, icon_url, color, is_active)
VALUES (
    'Điểm thưởng',
    'POINTS',
    'Nhận 100 điểm thưởng tích lũy',
    100,
    0.20,  -- 20% tỷ lệ trúng
    '/images/prizes/points.png',
    '#95E1D3',
    TRUE
);

-- 5. Quà tặng
INSERT INTO prizes (name, type, description, probability, icon_url, color, is_active)
VALUES (
    'Quà tặng',
    'GIFT',
    'Nhận 1 quà tặng bất ngờ từ Nike',
    0.05,  -- 5% tỷ lệ trúng
    '/images/prizes/gift.png',
    '#F38181',
    TRUE
);

-- 6. Chúc may mắn lần sau
INSERT INTO prizes (name, type, description, probability, icon_url, color, is_active)
VALUES (
    'Chúc may mắn',
    'NOTHING',
    'Chúc bạn may mắn lần sau!',
    0.15,  -- 15% tỷ lệ trúng
    '/images/prizes/nothing.png',
    '#AA96DA',
    TRUE
);

-- ============================================
-- VERIFY DATA
-- ============================================
-- Kiểm tra tổng probability = 1.0 (100%)
SELECT 
    'Total Probability Check' as check_type,
    SUM(probability) as total_probability,
    CASE 
        WHEN SUM(probability) = 1.0 THEN 'OK ✓'
        ELSE 'ERROR: Must be 1.0 ✗'
    END as status
FROM prizes
WHERE is_active = TRUE;

-- Hiển thị danh sách prizes
SELECT 
    id,
    name,
    type,
    probability * 100 as probability_percent,
    color,
    is_active
FROM prizes
ORDER BY id;

-- Reset sequence
SELECT setval('prizes_id_seq', (SELECT MAX(id) FROM prizes), true);
SELECT setval('spin_history_id_seq', 1, false);

