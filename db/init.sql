CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,  -- Auto-increment ID starting from 1
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(1000) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Columns needed by Java entity but missing from current schema
    avatar_url VARCHAR(255),
    refresh_token VARCHAR(1000),
    
    -- Additional columns from original schema
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(255) UNIQUE,
    address VARCHAR(255),
    gender VARCHAR(10),
    provider VARCHAR(50),
    provider_id VARCHAR(255),
    email_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(100),
    verification_token_expiry TIMESTAMP
);


-- ============================================
-- DỮ LIỆU MẪU CHO USERS
-- ============================================
-- 🔐 PASSWORD HASH: $2a$12$I2iwWVAzrkWIAHWuSWRLO.HHHnhaf99xYswm2c.qiueIUBiN8IeeC
-- 📝 PLAINTEXT PASSWORD: "password" cho TẤT CẢ tài khoản
-- ✅ VERIFIED: This hash CORRECTLY matches "password" (tested with BCrypt strength 12)
-- 
-- 👤 ROLES (ENUM NAMES - NOT NUMBERS!):
--   ROOT    = Super Admin (highest privilege)
--   ADMIN   = Administrator
--   MANAGER = Manager
--   STAFF   = Staff member
--   MEMBER  = Regular member/user
--   GUEST   = Guest (lowest privilege)
-- 
-- ⚠️ QUAN TRỌNG: Database phải lưu TÊN ENUM (vd: "ADMIN") 
--    KHÔNG PHẢI SỐ (vd: "1") vì @Enumerated(EnumType.STRING)
-- ============================================

-- 1. User thường đã xác nhận email
-- Email: test@example.com | Password: password | Role: 2 (USER)
INSERT INTO users (first_name, last_name, full_name, phone, email, password_hash, address, gender, role, email_verified, is_active, avatar_url)
VALUES (
    'Test', 
    'User',
    'Test User',
    '0889251007',
    'test@example.com',
    '$2a$12$I2iwWVAzrkWIAHWuSWRLO.HHHnhaf99xYswm2c.qiueIUBiN8IeeC', -- password: password
    'Số 1 Đường ABC, Quận 1, TP.HCM',
    'Nam',
    'MEMBER', -- ✅ ROLE: MEMBER (regular user)
    TRUE, -- email đã xác minh
    TRUE,
    'uploads/avatars/defaultAvt.jpg'
)
ON CONFLICT (phone) DO NOTHING;

-- 2. User chưa xác nhận email
-- Email: pending@example.com | Password: password | Role: 2 (USER)
INSERT INTO users (first_name, last_name, full_name, phone, email, password_hash, address, gender, role, email_verified, verification_token, verification_token_expiry, is_active, avatar_url)
VALUES (
    'Pending', 
    'User',
    'Pending User',
    '0901234567',
    'pending@example.com',
    '$2a$12$I2iwWVAzrkWIAHWuSWRLO.HHHnhaf99xYswm2c.qiueIUBiN8IeeC', -- password: password
    'Số 2 Đường XYZ, Quận 2, TP.HCM',
    'Nữ',
    'MEMBER', -- ✅ ROLE: MEMBER (regular user)
    FALSE, -- chưa xác minh email
    'test-verification-token-123',
    NOW() + INTERVAL '24 HOURS',
    TRUE,
    'uploads/avatars/defaultAvt.jpg'
)
ON CONFLICT (phone) DO NOTHING;

-- 3. Admin
-- Email: admin@example.com | Password: password | Role: ADMIN
-- ✅ FIXED: Using correct BCrypt hash
INSERT INTO users (first_name, last_name, full_name, phone, email, password_hash, address, gender, role, email_verified, is_active, avatar_url)
VALUES (
    'Admin', 
    'User',
    'Admin User',
    '0909876543',
    'admin@example.com',
    '$2a$12$I2iwWVAzrkWIAHWuSWRLO.HHHnhaf99xYswm2c.qiueIUBiN8IeeC', -- password: password (VERIFIED CORRECT)
    'Số 3 Đường DEF, Quận 3, TP.HCM',
    'Nam',
    'ADMIN', -- ✅ ROLE: ADMIN
    TRUE,
    TRUE,
    'uploads/avatars/defaultAvt.jpg'
)
ON CONFLICT (phone) DO NOTHING;

