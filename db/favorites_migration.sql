-- Migration: Tạo bảng favorites
-- Created: 2025-12-18

CREATE TABLE IF NOT EXISTS favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_product FOREIGN KEY (product_id) 
        REFERENCES product(id) ON DELETE CASCADE,
    
    -- Unique constraint: Mỗi user chỉ có thể thích 1 product 1 lần
    CONSTRAINT uk_user_product UNIQUE (user_id, product_id)
);

-- Index để tăng tốc truy vấn
CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_product_id ON favorites(product_id);
CREATE INDEX IF NOT EXISTS idx_favorites_created_at ON favorites(created_at DESC);

-- Comment
COMMENT ON TABLE favorites IS 'Bảng lưu trữ sản phẩm yêu thích của người dùng';
COMMENT ON COLUMN favorites.user_id IS 'ID người dùng';
COMMENT ON COLUMN favorites.product_id IS 'ID sản phẩm yêu thích';
