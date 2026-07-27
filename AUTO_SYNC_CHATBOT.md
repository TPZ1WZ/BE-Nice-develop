# 🔄 TỰ ĐỘNG ĐỒNG BỘ CHATBOT KHI CÓ SẢN PHẨM MỚI

## ✅ ĐÃ CÀI ĐẶT XONG

Hệ thống của bạn đã được cập nhật để **tự động đồng bộ chatbot** mỗi khi có sản phẩm mới/cập nhật/xóa.

---

## 🎯 CÁCH HOẠT ĐỘNG

### **1. Khi tạo sản phẩm mới:**
```java
// ProductService.createProduct() - Line 285-298
// ✅ Tự động gọi dataSeedService.syncSingleProduct(saved)
```

**Ví dụ:**
```bash
POST /api/products
{
  "name": "Nike Air Max 2024",
  "price": 3500000,
  "categoryId": 1,
  ...
}
```
➡️ **Chatbot tự động biết ngay sản phẩm mới này!** 🎉

---

### **2. Khi cập nhật sản phẩm:**
```java
// ProductService.updateProduct() - Line 300-346
// ✅ Tự động gọi dataSeedService.syncSingleProduct(updated)
```

**Ví dụ:**
```bash
PUT /api/products/123
{
  "price": 2990000,  // Giảm giá
  "stock": 50        // Cập nhật tồn kho
}
```
➡️ **Chatbot tự động cập nhật thông tin mới!** 🔄

---

### **3. Khi xóa sản phẩm (soft delete):**
```java
// ProductService.deleteProduct() - Line 348-361
// ✅ Tự động gọi dataSeedService.removeSingleProduct(id)
```

**Ví dụ:**
```bash
DELETE /api/products/123
```
➡️ **Chatbot tự động xóa sản phẩm khỏi danh sách!** 🗑️

---

## 📋 CÁC METHOD MỚI ĐƯỢC THÊM

### **DataSeedService.java:**

#### 1. `syncSingleProduct(Product product)`
- Đồng bộ **1 sản phẩm** vào vector store
- Gọi khi: Tạo mới hoặc cập nhật sản phẩm

#### 2. `removeSingleProduct(Long productId)`
- Xóa **1 sản phẩm** khỏi vector store  
- Gọi khi: Soft delete sản phẩm

---

## 🎮 KIỂM TRA

### **Test thử:**
```bash
# 1. Tạo sản phẩm mới
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nike Test Product",
    "price": 1500000,
    "categoryId": 1
  }'

# 2. Kiểm tra chatbot đã biết chưa
curl http://localhost:8080/api/v1/chat/admin/stats
# Sẽ thấy product_documents tăng lên 1

# 3. Hỏi chatbot
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Có sản phẩm Nike Test Product không?",
    "sessionId": "test-123"
  }'
```

---

## 🔧 ENDPOINT ADMIN (vẫn còn dùng được)

### **Đồng bộ thủ công toàn bộ:**
```bash
# Sync lại tất cả sản phẩm (nếu cần)
curl -X POST http://localhost:8080/api/v1/chat/admin/seed-products

# Reset và sync lại toàn bộ
curl -X POST http://localhost:8080/api/v1/chat/admin/reseed-all

# Kiểm tra thống kê
curl http://localhost:8080/api/v1/chat/admin/stats
```

---

## 📊 LOG ĐỂ THEO DÕI

Khi tạo/sửa/xóa sản phẩm, bạn sẽ thấy log:

```log
✅ Synced product 123 to chatbot
✅ Removed product 456 from chatbot
```

Nếu có lỗi (không ảnh hưởng đến việc tạo/sửa sản phẩm):
```log
⚠️ Warning: Failed to sync product to chatbot: [error message]
```

---

## ⚡ LỢI ÍCH

| Trước | Sau |
|-------|-----|
| ❌ Phải gọi API `/admin/seed-products` thủ công | ✅ Tự động đồng bộ |
| ❌ Chatbot không biết sản phẩm mới | ✅ Chatbot biết ngay lập tức |
| ❌ Phải nhớ sync sau mỗi lần thêm sản phẩm | ✅ Không cần làm gì cả |

---

## 🎯 KẾT LUẬN

**Bây giờ bạn KHÔNG cần làm gì cả!**

Chỉ cần:
1. ➕ Thêm sản phẩm bình thường qua API Product
2. ✏️ Sửa sản phẩm bình thường qua API Product  
3. 🗑️ Xóa sản phẩm bình thường qua API Product

➡️ **Chatbot tự động cập nhật!** 🚀

---

## 📝 LƯU Ý

- Tự động sync **không làm chậm** API Product (chạy async)
- Nếu sync lỗi, sản phẩm vẫn được **tạo/sửa/xóa thành công**
- Có thể dùng endpoint `/admin/seed-products` để sync lại nếu cần
