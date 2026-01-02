# 🔄 AUTO-SYNC CHATBOT VỚI SẢN PHẨM

## 📋 Tổng quan

Hệ thống **tự động đồng bộ** giữa Product Database và Chatbot Vector Database. Mỗi khi có thay đổi sản phẩm (tạo/sửa/xóa), chatbot sẽ **tự động cập nhật** ngay lập tức.

## ⚙️ Cơ chế hoạt động

```
┌─────────────────┐
│  Admin/API      │
│  Tạo/Sửa/Xóa   │
│  Sản phẩm      │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│  AdminProductService    │
│  - createProduct()      │
│  - updateProduct()      │
│  - deleteProduct()      │
└────────┬────────────────┘
         │ Publish Event
         ▼
┌─────────────────────────┐
│  ProductEventListener   │
│  @TransactionalEvent    │
└────────┬────────────────┘
         │ Auto Sync
         ▼
┌─────────────────────────┐
│  Vector Database        │
│  (Chatbot Knowledge)    │
└─────────────────────────┘
```

## ✨ Tính năng

### 1. **Tự động Index khi tạo sản phẩm mới**
```java
// Khi admin tạo sản phẩm mới
Product product = adminProductService.createProduct(newProduct);

// ✅ Chatbot tự động index sản phẩm vào vector DB
// Không cần gọi API seed nữa!
```

### 2. **Tự động cập nhật khi sửa sản phẩm**
```java
// Khi admin cập nhật giá, tên, mô tả, v.v.
adminProductService.updateProduct(productId, updatedData);

// ✅ Chatbot tự động xóa data cũ và index lại data mới
// Luôn đảm bảo thông tin mới nhất!
```

### 3. **Tự động xóa khi xóa sản phẩm**
```java
// Khi admin xóa sản phẩm
adminProductService.deleteProduct(productId);

// ✅ Chatbot tự động xóa sản phẩm khỏi vector DB
// Không còn trả lời về sản phẩm đã xóa!
```

## 📂 File code

### 1. ProductEventListener.java
`src/main/java/com/proj/webprojrct/chatbot/listener/ProductEventListener.java`

- Lắng nghe các event về sản phẩm
- Xử lý đồng bộ với vector database
- Log chi tiết mọi thao tác

### 2. AdminProductService.java (đã cập nhật)
`src/main/java/com/proj/webprojrct/admin/service/AdminProductService.java`

- Thêm `ApplicationEventPublisher` để publish events
- Tự động trigger events sau mỗi thao tác

## 🚀 Cách sử dụng

### Cho Developer:

**Không cần làm gì thêm!** 

Khi bạn sử dụng `AdminProductService` để thao tác sản phẩm, chatbot sẽ tự động đồng bộ:

```java
@Autowired
private AdminProductService productService;

// Tạo sản phẩm - tự động sync chatbot
Product product = productService.createProduct(newProduct);

// Cập nhật sản phẩm - tự động sync chatbot  
productService.updateProduct(id, updatedData);

// Xóa sản phẩm - tự động sync chatbot
productService.deleteProduct(id);
```

### Cho Admin/User:

1. **Tạo sản phẩm mới trên Admin Dashboard**
   - ✅ Chatbot biết ngay lập tức về sản phẩm mới
   - ✅ Có thể hỏi chatbot về sản phẩm vừa tạo

2. **Cập nhật thông tin sản phẩm**
   - ✅ Chatbot cập nhật thông tin mới nhất
   - ✅ Trả lời đúng giá, stock, mô tả mới

3. **Xóa sản phẩm**
   - ✅ Chatbot không còn gợi ý sản phẩm đã xóa
   - ✅ Dữ liệu luôn sạch và chính xác

## 📊 Log monitoring

Hệ thống log chi tiết mọi hoạt động:

```log
🔔 Product Created Event: Đang index sản phẩm ID 42 vào chatbot...
✅ Đã index sản phẩm ID 42 vào chatbot thành công

🔔 Product Updated Event: Đang cập nhật sản phẩm ID 42 trong chatbot...
✅ Đã cập nhật sản phẩm ID 42 trong chatbot thành công

🔔 Product Deleted Event: Đang xóa sản phẩm ID 42 khỏi chatbot...
✅ Đã xóa sản phẩm ID 42 khỏi chatbot thành công
```

## 🔧 API Seed thủ công (backup)

Nếu cần reseed toàn bộ (VD: sau khi import bulk data):

```bash
POST http://localhost:8080/api/v1/chat/admin/seed-products
```

## ⚡ Performance

- **Async Processing**: Events xử lý sau khi transaction commit
- **Non-blocking**: Không ảnh hưởng đến response time của API
- **Error Handling**: Nếu sync thất bại, API vẫn thành công
- **Retry**: Có thể retry bằng API seed thủ công

## 🎯 Best Practices

### ✅ DO:
- Sử dụng `AdminProductService` để thao tác sản phẩm
- Kiểm tra log để đảm bảo sync thành công
- Định kỳ verify chatbot có data đúng

### ❌ DON'T:
- Không trực tiếp `productRepository.save()` (bỏ qua event)
- Không bulk update/delete trực tiếp database
- Nếu phải bulk operations, nhớ gọi API seed sau đó

## 🧪 Testing

### Test tạo sản phẩm:
```bash
# 1. Tạo sản phẩm mới qua API/Admin
POST /api/v1/products
{
  "name": "Nike Test Shoe",
  "price": 1500000
}

# 2. Test chatbot ngay lập tức
POST /api/v1/chat
{
  "message": "Có giày Nike Test Shoe không?"
}

# ✅ Chatbot phải biết sản phẩm vừa tạo
```

### Test cập nhật sản phẩm:
```bash
# 1. Cập nhật giá sản phẩm
PATCH /api/v1/products/42
{
  "price": 2000000
}

# 2. Hỏi chatbot về giá
POST /api/v1/chat
{
  "message": "Giá sản phẩm ID 42 bao nhiêu?"
}

# ✅ Chatbot phải trả lời giá mới (2,000,000đ)
```

## 🐛 Troubleshooting

### Chatbot không cập nhật sau khi thay đổi sản phẩm?

1. **Kiểm tra log**: Tìm `Product Created/Updated/Deleted Event` trong log
2. **Verify event fired**: Đảm bảo dùng `AdminProductService`, không phải repository trực tiếp
3. **Check transaction**: Event chỉ fire sau khi transaction commit thành công
4. **Fallback**: Gọi API seed thủ công để resync

### Sản phẩm bị duplicate trong chatbot?

```bash
# Xóa toàn bộ và reseed
POST /api/v1/chat/admin/reseed-all
```

## 📝 Notes

- Event listener hoạt động **AFTER_COMMIT** để đảm bảo data consistency
- Nếu event listener fail, không ảnh hưởng đến API response
- Vector database updates là **eventually consistent**
- Chatbot có thể mất vài giây để index sản phẩm mới (thường < 1s)

---

## 🎉 Kết luận

Với hệ thống auto-sync này:
- ✅ **KHÔNG CẦN** gọi API seed mỗi lần thay đổi sản phẩm
- ✅ Chatbot **LUÔN** có dữ liệu mới nhất
- ✅ Developer **KHÔNG CẦN** lo việc đồng bộ
- ✅ Hệ thống hoạt động **TỰ ĐỘNG** và **TRONG SUỐT**

**Chatbot của bạn giờ đây luôn đồng bộ với shop! 🚀**
