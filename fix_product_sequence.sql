-- Fix product sequence để tránh lỗi duplicate key
-- Chạy file này trong PostgreSQL

-- Reset sequence về giá trị max hiện tại
SELECT setval('product_id_seq', COALESCE((SELECT MAX(id) FROM product), 1), true);

-- Kiểm tra sequence hiện tại
SELECT currval('product_id_seq') as current_sequence_value;

-- Kiểm tra max ID trong table
SELECT MAX(id) as max_product_id FROM product;
