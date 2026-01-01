-- Fix duplicate categories
-- Giữ lại category có ID nhỏ nhất, xóa các category trùng lặp

BEGIN;

-- 1. Cập nhật các sản phẩm từ ADDIDAS trùng (12, 13, 15) sang ADDIDAS chính (9)
UPDATE product SET category_id = 9 WHERE category_id IN (12, 13, 15);

-- 2. Cập nhật các sản phẩm từ "Giày trẻ em" trùng (4, 5, 8, 10) sang "Giày trẻ em" chính (1)
UPDATE product SET category_id = 1 WHERE category_id IN (4, 5, 8, 10);

-- 3. Cập nhật các sản phẩm từ "Phụ kiện" trùng (7, 11, 14, 16, 17) sang "Phụ kiện" chính (6)
UPDATE product SET category_id = 6 WHERE category_id IN (7, 11, 14, 16, 17);

-- 4. Đánh dấu xóa các category trùng lặp
UPDATE category SET is_delete = true WHERE id IN (4, 5, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17);

-- 5. Cập nhật description cho các category chính nếu cần
UPDATE category SET description = 'Giày dành cho trẻ em' WHERE id = 1 AND (description IS NULL OR description = '');
UPDATE category SET description = 'Phụ kiện thể thao' WHERE id = 6 AND (description IS NULL OR description = '');
UPDATE category SET description = 'Sản phẩm Adidas' WHERE id = 9 AND name = 'ADDIDAS';

-- 6. Kiểm tra kết quả
SELECT 'Categories còn lại:' as info;
SELECT id, name, description, is_delete FROM category WHERE is_delete = false ORDER BY id;

SELECT 'Số lượng sản phẩm theo category:' as info;
SELECT c.id, c.name, COUNT(p.id) as product_count 
FROM category c 
LEFT JOIN product p ON c.id = p.category_id 
WHERE c.is_delete = false 
GROUP BY c.id, c.name 
ORDER BY c.id;

COMMIT;
