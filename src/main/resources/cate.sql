
-- Thêm dữ liệu mẫu cho bảng category
INSERT INTO category (is_delete, created_at, updated_at, description, name)
SELECT false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       'Nam.', 'Nam'
    WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Nam');

INSERT INTO category (is_delete, created_at, updated_at, description, name)
SELECT false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       'Nữ.', 'Nữ'
    WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Nữ');

INSERT INTO category (is_delete, created_at, updated_at, description, name)
SELECT false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       'Trẻ em.', 'Trẻ em'
    WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Trẻ em');

INSERT INTO category (is_delete, created_at, updated_at, description, name)
SELECT false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       'Khác.', 'Khác'
    WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Khác');
