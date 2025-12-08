-- Insert Nike product categories
INSERT INTO public.category (id, created_at, updated_at, name) VALUES 
(1, now(), now(), 'Giày thể thao'),
(2, now(), now(), 'Giày chạy bộ'),
(3, now(), now(), 'Giày bóng đá'),
(4, now(), now(), 'Giày bóng rổ'),
(5, now(), now(), 'Giày trẻ em'),
(6, now(), now(), 'Phụ kiện')
ON CONFLICT (id) DO NOTHING;

-- Insert Nike products
INSERT INTO public.product (id, created_at, updated_at, description, images, is_delete, name, price, slug, stock, sub_title, category_id) VALUES 
-- Nike Air Max shoes
(1, now(), now(), 'Giày Nike Air Max 90 với thiết kế cổ điển và đệm khí Max Air mang lại sự thoải mái tối đa cho mọi bước chân.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/fd17b420-b388-4c8a-aaaa-e0a98ddf175f/air-max-90-shoes-6n3vKB.png"]', false, 'Nike Air Max 90', 3200000, 'nike-air-max-90', 25, 'Giày thể thao nam nữ', 1),

-- Nike Air Force 1
(2, now(), now(), 'Nike Air Force 1 - biểu tượng bất hủ của thời trang đường phố với thiết kế cổ điển và chất lượng vượt trội.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/4f37fca8-6bce-43e7-ad07-f57ae3c13142/air-force-1-07-shoes-WrLlWX.png"]', false, 'Nike Air Force 1 ''07', 2800000, 'nike-air-force-1-07', 30, 'Giày thể thao nam nữ', 1),

-- Nike React running shoes
(3, now(), now(), 'Nike React Infinity Run với công nghệ đệm React mang lại cảm giác nhẹ nhàng và năng lượng cho mỗi bước chạy.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/8439f823-86cf-4086-81d2-4f9ff9a66866/react-infinity-run-flyknit-3-road-running-shoes-XJFKqC.png"]', false, 'Nike React Infinity Run 3', 4200000, 'nike-react-infinity-run-3', 20, 'Giày chạy bộ nam nữ', 2),

-- Nike Mercurial football boots
(4, now(), now(), 'Nike Mercurial Superfly với thiết kế nhẹ và khả năng bám sân tốt, giúp bạn tỏa sáng trên sân cỏ.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/98ca22f3-2dce-4df0-bb48-658145a0c9b7/mercurial-superfly-9-elite-fg-football-boot-VBRvgV.png"]', false, 'Nike Mercurial Superfly 9 Elite', 6500000, 'nike-mercurial-superfly-9-elite', 15, 'Giày bóng đá chuyên nghiệp', 3),

-- Nike Jordan basketball shoes
(5, now(), now(), 'Air Jordan 1 Retro High - đôi giày bóng rổ huyền thoại với thiết kế cổ điển và phong cách không thể nhầm lẫn.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/34cd5c7b-72bb-4a1e-9b69-0e4cd0151121/air-jordan-1-retro-high-og-shoes-Pz8n5Y.png"]', false, 'Air Jordan 1 Retro High OG', 4800000, 'air-jordan-1-retro-high-og', 18, 'Giày bóng rổ nam', 4),

-- Nike kids shoes
(6, now(), now(), 'Nike Air Max 270 cho trẻ em với thiết kế bắt mắt và đệm khí lớn, tạo sự thoải mái cho các bé hoạt động.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/63b1a4a4-6e8e-4a70-9e66-d5c6c1f8d49e/air-max-270-big-kids-shoes-J9xK8M.png"]', false, 'Nike Air Max 270 Kids', 2200000, 'nike-air-max-270-kids', 35, 'Giày thể thao trẻ em', 5),

-- Nike accessories
(7, now(), now(), 'Ba lô Nike Brasilia với thiết kế tiện dụng, nhiều ngăn để đồ và chất liệu bền bỉ phù hợp cho học tập và thể thao.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/b28c8ede-1d9f-423b-b0ac-7b03c4a25d4e/brasilia-9-5-training-backpack-large-26l-K5KHMP.png"]', false, 'Nike Brasilia Backpack', 890000, 'nike-brasilia-backpack', 50, 'Ba lô thể thao', 6),

-- More Nike shoes
(8, now(), now(), 'Nike Dunk Low với thiết kế retro và nhiều phối màu đa dạng, là lựa chọn hoàn hảo cho phong cách streetwear.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/af53d53e-03c6-4fb8-8cc1-964f5c1f97cf/dunk-low-shoes-5SNPR8.png"]', false, 'Nike Dunk Low', 3500000, 'nike-dunk-low', 28, 'Giày thể thao nam nữ', 1),

(9, now(), now(), 'Nike Zoom Fly 5 với đế carbon giúp tăng độ đàn hồi và hiệu suất chạy, phù hợp cho các runner chuyên nghiệp.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/e777c881-5b62-4250-92a6-362967f54cca/zoom-fly-5-road-running-shoes-JZmSLk.png"]', false, 'Nike Zoom Fly 5', 4800000, 'nike-zoom-fly-5', 22, 'Giày chạy bộ performance', 2),

(10, now(), now(), 'Nike Phantom GT2 với thiết kế ôm chân và khả năng kiểm soát bóng tuyệt vời cho các cầu thủ sáng tạo.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/726f3a56-7d45-4076-9b66-1c11b2bae09a/phantom-gt2-elite-fg-football-boot-TJsqKv.png"]', false, 'Nike Phantom GT2 Elite', 5800000, 'nike-phantom-gt2-elite', 12, 'Giày bóng đá cao cấp', 3),

