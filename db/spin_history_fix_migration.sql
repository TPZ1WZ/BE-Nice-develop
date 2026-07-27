-- =====================================================
-- Lucky Wheel - Spin History Schema Fix Migration
-- =====================================================
-- Mục đích: Fix lỗi "null value in column prize_id violates not-null constraint"
-- Nguyên nhân: SpinHistory entity không sử dụng prize_id nữa, nhưng DB column vẫn có constraint NOT NULL
-- 
-- QUAN TRỌNG: Chạy migration này SAU KHI đã chạy lucky_wheel_migration.sql
--
-- Cách 1: Chạy SQL trực tiếp (khuyến nghị nếu có psql)
-- Cách 2: Gọi API POST /api/v1/admin/lucky-wheel/migration/fix-spin-history-schema
-- =====================================================

-- Cho phép prize_id NULL (giữ lại column để tương thích với DB cũ)
ALTER TABLE spin_history ALTER COLUMN prize_id DROP NOT NULL;

-- ===== HOẶC ===== 
-- Xóa hoàn toàn column prize_id nếu muốn cleanup (không khuyến nghị):
-- ALTER TABLE spin_history DROP COLUMN prize_id;

-- =====================================================
-- Sau khi chạy migration này, vòng quay sẽ hoạt động bình thường
-- =====================================================
