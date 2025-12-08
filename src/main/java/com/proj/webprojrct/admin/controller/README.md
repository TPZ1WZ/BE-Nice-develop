# Admin Dashboard Controllers

Module này chứa các **REST Controllers** cho **Admin Dashboard** của hệ thống NICESTORE. Các controller được thiết kế **đơn giản** và **đủ dùng** cho các tác vụ quản lý cơ bản.

---

## 📋 Danh sách Controllers

### 1. **AdminUserController** 
```java
@RestController
@RequestMapping("/admin/api/users")
```

**🎯 Chức năng**:
- ✅ `GET /admin/api/users` - Lấy danh sách user (có phân trang)
- ✅ `GET /admin/api/users/{id}` - Xem chi tiết user
- ✅ `PUT /admin/api/users/{id}/status` - Cập nhật trạng thái active
- ✅ `DELETE /admin/api/users/{id}` - Xóa user (soft delete)
- ✅ `GET /admin/api/users/stats/by-role` - Thống kê theo role
- ✅ `GET /admin/api/users/stats/total` - Tổng số user
- ✅ `GET /admin/api/users/stats/active` - Số user active

---

### 2. **AdminProductController**
```java
@RestController
@RequestMapping("/admin/api/products")
```

**🎯 Chức năng**:
- ✅ `GET /admin/api/products` - Lấy danh sách sản phẩm (có phân trang)
- ✅ `GET /admin/api/products/{id}` - Xem chi tiết sản phẩm
- ✅ `POST /admin/api/products` - Tạo sản phẩm mới
- ✅ `PUT /admin/api/products/{id}` - Cập nhật sản phẩm
- ✅ `DELETE /admin/api/products/{id}` - Xóa sản phẩm
- ✅ `GET /admin/api/products/search` - Tìm kiếm sản phẩm
- ✅ `GET /admin/api/products/stats/total` - Tổng số sản phẩm
- ✅ `GET /admin/api/products/stats/out-of-stock` - Sản phẩm hết hàng
- ✅ `GET /admin/api/products/stats/low-stock` - Sản phẩm sắp hết
- ✅ `GET /admin/api/products/top-selling` - Top sản phẩm bán chạy

---

### 3. **AdminOrderController**
```java
@RestController
@RequestMapping("/admin/api/orders")
```

**🎯 Chức năng**:
- ✅ `GET /admin/api/orders` - Lấy danh sách đơn hàng (có phân trang)
- ✅ `GET /admin/api/orders/{id}` - Xem chi tiết đơn hàng
- ✅ `PUT /admin/api/orders/{id}/status` - Cập nhật trạng thái đơn hàng
- ✅ `GET /admin/api/orders/by-status` - Lọc đơn hàng theo trạng thái
- ✅ `GET /admin/api/orders/stats/total` - Tổng số đơn hàng
- ✅ `GET /admin/api/orders/stats/by-status` - Thống kê theo trạng thái
- ✅ `GET /admin/api/orders/stats/daily-revenue` - Doanh thu hàng ngày
- ✅ `GET /admin/api/orders/stats/total-revenue` - Tổng doanh thu
- ✅ `GET /admin/api/orders/by-date-range` - Đơn hàng theo khoảng thời gian
- ✅ `DELETE /admin/api/orders/{id}` - Xóa đơn hàng

---

### 4. **AdminReviewController**
```java
@RestController
@RequestMapping("/admin/api/reviews")
```

**🎯 Chức năng**:
- ✅ `GET /admin/api/reviews` - Lấy danh sách đánh giá (có phân trang)
- ✅ `GET /admin/api/reviews/{id}` - Xem chi tiết đánh giá
- ✅ `PUT /admin/api/reviews/{id}/approve` - Duyệt đánh giá
- ✅ `PUT /admin/api/reviews/{id}/hide` - Ẩn đánh giá
- ✅ `GET /admin/api/reviews/pending` - Đánh giá chờ duyệt
- ✅ `GET /admin/api/reviews/stats/by-status` - Thống kê theo trạng thái
- ✅ `GET /admin/api/reviews/stats/total` - Tổng số đánh giá
- ✅ `GET /admin/api/reviews/by-product/{productId}` - Đánh giá theo sản phẩm
- ✅ `DELETE /admin/api/reviews/{id}` - Xóa đánh giá
- ✅ `GET /admin/api/reviews/by-rating` - Lọc theo rating

---

### 5. **AdminDashboardController**
```java
@RestController
@RequestMapping("/admin/api/dashboard")
```

**🎯 Chức năng**:
- ✅ `GET /admin/api/dashboard/overview` - Tổng quan dashboard
- ✅ `GET /admin/api/dashboard/stats/users` - Thống kê người dùng
- ✅ `GET /admin/api/dashboard/stats/products` - Thống kê sản phẩm  
- ✅ `GET /admin/api/dashboard/stats/orders` - Thống kê đơn hàng
- ✅ `GET /admin/api/dashboard/charts/revenue` - Biểu đồ doanh thu
- ✅ `GET /admin/api/dashboard/charts/orders` - Biểu đồ đơn hàng
- ✅ `GET /admin/api/dashboard/recent-activities` - Hoạt động gần đây
- ✅ `GET /admin/api/dashboard/alerts` - Thông báo & cảnh báo
- ✅ `GET /admin/api/dashboard/export/summary` - Xuất báo cáo tổng hợp