-- Nike Blazer Mid
(11, now(), now(), 'Nike Blazer Mid với thiết kế cổ cao cổ điển, phong cách vintage kết hợp hiện đại hoàn hảo cho mọi outfit.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/ce8f9c5b-6b93-4b6a-9c90-d3ff7f2c2c5e/blazer-mid-77-vintage-shoes-qw85rK.png"]', false, 'Nike Blazer Mid ''77 Vintage', 2900000, 'nike-blazer-mid-77-vintage', 24, 'Giày lifestyle nam nữ', 1),

-- Nike Pegasus running shoes
(12, now(), now(), 'Nike Air Zoom Pegasus 40 - đôi giày chạy bộ tin cậy với đệm Zoom Air và thiết kế thoải mái cho mọi khoảng cách.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/fb7eda3e-5ac8-4d6d-9c28-1ba7cfb6e4a0/air-zoom-pegasus-40-road-running-shoes-PQlM6K.png"]', false, 'Nike Air Zoom Pegasus 40', 3800000, 'nike-air-zoom-pegasus-40', 30, 'Giày chạy bộ hàng ngày', 2),

-- Nike Tiempo football boots
(13, now(), now(), 'Nike Tiempo Legend 10 với da kangaroo cao cấp và thiết kế truyền thống, mang lại cảm giác chạm bóng tuyệt vời.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/a8a0e1a3-c7d5-4a3f-8e9f-3b2c1d0a9b8c/tiempo-legend-10-elite-fg-football-boot-XqWpJk.png"]', false, 'Nike Tiempo Legend 10 Elite', 6200000, 'nike-tiempo-legend-10-elite', 10, 'Giày bóng đá da thật', 3),

-- Nike Air Jordan 4
(14, now(), now(), 'Air Jordan 4 Retro với thiết kế mesh và các chi tiết wing, một trong những mẫu Jordan được yêu thích nhất.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/6c1e4b8e-2a5c-4d6f-9e8a-5f3b2a1c9d7e/air-jordan-4-retro-shoes-NkJV6L.png"]', false, 'Air Jordan 4 Retro', 5200000, 'air-jordan-4-retro', 16, 'Giày bóng rổ retro', 4),

-- Nike Free running shoes for kids
(15, now(), now(), 'Nike Free RN 5.0 Kids với đế giày linh hoạt giúp bàn chân trẻ em phát triển tự nhiên khi chạy và vận động.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/9d7e6c5b-4a3f-4e8d-9c2a-6f1e4b8a7c5d/free-rn-5-0-2023-big-kids-road-running-shoes-WqXpYk.png"]', false, 'Nike Free RN 5.0 Kids', 1950000, 'nike-free-rn-5-kids', 40, 'Giày chạy bộ trẻ em', 5),

-- Nike accessories - Cap
(16, now(), now(), 'Nón Nike Dri-FIT với công nghệ thấm hút mồ hôi và thiết kế thể thao, phù hợp cho mọi hoạt động ngoài trời.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/2f5e8c9b-6a4d-4e7f-8c1a-9d6f3b2e5a8c/dri-fit-legacy91-cap-QrXpMk.png"]', false, 'Nike Dri-FIT Legacy91 Cap', 650000, 'nike-dri-fit-legacy91-cap', 60, 'Nón thể thao', 6),

-- Nike React Element
(17, now(), now(), 'Nike React Element 55 với thiết kế futuristic và đế React siêu nhẹ, tạo nên phong cách streetwear độc đáo.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/8e7f9c6a-5b4d-4f8e-9a2c-6e1f4b7a8c5d/react-element-55-shoes-TqWpZk.png"]', false, 'Nike React Element 55', 4100000, 'nike-react-element-55', 20, 'Giày lifestyle nam nữ', 1),

-- Nike Court Vision
(18, now(), now(), 'Nike Court Vision Low với thiết kế lấy cảm hứng từ bóng rổ thập niên 80, phong cách cổ điển và giá cả phải chăng.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/7c9e6f5a-4b3d-4e8f-9a1c-6d2f5b8a7c4e/court-vision-low-shoes-MrXpLk.png"]', false, 'Nike Court Vision Low', 2100000, 'nike-court-vision-low', 35, 'Giày thể thao retro', 1),

-- Nike Flex running shoes
(19, now(), now(), 'Nike Flex Experience Run 12 thiết kế linh hoạt và nhẹ nhàng, hoàn hảo cho người mới bắt đầu chạy bộ.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/9e8f7c6b-5a4d-4f9e-8b2c-7e1f5a8b6c4d/flex-experience-run-12-road-running-shoes-LqWpXk.png"]', false, 'Nike Flex Experience Run 12', 2600000, 'nike-flex-experience-run-12', 28, 'Giày chạy bộ cơ bản', 2),

-- Nike accessories - Socks
(20, now(), now(), 'Tất Nike Everyday Cushion với đệm êm ái và chất liệu Dri-FIT thấm hút mồ hôi, thoải mái suốt cả ngày.', '["https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/6f8e9d7c-4b5a-4f9e-8c2d-7a1f6b9a5c8e/everyday-cushion-ankle-socks-6-pack-JqXpMk.png"]', false, 'Nike Everyday Cushion Socks (6-Pack)', 480000, 'nike-everyday-cushion-socks-6-pack', 80, 'Tất thể thao 6 đôi', 6)

ON CONFLICT (id) DO NOTHING;