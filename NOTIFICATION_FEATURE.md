# 🔔 NOTIFICATION SYSTEM - HỆ THỐNG THÔNG BÁO
## Nike Store App - Tài liệu thiết kế & triển khai

---

## 📋 MỤC LỤC
1. [Tổng quan](#tổng-quan)
2. [Luồng hoạt động](#luồng-hoạt-động)
3. [Cấu trúc Database](#cấu-trúc-database)
4. [Backend Architecture](#backend-architecture)
5. [Frontend Architecture](#frontend-architecture)
6. [Các sự kiện tạo thông báo](#các-sự-kiện-tạo-thông-báo)
7. [API Endpoints](#api-endpoints)
8. [Hướng dẫn triển khai](#hướng-dẫn-triển-khai)

---

## 🎯 TỔNG QUAN

Hệ thống thông báo (Notification System) cho phép gửi thông báo real-time đến người dùng về:
- **Đơn hàng**: Trạng thái đơn hàng (tạo, xác nhận, giao hàng, hoàn thành, hủy)
- **Sản phẩm yêu thích**: Giảm giá, có hàng trở lại, sắp hết hàng
- **Khuyến mãi**: Mã giảm giá mới, mã sắp hết hạn
- **Sản phẩm**: Sản phẩm mới, flash sale
- **Hệ thống**: Thông báo quan trọng, bảo trì, cập nhật

### Tính năng chính:
✅ Thông báo đa dạng theo 5 loại
✅ Phân trang và lọc thông báo
✅ Đếm số thông báo chưa đọc (badge)
✅ Đánh dấu đã đọc/chưa đọc
✅ Xóa thông báo đơn lẻ hoặc hàng loạt
✅ Thống kê thông báo theo loại
✅ Broadcast thông báo (Admin)
✅ Tích hợp với Order, Favorite, Coupon

---

## 🔄 LUỒNG HOẠT ĐỘNG

### 1. Luồng tạo thông báo (Backend → Database)

```
┌─────────────────┐
│  Sự kiện xảy ra │ (Đơn hàng được tạo, sản phẩm giảm giá, v.v.)
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Service Layer                      │
│  - OrderService                     │
│  - FavoriteService                  │
│  - CouponService                    │
│  - ProductService                   │
└────────┬────────────────────────────┘
         │ Gọi NotificationService
         ▼
┌─────────────────────────────────────┐
│  NotificationService                │
│  - createOrderNotification()        │
│  - createFavoriteNotification()     │
│  - createCouponNotification()       │
│  - createProductNotification()      │
│  - createSystemNotification()       │
└────────┬────────────────────────────┘
         │ @Async (không block)
         ▼
┌─────────────────────────────────────┐
│  NotificationRepository             │
│  - save(notification)               │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Database                           │
│  Table: notifications               │
└─────────────────────────────────────┘
```

### 2. Luồng hiển thị thông báo (Frontend → Backend → Frontend)

```
┌─────────────────┐
│  User mở App    │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  MainActivity / HomeActivity        │
│  - Check unread count               │
│  - Display badge trên icon          │
└────────┬────────────────────────────┘
         │ GET /api/notifications/count-unread
         ▼
┌─────────────────────────────────────┐
│  NotificationController             │
│  - countUnreadNotifications()       │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  NotificationService                │
│  - countUnreadNotifications()       │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Response: {"count": 5}             │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Frontend hiển thị badge "5"        │
└─────────────────────────────────────┘
```

### 3. Luồng user xem thông báo

```
┌─────────────────┐
│  User click     │
│  Notification   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  NotificationActivity               │
│  - Load notifications list          │
└────────┬────────────────────────────┘
         │ GET /api/notifications?page=0&size=20
         ▼
┌─────────────────────────────────────┐
│  NotificationController             │
│  - getNotifications()               │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  NotificationService                │
│  - getUserNotifications()           │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Response: Page<Notification>       │
│  - content: [...]                   │
│  - totalPages: 3                    │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  RecyclerView hiển thị list         │
└─────────────────────────────────────┘
```

### 4. Luồng đánh dấu đã đọc

```
┌─────────────────┐
│  User click     │
│  notification   │
│  item           │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  NotificationAdapter                │
│  - onItemClick()                    │
└────────┬────────────────────────────┘
         │ PUT /api/notifications/{id}/read
         ▼
┌─────────────────────────────────────┐
│  NotificationController             │
│  - markAsRead(id)                   │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  NotificationService                │
│  - markAsRead(id, userId)           │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Update: isRead = true              │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Navigate to related screen         │
│  - Order detail (nếu ORDER)         │
│  - Product detail (nếu FAVORITE)    │
│  - Coupons (nếu COUPON)             │
└─────────────────────────────────────┘
```

---

## 🗄️ CẤU TRÚC DATABASE

### Table: `notifications`

```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,           -- ORDER, FAVORITE, COUPON, PRODUCT, SYSTEM
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    data JSONB,                          -- Dữ liệu bổ sung dạng JSON
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes để tăng hiệu suất
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;
```

### Cột `data` (JSONB) - Ví dụ:

#### ORDER notification:
```json
{
  "order_id": 12345,
  "status": "CONFIRMED"
}
```

#### FAVORITE notification:
```json
{
  "product_id": 456,
  "product_name": "Nike Air Max 90",
  "event_type": "PRICE_DROP",
  "discount": 20
}
```

#### COUPON notification:
```json
{
  "coupon_code": "SUMMER2024",
  "discount": 100000,
  "expiry": "2025-12-25"
}
```

---

## 🏗️ BACKEND ARCHITECTURE

### 1. Entity Layer

**Notification.java**
```java
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    private String title;
    private String message;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data;
    
    private Boolean isRead = false;
    
    // Helper methods
    public void markAsRead() { this.isRead = true; }
    public Long getOrderId() { ... }
    public Long getProductId() { ... }
}
```

**NotificationType.java (Enum)**
```java
public enum NotificationType {
    ORDER("Đơn hàng"),
    FAVORITE("Yêu thích"),
    COUPON("Khuyến mãi"),
    PRODUCT("Sản phẩm"),
    SYSTEM("Hệ thống");
}
```

### 2. Repository Layer

**NotificationRepository.java**
```java
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Page<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user, Pageable pageable);
    Long countUnreadByUser(@Param("user") User user);
    void markAllAsReadByUser(@Param("user") User user);
}
```

### 3. Service Layer

**NotificationService.java**
```java
@Service
public class NotificationService {
    
    // Tạo thông báo (async)
    @Async
    public void createNotification(Long userId, NotificationType type, 
                                   String title, String message, Map<String, Object> data);
    
    // Các method tiện ích
    @Async
    public void createOrderNotification(Long userId, Long orderId, String status, String message);
    
    @Async
    public void createFavoriteNotification(Long userId, Long productId, String productName, ...);
    
    @Async
    public void createCouponNotification(Long userId, String couponCode, String message);
    
    // Lấy thông báo
    public Page<NotificationResponse> getUserNotifications(Long userId, int page, int size);
    public Page<NotificationResponse> getUnreadNotifications(Long userId, int page, int size);
    public Long countUnreadNotifications(Long userId);
    
    // Thao tác
    public void markAsRead(Long notificationId, Long userId);
    public void markAllAsRead(Long userId);
    public void deleteNotification(Long notificationId, Long userId);
}
```

### 4. Controller Layer

**NotificationController.java**
```java
@RestController
@RequestMapping("/api")
public class NotificationController {
    
    @GetMapping("/notifications")
    public ResponseEntity<Page<NotificationResponse>> getNotifications(...);
    
    @GetMapping("/notifications/unread")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(...);
    
    @GetMapping("/notifications/count-unread")
    public ResponseEntity<Map<String, Long>> countUnreadNotifications();
    
    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id);
    
    @PutMapping("/notifications/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead();
    
    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id);
    
    @PostMapping("/admin/notifications/broadcast")
    public ResponseEntity<Map<String, String>> broadcastNotification(...);
}
```

---

## 📱 FRONTEND ARCHITECTURE

### 1. API Interface

**NotificationApi.java**
```java
public interface NotificationApi {
    @GET("api/notifications")
    Call<NotificationResponse> getNotifications(@Query("page") int page, @Query("size") int size);
    
    @GET("api/notifications/unread")
    Call<NotificationResponse> getUnreadNotifications(...);
    
    @GET("api/notifications/count-unread")
    Call<UnreadCountResponse> getUnreadCount();
    
    @PUT("api/notifications/{id}/read")
    Call<Map<String, String>> markAsRead(@Path("id") Long notificationId);
    
    @DELETE("api/notifications/{id}")
    Call<Map<String, String>> deleteNotification(@Path("id") Long notificationId);
}
```

### 2. Data Models

**Notification.java**
```java
public class Notification {
    private Long id;
    private Long userId;
    private String type;  // ORDER, FAVORITE, COUPON, PRODUCT, SYSTEM
    private String title;
    private String message;
    private Map<String, Object> data;
    private Boolean isRead;
    private String createdAt;
    
    // Helper methods
    public boolean isOrderNotification() { return "ORDER".equals(type); }
    public Long getOrderId() { ... }
    public int getIconResource() { ... }
    public int getColor() { ... }
}
```

### 3. UI Components

**Layouts:**
- `activity_notification.xml` - Màn hình chính
- `item_notification.xml` - Item trong RecyclerView

**Activities/Fragments:**
- `NotificationActivity.java` - Hiển thị danh sách thông báo
- `NotificationAdapter.java` - Adapter cho RecyclerView

**Features trong UI:**
- Tab "Tất cả" và "Chưa đọc"
- Badge hiển thị số thông báo chưa đọc
- Click vào notification → Navigate to related screen
- Swipe to delete
- Pull to refresh
- Pagination (load more)

---

## 🎬 CÁC SỰ KIỆN TẠO THÔNG BÁO

### 1. ORDER - Thông báo đơn hàng

| Sự kiện | Khi nào xảy ra | Code tích hợp |
|---------|---------------|---------------|
| Đơn hàng được tạo | User đặt hàng thành công | `OrderService.placeOrder()` |
| Đơn hàng xác nhận | Admin xác nhận đơn | `AdminOrderService.confirmOrder()` |
| Đơn hàng đang giao | Admin cập nhật trạng thái | `AdminOrderService.updateStatus("SHIPPING")` |
| Đơn hàng giao thành công | Shipper xác nhận giao | `AdminOrderService.updateStatus("DELIVERED")` |
| Đơn hàng bị hủy | User hoặc Admin hủy | `OrderService.cancelOrder()` |

**Ví dụ tích hợp:**
```java
// Trong OrderService.placeOrder()
Order order = orderRepository.save(newOrder);

// Tạo thông báo
notificationService.createOrderNotification(
    user.getId(),
    order.getId(),
    "PENDING",
    "Đơn hàng #" + order.getId() + " đã được tạo và đang chờ xác nhận."
);
```

### 2. FAVORITE - Thông báo sản phẩm yêu thích

| Sự kiện | Khi nào xảy ra | Cách triển khai |
|---------|---------------|-----------------|
| Sản phẩm giảm giá | Admin cập nhật giá/discount | Scheduled job kiểm tra hàng ngày |
| Sản phẩm có hàng trở lại | Stock > 0 sau khi sold out | Event listener khi update stock |
| Sản phẩm sắp hết | Stock < 10 | Scheduled job kiểm tra |

**Ví dụ Scheduled Job:**
```java
@Scheduled(cron = "0 0 8 * * *") // 8h sáng mỗi ngày
public void checkFavoritePriceDrops() {
    List<User> allUsers = userRepository.findAll();
    
    for (User user : allUsers) {
        List<Favorite> favorites = favoriteRepository.findByUser(user);
        
        for (Favorite fav : favorites) {
            Product product = fav.getProduct();
            
            if (product.getDiscountPercent() > 0) {
                notificationService.createFavoriteNotification(
                    user.getId(),
                    product.getId(),
                    product.getName(),
                    "PRICE_DROP",
                    product.getName() + " đang giảm " + product.getDiscountPercent() + "%!"
                );
            }
        }
    }
}
```

### 3. COUPON - Thông báo khuyến mãi

| Sự kiện | Khi nào xảy ra | Code tích hợp |
|---------|---------------|---------------|
| Nhận mã mới | User lưu coupon | `UserCouponService.saveCoupon()` |
| Mã sắp hết hạn | 3 ngày trước expiry | Scheduled job |
| Mã đã sử dụng | User apply coupon | `OrderService.placeOrder()` |

**Ví dụ Scheduled Job:**
```java
@Scheduled(cron = "0 0 9 * * *") // 9h sáng mỗi ngày
public void checkExpiringCoupons() {
    LocalDateTime threeDaysLater = LocalDateTime.now().plusDays(3);
    List<Coupon> expiringCoupons = couponRepository
        .findByEndDateBetween(LocalDateTime.now(), threeDaysLater);
    
    for (Coupon coupon : expiringCoupons) {
        List<User> users = userCouponRepository.findUsersByCouponId(coupon.getId());
        for (User user : users) {
            notificationService.createCouponNotification(
                user.getId(),
                coupon.getCode(),
                "Mã " + coupon.getCode() + " sắp hết hạn. Sử dụng ngay!"
            );
        }
    }
}
```

### 4. PRODUCT - Thông báo sản phẩm

| Sự kiện | Khi nào xảy ra | Code tích hợp |
|---------|---------------|---------------|
| Sản phẩm mới | Admin tạo sản phẩm | `AdminProductService.createProduct()` |
| Flash sale | Admin tạo flash sale | Broadcast notification |

**Ví dụ broadcast:**
```java
// Khi có sản phẩm mới
notificationService.broadcastNotification(
    NotificationType.PRODUCT,
    "Sản phẩm mới",
    "Nike Air Force 1 Limited Edition vừa ra mắt!",
    Map.of("product_id", productId, "product_name", productName)
);
```

### 5. SYSTEM - Thông báo hệ thống

| Sự kiện | Khi nào xảy ra | Code tích hợp |
|---------|---------------|---------------|
| Chào mừng user mới | User đăng ký | `AuthService.register()` |
| Bảo trì hệ thống | Admin thông báo | Manual broadcast |

**Ví dụ:**
```java
// Trong AuthService.register()
User newUser = userRepository.save(user);

notificationService.createSystemNotification(
    newUser.getId(),
    "Chào mừng đến Nike Store",
    "Xin chào " + newUser.getUsername() + "! Khám phá hàng nghìn sản phẩm!"
);
```

---

## 🌐 API ENDPOINTS

### User Endpoints

#### 1. Lấy danh sách thông báo
```http
GET /api/notifications?page=0&size=20
Authorization: Bearer {token}

Response:
{
  "content": [
    {
      "id": 1,
      "userId": 123,
      "type": "ORDER",
      "title": "Đơn hàng đã xác nhận",
      "message": "Đơn hàng #12345 đã được xác nhận...",
      "data": {"order_id": 12345, "status": "CONFIRMED"},
      "isRead": false,
      "createdAt": "2025-12-24T10:30:00",
      "updatedAt": "2025-12-24T10:30:00"
    }
  ],
  "totalPages": 3,
  "totalElements": 50,
  "number": 0,
  "size": 20
}
```

#### 2. Lấy thông báo chưa đọc
```http
GET /api/notifications/unread?page=0&size=20
Authorization: Bearer {token}

Response: (giống trên, chỉ có isRead = false)
```

#### 3. Đếm thông báo chưa đọc
```http
GET /api/notifications/count-unread
Authorization: Bearer {token}

Response:
{
  "count": 5
}
```

#### 4. Đánh dấu đã đọc
```http
PUT /api/notifications/{id}/read
Authorization: Bearer {token}

Response:
{
  "message": "Đã đánh dấu thông báo là đã đọc"
}
```

#### 5. Đánh dấu tất cả đã đọc
```http
PUT /api/notifications/read-all
Authorization: Bearer {token}

Response:
{
  "message": "Đã đánh dấu tất cả thông báo là đã đọc"
}
```

#### 6. Xóa thông báo
```http
DELETE /api/notifications/{id}
Authorization: Bearer {token}

Response:
{
  "message": "Đã xóa thông báo"
}
```

#### 7. Xóa tất cả thông báo
```http
DELETE /api/notifications
Authorization: Bearer {token}

Response:
{
  "message": "Đã xóa tất cả thông báo"
}
```

#### 8. Thống kê thông báo
```http
GET /api/notifications/statistics
Authorization: Bearer {token}

Response:
{
  "ORDER": 10,
  "FAVORITE": 5,
  "COUPON": 3,
  "PRODUCT": 2,
  "SYSTEM": 1
}
```

### Admin Endpoints

#### 9. Broadcast thông báo
```http
POST /api/admin/notifications/broadcast
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "type": "SYSTEM",
  "title": "Thông báo bảo trì",
  "message": "Hệ thống sẽ bảo trì từ 00:00 - 02:00 ngày mai",
  "data": {}
}

Response:
{
  "message": "Đã gửi thông báo cho tất cả người dùng"
}
```

#### 10. Tạo thông báo cho user cụ thể
```http
POST /api/admin/notifications/user/{userId}
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "type": "COUPON",
  "title": "Mã giảm giá đặc biệt",
  "message": "Bạn nhận được mã VIP100 giảm 100k",
  "data": {"coupon_code": "VIP100"}
}

Response:
{
  "message": "Đã tạo thông báo cho user 123"
}
```

---

## 🚀 HƯỚNG DẪN TRIỂN KHAI

### Bước 1: Setup Database

```bash
# Chạy migration
cd BE-Nice-develop
psql -U cps_user -d cps_db -f db/notifications_migration.sql
```

### Bước 2: Cấu hình Backend

1. **Thêm dependency vào pom.xml** (nếu chưa có):
```xml
<!-- JSON processing cho JSONB -->
<dependency>
    <groupId>io.hypersistence</groupId>
    <artifactId>hypersistence-utils-hibernate-60</artifactId>
    <version>3.6.1</version>
</dependency>
```

2. **Enable Async trong Spring Boot**:
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notification-");
        executor.initialize();
        return executor;
    }
}
```

3. **Tích hợp vào OrderService**:
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final NotificationService notificationService; // Inject
    
    public String placeOrder(...) {
        // ... existing code ...
        Order order = orderRepository.save(newOrder);
        
        // Tạo thông báo
        notificationService.createOrderNotification(
            user.getId(),
            order.getId(),
            "PENDING",
            "Đơn hàng #" + order.getId() + " đã được tạo thành công."
        );
        
        return order.getId().toString();
    }
}
```

### Bước 3: Setup Frontend Android

1. **Thêm method vào RetrofitClient**:
```java
public NotificationApi getNotificationApi() {
    return retrofit.create(NotificationApi.class);
}
```

2. **Tạo NotificationActivity**:
- Hiển thị danh sách thông báo
- Tab "Tất cả" và "Chưa đọc"
- Click item → Navigate to related screen

3. **Hiển thị badge trên Home**:
```java
// Trong MainActivity
private void loadUnreadCount() {
    notificationApi.getUnreadCount().enqueue(new Callback<UnreadCountResponse>() {
        @Override
        public void onResponse(..., Response<UnreadCountResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                long count = response.body().getCount();
                // Hiển thị badge với count
                updateNotificationBadge(count);
            }
        }
    });
}
```

### Bước 4: Testing

1. **Test Backend API với Postman**:
   - Import collection từ `POSTMAN_TEST_COMMANDS.md`
   - Test tất cả endpoints

2. **Test Android App**:
   - Tạo đơn hàng → Kiểm tra nhận thông báo
   - Thêm sản phẩm yêu thích → Kiểm tra thông báo giảm giá
   - Xem danh sách thông báo
   - Đánh dấu đã đọc
   - Xóa thông báo

### Bước 5: Scheduled Jobs (Optional)

Tạo các job tự động:

```java
@Component
public class NotificationScheduledTasks {
    
    @Scheduled(cron = "0 0 8 * * *") // 8h sáng
    public void checkFavoritePriceDrops() {
        // Logic kiểm tra giá sản phẩm yêu thích
    }
    
    @Scheduled(cron = "0 0 9 * * *") // 9h sáng
    public void checkExpiringCoupons() {
        // Logic kiểm tra mã sắp hết hạn
    }
    
    @Scheduled(cron = "0 0 2 * * *") // 2h sáng
    public void cleanOldNotifications() {
        // Xóa notification > 30 ngày
        notificationService.cleanOldNotifications();
    }
}
```

---

## 📊 DIAGRAM TỔNG QUAN

```
┌──────────────────────────────────────────────────────────────┐
│                    NOTIFICATION SYSTEM                        │
└──────────────────────────────────────────────────────────────┘

┌─────────────────┐         ┌──────────────────┐
│   User Events   │────────>│  Service Layer   │
│                 │         │  - OrderService  │
│ - Đặt hàng      │         │  - FavoriteServ  │
│ - Yêu thích SP  │         │  - CouponService │
│ - Nhận coupon   │         └────────┬─────────┘
└─────────────────┘                  │
                                     ▼
                          ┌─────────────────────┐
                          │ NotificationService │
                          │  @Async execution   │
                          └─────────┬───────────┘
                                    │
                                    ▼
                          ┌─────────────────────┐
                          │     Database        │
                          │  notifications      │
                          └─────────┬───────────┘
                                    │
                                    ▼
┌─────────────────┐         ┌──────────────────┐
│  Android App    │<────────│  REST API        │
│                 │         │  /api/notif...   │
│ - List view     │  HTTP   │                  │
│ - Badge count   │ Requests│ - GET, PUT, DEL  │
│ - Mark read     │         │ - Pagination     │
└─────────────────┘         └──────────────────┘
```

---

## ✅ CHECKLIST TRIỂN KHAI

### Backend
- [x] Tạo database migration
- [x] Tạo Notification entity
- [x] Tạo NotificationRepository
- [x] Tạo NotificationService với @Async
- [x] Tạo NotificationController với đầy đủ endpoints
- [ ] Tích hợp vào OrderService
- [ ] Tích hợp vào FavoriteService  
- [ ] Tích hợp vào CouponService
- [ ] Tạo scheduled jobs (optional)
- [ ] Test với Postman

### Frontend
- [x] Tạo NotificationApi interface
- [x] Thêm method vào RetrofitClient
- [x] Tạo Notification models
- [ ] Tạo NotificationActivity
- [ ] Tạo NotificationAdapter
- [ ] Tạo layouts (activity, item)
- [ ] Hiển thị badge trên Home
- [ ] Handle click notification → navigate
- [ ] Test trên emulator/device

### Testing
- [ ] Test API với Postman
- [ ] Test tạo notification khi đặt hàng
- [ ] Test hiển thị notification trong app
- [ ] Test đánh dấu đã đọc
- [ ] Test xóa notification
- [ ] Test pagination
- [ ] Test badge count

---

## 🎓 KẾT LUẬN

Hệ thống thông báo đã được thiết kế hoàn chỉnh với:

✅ **Backend**: Entity, Repository, Service (@Async), Controller, Integration examples
✅ **Frontend**: API interface, Models, UI components
✅ **Database**: Schema tối ưu với indexes
✅ **Documentation**: Luồng hoạt động chi tiết, API docs, hướng dẫn triển khai

### Các tính năng nổi bật:
- Async notification creation (không block main thread)
- JSONB data storage (linh hoạt)
- Phân trang, lọc, thống kê
- Badge count real-time
- Deep linking (từ notification → màn hình liên quan)
- Admin broadcast notifications
- Scheduled jobs tự động

### Mở rộng trong tương lai:
- Push notification (Firebase Cloud Messaging)
- Real-time notifications (WebSocket/SSE)
- Notification preferences (user settings)
- Rich notifications (images, actions)
- Notification history analytics

---

**Tác giả**: GitHub Copilot  
**Ngày tạo**: 24/12/2025  
**Version**: 1.0
