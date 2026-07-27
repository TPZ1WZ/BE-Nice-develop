-- Migration: Thêm snapshot thông tin sản phẩm vào OrderItem
-- Mục đích: Giữ nguyên thông tin sản phẩm tại thời điểm mua, không bị thay đổi khi admin sửa sản phẩm

-- Thêm các cột mới
ALTER TABLE order_item 
ADD COLUMN IF NOT EXISTS product_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS product_image TEXT;

-- Cập nhật dữ liệu cũ: copy thông tin từ Product hiện tại vào snapshot
UPDATE order_item oi
SET 
    product_name = p.name,
    product_image = p.images[1]  -- Lấy ảnh đầu tiên từ array images
FROM product p
WHERE oi.product_id = p.id
  AND (oi.product_name IS NULL OR oi.product_image IS NULL);

-- Kiểm tra kết quả
SELECT 
    oi.id,
    oi.product_id,
    oi.product_name,
    oi.product_image,
    p.name as current_product_name
FROM order_item oi
LEFT JOIN product p ON oi.product_id = p.id
LIMIT 10;
