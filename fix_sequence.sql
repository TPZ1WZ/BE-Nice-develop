-- Fix sequence for all tables
-- This resets the sequence to start from the correct value

-- Fix users table sequence
SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 0) + 1, false);

-- Fix other tables that might have the same issue
SELECT setval('categories_id_seq', COALESCE((SELECT MAX(id) FROM categories), 0) + 1, false);
SELECT setval('products_id_seq', COALESCE((SELECT MAX(id) FROM products), 0) + 1, false);
SELECT setval('orders_id_seq', COALESCE((SELECT MAX(id) FROM orders), 0) + 1, false);
SELECT setval('order_items_id_seq', COALESCE((SELECT MAX(id) FROM order_items), 0) + 1, false);
SELECT setval('cart_items_id_seq', COALESCE((SELECT MAX(id) FROM cart_items), 0) + 1, false);
SELECT setval('reviews_id_seq', COALESCE((SELECT MAX(id) FROM reviews), 0) + 1, false);
SELECT setval('addresses_id_seq', COALESCE((SELECT MAX(id) FROM addresses), 0) + 1, false);

-- Verify current sequences
SELECT 'users_id_seq' as sequence_name, last_value FROM users_id_seq
UNION ALL
SELECT 'categories_id_seq', last_value FROM categories_id_seq
UNION ALL
SELECT 'products_id_seq', last_value FROM products_id_seq
UNION ALL
SELECT 'orders_id_seq', last_value FROM orders_id_seq;
