# Module Review - Hệ thống Đánh giá Sản phẩm

## Bảng tóm tắt Use Cases

| **Chức năng** | **Người dùng** | **Mô tả** |
|---------------|----------------|-----------|
| **Tạo đánh giá sản phẩm** | Khách hàng đã mua | Khách hàng đánh giá sản phẩm từ 1-5 sao, viết bình luận và upload hình ảnh minh họa |
| **Xem danh sách đánh giá** | Guest, Khách hàng | Người dùng xem tất cả đánh giá của sản phẩm với các tùy chọn lọc và sắp xếp |
| **Lọc và tìm kiếm đánh giá** | Guest, Khách hàng | Lọc theo số sao, có ảnh, đã xác minh mua hàng; tìm kiếm theo từ khóa trong bình luận |
| **Cập nhật đánh giá** | Khách hàng | Khách hàng chỉnh sửa đánh giá của chính mình (rating, comment, images) |
| **Trả lời đánh giá** | Khách hàng, Admin | Khách hàng và Admin có thể trả lời các đánh giá để tương tác |
| **Duyệt đánh giá** | Admin, Root | Admin/Root duyệt hoặc từ chối đánh giá, ẩn những đánh giá vi phạm |
| **Quản lý đánh giá chờ duyệt** | Admin, Root | Xem danh sách các đánh giá chưa được duyệt để kiểm tra và phê duyệt |
| **Trả lời chính thức** | Admin, Root | Admin/Root trả lời chính thức các đánh giá thay mặt cửa hàng |
| **Xóa/ẩn phản hồi** | Admin, Root | Quản lý các phản hồi không phù hợp, xóa hoặc ẩn nội dung vi phạm |
| **Thống kê đánh giá** | Guest, Admin | Xem tổng quan rating trung bình, phân phối số sao, số lượng đánh giá có ảnh |
| **Sắp xếp đánh giá** | Guest, Khách hàng | Sắp xếp đánh giá theo mới nhất, rating cao/thấp, hữu ích nhất |

## Entities

### 1. Review (Đánh giá)
- **ID**: Khóa chính
- **Product ID**: Sản phẩm được đánh giá
- **User ID**: Người đánh giá
- **Rating**: Điểm từ 1-5 sao
- **Title**: Tiêu đề đánh giá (tùy chọn)
- **Comment**: Nội dung bình luận (tối đa 1000 ký tự)
- **Is Verified Purchase**: Đã mua hàng xác thực
- **Is Approved**: Đã được duyệt
- **Is Hidden**: Đã bị ẩn
- **Helpful Count**: Số lượt đánh giá hữu ích
- **Reported Count**: Số lượt báo cáo vi phạm

### 2. ReviewReply (Phản hồi đánh giá)
- **ID**: Khóa chính
- **Review ID**: Đánh giá được trả lời
- **User ID**: Người trả lời
- **Is Admin**: Có phải Admin trả lời không
- **Content**: Nội dung phản hồi (tối đa 1000 ký tự)
- **Is Hidden**: Đã bị ẩn

### 3. ReviewImage (Hình ảnh đánh giá)
- **ID**: Khóa chính
- **Review ID**: Đánh giá chứa ảnh
- **Image URL**: Đường dẫn ảnh
- **Caption**: Chú thích ảnh (tùy chọn)
- **Display Order**: Thứ tự hiển thị

## API Endpoints

### Public APIs (`/api/v1/reviews`)
- `POST /` - Tạo đánh giá mới
- `GET /product/{productId}` - Lấy đánh giá theo sản phẩm
- `POST /filter` - Lọc đánh giá theo tiêu chí
- `GET /product/{productId}/summary` - Thống kê tổng quan
- `GET /product/{productId}/search` - Tìm kiếm theo từ khóa
- `GET /product/{productId}/sorted` - Sắp xếp đánh giá
- `PUT /{reviewId}` - Cập nhật đánh giá của user
- `POST /{reviewId}/replies` - Tạo phản hồi
- `GET /{reviewId}/replies` - Lấy danh sách phản hồi

### Admin APIs (`/api/v1/admin/reviews`)
- `GET /` - Xem tất cả đánh giá (bao gồm chưa duyệt)
- `GET /pending` - Lấy đánh giá chờ duyệt
- `POST /filter` - Lọc đánh giá với quyền Admin
- `PUT /{reviewId}/status` - Cập nhật trạng thái duyệt
- `POST /{reviewId}/admin-reply` - Trả lời chính thức
- `PUT /replies/{replyId}/hide` - Ẩn phản hồi
- `DELETE /replies/{replyId}` - Xóa phản hồi

## Tính năng chính

### 1. **Hệ thống đánh giá đa dạng**
- Rating 1-5 sao với validation
- Tiêu đề và bình luận chi tiết
- Upload nhiều ảnh minh họa
- Xác thực người mua hàng

### 2. **Quản lý nội dung**
- Hệ thống duyệt trước khi hiển thị
- Ẩn/hiện đánh giá vi phạm
- Báo cáo đánh giá không phù hợp
- Đếm lượt đánh giá hữu ích

### 3. **Tương tác xã hội**
- Trả lời đánh giá (user & admin)
- Phân biệt phản hồi chính thức
- Hệ thống ẩn/xóa phản hồi

### 4. **Tìm kiếm và lọc nâng cao**
- Lọc theo số sao, có ảnh, đã xác minh
- Tìm kiếm full-text trong nội dung
- Sắp xếp đa dạng (mới, rating, helpful)

### 5. **Thống kê và phân tích**
- Rating trung bình
- Phân phối số sao (1★-5★)
- Tỷ lệ đánh giá có ảnh
- Số lượng mua hàng xác thực

## Business Rules

1. **Mỗi user chỉ được đánh giá 1 lần/sản phẩm**
2. **Đánh giá phải được duyệt trước khi hiển thị công khai**
3. **Chỉ owner mới được sửa đánh giá của mình**
4. **Admin có thể trả lời chính thức mỗi đánh giá tối đa 1 lần**
5. **Hình ảnh được lưu theo thứ tự hiển thị**
6. **Đánh giá bị ẩn vẫn tồn tại trong database**

## Validation

- **Rating**: Bắt buộc, từ 1-5
- **Title**: Tùy chọn, tối đa 255 ký tự
- **Comment**: Tùy chọn, tối đa 1000 ký tự  
- **Images**: Tùy chọn, mảng URL hợp lệ
- **Reply Content**: Bắt buộc, tối đa 1000 ký tự

## Security

- **Authentication**: Yêu cầu đăng nhập để tạo/sửa đánh giá
- **Authorization**: 
  - User chỉ sửa được đánh giá của mình
  - Admin/Root có full quyền quản lý
- **Rate Limiting**: Giới hạn số lượng đánh giá/thời gian
- **Content Filtering**: Kiểm tra nội dung trước khi hiển thị