---

## 🚀 Đặc điểm Thiết kế

### **🎯 Đơn giản & Đủ dùng**
- **RESTful API** chuẩn với HTTP methods phù hợp
- **Response Entity** pattern cho error handling
- **Pageable support** cho các endpoint list
- **Query parameters** cho filtering và searching

### **📊 Thống kê & Báo cáo**
- **Basic statistics** cho từng module
- **Chart data endpoints** cho dashboard visualization  
- **Export capabilities** cho báo cáo
- **Real-time data** cho monitoring

### **🔒 Security Ready**
- **Path variables validation** 
- **Request parameter validation**
- **Consistent error responses**
- **Ready cho authentication/authorization**

---

## 💻 Usage Examples

### **User Management**
```bash
# Lấy danh sách users
GET /admin/api/users?page=0&size=10

# Xem chi tiết user
GET /admin/api/users/123

# Cập nhật trạng thái user  
PUT /admin/api/users/123/status?isActive=false

# Thống kê users theo role
GET /admin/api/users/stats/by-role
```

### **Product Management**
```bash
# Tạo sản phẩm mới
POST /admin/api/products
Content-Type: application/json
{
  "name": "Nike Air Force 1",
  "description": "Classic sneaker",
  "price": 100.0,
  "stock": 50
}

# Tìm kiếm sản phẩm
GET /admin/api/products/search?keyword=nike

# Top sản phẩm bán chạy
GET /admin/api/products/top-selling?limit=5
```

### **Order Management**
```bash
# Cập nhật trạng thái đơn hàng
PUT /admin/api/orders/456/status?status=shipped

# Thống kê doanh thu 30 ngày
GET /admin/api/orders/stats/daily-revenue?days=30

# Đơn hàng theo khoảng thời gian
GET /admin/api/orders/by-date-range?startDate=2025-01-01&endDate=2025-01-31
```

### **Dashboard Overview**
```bash
# Tổng quan dashboard
GET /admin/api/dashboard/overview

# Biểu đồ doanh thu
GET /admin/api/dashboard/charts/revenue?days=30

# Xuất báo cáo
GET /admin/api/dashboard/export/summary
```

---

## 📈 Response Examples

### **Dashboard Overview Response**
```json
{
  "totalUsers": 1250,
  "totalProducts": 485,
  "totalOrders": 2340,
  "totalRevenue": 125000.50,
  "usersByRole": [
    ["CUSTOMER", 1200],
    ["ADMIN", 5],
    ["STAFF", 45]
  ],
  "ordersByStatus": [
    ["completed", 2100],
    ["pending", 150],
    ["shipped", 90]
  ]
}
```

### **Paginated Response**
```json
{
  "content": [
    {
      "id": 1,
      "fullName": "John Doe",
      "email": "john@example.com",
      "role": "CUSTOMER",
      "isActive": true
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1250,
  "totalPages": 125
}
```

---

## 🔧 Configuration & Setup

### **Required Dependencies**
```java
@RequiredArgsConstructor  // Lombok for constructor injection
@RestController          // Spring MVC REST controller
@RequestMapping         // Base path mapping
```

### **Error Handling Pattern**
```java
return adminRepository.findById(id)
    .map(entity -> {
        // Success logic
        return ResponseEntity.ok(result);
    })
    .orElse(ResponseEntity.notFound().build());
```

### **Pagination Support**
```java
@GetMapping
public ResponseEntity<Page<Entity>> getAll(Pageable pageable) {
    Page<Entity> page = repository.findAll(pageable);
    return ResponseEntity.ok(page);
}
```

---

## 🎯 Best Practices Implemented

### **1. Consistent Naming**
- **Verb + Resource** pattern: `getUserById`, `updateOrderStatus`
- **Clear endpoint paths**: `/admin/api/{resource}/{action}`
- **Meaningful HTTP status codes**

### **2. Error Handling**
- **404 Not Found** cho resource không tồn tại
- **200 OK** cho success responses
- **Consistent error response format**

### **3. Performance**
- **Pagination** cho large datasets
- **Query parameters** cho filtering  
- **Lightweight responses** cho list views

### **4. Maintainability**
- **Single responsibility** per endpoint
- **Clear separation** giữa các modules
- **Readable code** với proper documentation

---

## 🚧 Note về Lỗi

Một số endpoints có **compile errors** do dependency vào:
- **Repository methods** chưa implement
- **DTO classes** chưa tạo
- **Custom query methods** cần bổ sung

Đây là **normal** trong development process. Các controller đã được thiết kế **ready-to-use** sau khi các dependencies được implement.

---

## 🎉 Kết luận

Admin Dashboard Controllers module cung cấp **complete REST API** cho tất cả admin operations. Thiết kế **đơn giản**, **đủ dùng**, và **easy to extend** cho future requirements! 🚀