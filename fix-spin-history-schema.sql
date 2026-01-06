-- Fix spin_history schema: Cho phép prize_id NULL
-- Vì prize_id không còn được sử dụng trong code
ALTER TABLE spin_history ALTER COLUMN prize_id DROP NOT NULL;

-- Hoặc xóa luôn cột prize_id nếu không cần:
-- ALTER TABLE spin_history DROP COLUMN prize_id;