-- 4. Root user (Super admin)
-- Email: root@example.com | Password: password | Role: ROOT
-- ✅ FIXED: Using correct BCrypt hash
INSERT INTO users (first_name, last_name, full_name, phone, email, password_hash, address, gender, role, email_verified, is_active, avatar_url)
VALUES (
    'Root', 
    'User',
    'Root User',
    '0912345678',
    'root@example.com',
    '$2a$12$I2iwWVAzrkWIAHWuSWRLO.HHHnhaf99xYswm2c.qiueIUBiN8IeeC', -- password: password (VERIFIED CORRECT)
    'Số 4 Đường GHI, Quận 4, TP.HCM',
    'Nam',
    'ROOT', -- ✅ ROLE: ROOT (Super Admin)
    TRUE,
    TRUE,
    'uploads/avatars/defaultAvt.jpg'
)
ON CONFLICT (phone) DO NOTHING;

-- 5. Người dùng đã đăng ký qua Google
-- Email: google.user@gmail.com | Password: (OAuth - no password) | Role: 2 (USER)
INSERT INTO users (first_name, last_name, full_name, phone, email, password_hash, address, gender, role, provider, provider_id, email_verified, is_active, avatar_url)
VALUES (
    'Google', 
    'User',
    'Google User',
    '0978123456',
    'google.user@gmail.com',
    '$2a$12$I2iwWVAzrkWIAHWuSWRLO.HHHnhaf99xYswm2c.qiueIUBiN8IeeC', -- password: password (dummy, not used for OAuth)
    'Số 5 Đường JKL, Quận 5, TP.HCM',
    'Nữ',
    'MEMBER', -- ✅ ROLE: MEMBER (regular user)
    'google',
    'google-oauth2-123456789',
    TRUE,
    TRUE,
    'uploads/avatars/defaultAvt.jpg'
)
ON CONFLICT (phone) DO NOTHING;

-- 6. Tài khoản bị vô hiệu hóa
-- Email: disabled@example.com | Password: password | Role: 2 (USER) | is_active: FALSE
INSERT INTO users (first_name, last_name, full_name, phone, email, password_hash, address, gender, role, email_verified, is_active, avatar_url)
VALUES (
    'Disabled', 
    'Account',
    'Disabled Account',
    '0932123456',
    'disabled@example.com',
    '$2a$12$I2iwWVAzrkWIAHWuSWRLO.HHHnhaf99xYswm2c.qiueIUBiN8IeeC', -- password: password
    'Số 6 Đường MNO, Quận 6, TP.HCM',
    'Nam',
    'MEMBER', -- ✅ ROLE: MEMBER (regular user)
    TRUE,
    FALSE, -- ⚠️ Account disabled
    'uploads/avatars/defaultAvt.jpg'
)
ON CONFLICT (phone) DO NOTHING;

-- ============================================
-- LEGACY USERS (từ data.sql cũ)
-- ============================================

-- 7. DangKhoa 
-- Email: dangkhoa@example.com | Password: password | Role: ADMIN
-- ✅ FIXED: Using correct BCrypt hash
INSERT INTO users (full_name, email, password_hash, role, is_active, created_at, updated_at, avatar_url)
VALUES ('DangKhoa', 'dangkhoa@example.com',
        '$2a$12$I2iwWVAzrkWIAHWuSWRLO.HHHnhaf99xYswm2c.qiueIUBiN8IeeC', -- password: password (VERIFIED CORRECT)
        'ADMIN', -- ✅ ROLE: ADMIN
        TRUE, NOW(), NOW(), 'uploads/avatars/defaultAvt.jpg')
ON CONFLICT (email) DO NOTHING;

-- 8. TuanKiet
-- Email: tuankiet@example.com | Password: password | Role: MEMBER
-- ✅ FIXED: Using correct BCrypt hash
INSERT INTO users (full_name, email, password_hash, role, is_active, created_at, updated_at, avatar_url)
VALUES ('TuanKiet', 'tuankiet@example.com',
        '$2a$12$I2iwWVAzrkWIAHWuSWRLO.HHHnhaf99xYswm2c.qiueIUBiN8IeeC', -- password: password (VERIFIED CORRECT)
        'MEMBER', -- ✅ ROLE: MEMBER (regular user)
        TRUE, NOW(), NOW(), 'uploads/avatars/defaultAvt.jpg')
ON CONFLICT (email) DO NOTHING;

-- ============================================
-- RESET SEQUENCE TO START FROM 1 FOR NEW USERS
-- ============================================
-- Reset sequence to 1 so new users will start with ID = 1, 2, 3...
SELECT setval('users_id_seq', 1, false);

