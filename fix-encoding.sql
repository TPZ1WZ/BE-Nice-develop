-- Fix encoding cho database Nike Store

-- 1. Fix bảng product
UPDATE product 
SET 
  name = regexp_replace(name, 'Gi├íy', 'Giày', 'g'),
  name = regexp_replace(name, 'trß║╗', 'trẻ', 'g'),
  name = regexp_replace(name, 'em', 'em', 'g'),
  name = regexp_replace(name, 'Phß╗Ñ', 'Phụ', 'g'),
  name = regexp_replace(name, 'kiß╗çn', 'kiện', 'g'),
  description = regexp_replace(description, 'Gi├íy', 'Giày', 'g'),
  description = regexp_replace(description, 'trß║╗', 'trẻ', 'g');

-- 2. Fix bảng category
UPDATE category 
SET 
  name = regexp_replace(name, 'Gi├íy', 'Giày', 'g'),
  name = regexp_replace(name, 'trß║╗', 'trẻ', 'g'),
  name = regexp_replace(name, 'Phß╗Ñ', 'Phụ', 'g'),
  description = regexp_replace(description, 'Gi├íy', 'Giày', 'g');

-- 3. Kiểm tra kết quả
SELECT 'Products:' as table_name, name FROM product LIMIT 5
UNION ALL
SELECT 'Categories:' as table_name, name FROM category LIMIT 5;
