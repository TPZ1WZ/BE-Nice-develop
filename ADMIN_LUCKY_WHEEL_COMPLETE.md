# 🎰 ADMIN LUCKY WHEEL - QUẢN LÝ VÒNG QUAY MAY MẮN

## 📋 Tổng quan chức năng Admin

Admin có đầy đủ quyền quản lý vòng quay may mắn với 6 nhóm chức năng chính:

---

## 1️⃣ QUẢN LÝ DANH SÁCH PHẦN THƯỞNG

### 📌 **Lấy tất cả phần thưởng**
```
GET /api/v1/admin/lucky-wheel/prizes
Authorization: Bearer {admin_token}
```

### 📌 **Tạo phần thưởng mới**
```
POST /api/v1/admin/lucky-wheel/prizes
Authorization: Bearer {admin_token}

Body:
{
  "name": "Giảm 20%",
  "type": "VOUCHER",
  "description": "Giảm 20% cho đơn hàng từ 1,000,000đ",
  "discountValue": 20.0,
  "probability": 0.05,
  "quantity": 50,
  "remainingQuantity": 50,
  "isActive": true,
  "iconUrl": "/images/prizes/discount-20.png",
  "color": "#FF5722"
}
```

### 📌 **Cập nhật phần thưởng**
```
PUT /api/v1/admin/lucky-wheel/prizes/{id}
Authorization: Bearer {admin_token}

Body: (same as create)
```

### 📌 **Xóa phần thưởng**
```
DELETE /api/v1/admin/lucky-wheel/prizes/{id}
Authorization: Bearer {admin_token}
```

---

## 2️⃣ CẤU HÌNH XÁC SUẤT TRÚNG

### 📌 **Cập nhật xác suất nhiều phần thưởng cùng lúc**
```
POST /api/v1/admin/lucky-wheel/prizes/batch-probability
Authorization: Bearer {admin_token}

Body:
{
  "prizes": [
    {"prizeId": 1, "probability": 0.05},    // Giảm 20% - 5%
    {"prizeId": 2, "probability": 0.20},    // Giảm 10% - 20%
    {"prizeId": 3, "probability": 0.15},    // Freeship - 15%
    {"prizeId": 4, "probability": 0.20},    // Điểm thưởng - 20%
    {"prizeId": 5, "probability": 0.40}     // Chúc may mắn - 40%
  ]
}

Response:
[
  {
    "id": 1,
    "name": "Giảm 20%",
    "probability": 0.05,
    ...
  },
  ...
]
```

**💡 Lưu ý:** Tổng probability nên = 1.0 (100%)

---

## 3️⃣ QUẢN LÝ SỰ KIỆN QUAY

### 📌 **Lấy cấu hình hiện tại**
```
GET /api/v1/admin/lucky-wheel/config
Authorization: Bearer {admin_token}
```

### 📌 **Bật/tắt vòng quay**
```
POST /api/v1/admin/lucky-wheel/toggle?active=true
Authorization: Bearer {admin_token}

Response:
{
  "id": 1,
  "isActive": true,
  "maxSpinsPerDay": 1,
  ...
}
```

### 📌 **Cập nhật thời gian sự kiện**
```
POST /api/v1/admin/lucky-wheel/event-schedule
Authorization: Bearer {admin_token}

Params:
- eventName: "Tết Nguyên Đán 2026"
- startDate: 2026-01-28T00:00:00
- endDate: 2026-02-03T23:59:59
- isTimeRestricted: true

Response:
{
  "id": 1,
  "eventName": "Tết Nguyên Đán 2026",
  "startDate": "2026-01-28T00:00:00",
  "endDate": "2026-02-03T23:59:59",
  "isTimeRestricted": true,
  "isActive": true,
  ...
}
```

### 📌 **Cập nhật cấu hình đầy đủ**
```
PUT /api/v1/admin/lucky-wheel/config
Authorization: Bearer {admin_token}

Body:
{
  "isActive": true,
  "maxSpinsPerDay": 3,
  "maxSpinsPerWeek": 10,
  "requiresLogin": true,
  "requiresOrder": false,
  "minOrderCount": 0,
  "description": "Chúc bạn may mắn!",
  "eventName": "Black Friday 2026",
  "startDate": "2026-11-27T00:00:00",
  "endDate": "2026-11-30T23:59:59",
  "isTimeRestricted": true
}
```

---

## 4️⃣ QUẢN LÝ LƯỢT QUAY USER

### 📌 **Xem danh sách user và lượt quay**
```
GET /api/v1/admin/lucky-wheel/user-spins
Authorization: Bearer {admin_token}

Response:
[
  {
    "userId": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "spinsToday": 1,
    "spinsThisWeek": 5,
    "totalSpins": 25,
    "prizesWon": 12
  },
  ...
]
```

### 📌 **Xem lịch sử quay của 1 user**
```
GET /api/v1/admin/lucky-wheel/user-spins/{userId}/history?page=0&size=20
Authorization: Bearer {admin_token}

Response: Page<SpinHistory>
```

### 📌 **Reset lượt quay hôm nay của user**
```
POST /api/v1/admin/lucky-wheel/user-spins/{userId}/reset
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Đã reset lượt quay của user 123"
}
```

**⚠️ Cảnh báo:** Sẽ XÓA tất cả lịch sử quay hôm nay của user này!

### 📌 **Tặng thêm lượt quay cho user**
```
POST /api/v1/admin/lucky-wheel/user-spins/{userId}/grant?bonusSpins=5
Authorization: Bearer {admin_token}

Response:
{
  "success": true,
  "message": "Đã tặng 5 lượt quay cho user 123"
}
```

**💡 Note:** Hiện tại chưa implement bảng bonus_spins riêng, cần bổ sung.

---

## 5️⃣ XEM LỊCH SỬ TẤT CẢ LƯỢT QUAY

### 📌 **Xem tất cả lịch sử quay (admin)**
```
GET /api/v1/admin/lucky-wheel/history?page=0&size=20
Authorization: Bearer {admin_token}

Response: Page<SpinHistory>
{
  "content": [
    {
      "id": 1,
      "user": { "id": 1, "username": "john_doe" },
      "prize": { "id": 2, "name": "Giảm 10%" },
      "spinTime": "2026-01-03T15:30:00",
      "prizeCode": "VCE9B90107",
      "isClaimed": true
    },
    ...
  ],
  "totalElements": 1250,
  "totalPages": 63,
  "number": 0,
  "size": 20
}
```

---

## 6️⃣ THỐNG KÊ

### 📌 **Lấy thống kê tổng quan**
```
GET /api/v1/admin/lucky-wheel/statistics
Authorization: Bearer {admin_token}

Response:
{
  "totalSpins": 1250,
  "spinsToday": 45,
  "spinsThisWeek": 320,
  "uniqueUsers": 89,
  "totalPrizesWon": 750,
  "mostPopularPrize": "Giảm 10%",
  "mostPopularPrizeCount": 250,
  "topSpinner": "john_doe",
  "topSpinnerCount": 35
}
```

---

## 🎯 Use Cases thực tế

### **Case 1: Tạo sự kiện Tết Nguyên Đán**
```bash
# Bước 1: Tạo phần thưởng đặc biệt
POST /api/v1/admin/lucky-wheel/prizes
{
  "name": "Giảm 30% - Đặc biệt Tết",
  "type": "VOUCHER",
  "discountValue": 30.0,
  "probability": 0.10,
  "quantity": 100,
  ...
}

# Bước 2: Cấu hình xác suất
POST /api/v1/admin/lucky-wheel/prizes/batch-probability
{
  "prizes": [
    {"prizeId": 1, "probability": 0.10},  // Giảm 30% - 10%
    {"prizeId": 2, "probability": 0.20},  // Giảm 20% - 20%
    {"prizeId": 3, "probability": 0.30},  // Giảm 10% - 30%
    {"prizeId": 4, "probability": 0.15},  // Freeship - 15%
    {"prizeId": 5, "probability": 0.25}   // Chúc may mắn - 25%
  ]
}

# Bước 3: Cấu hình sự kiện
POST /api/v1/admin/lucky-wheel/event-schedule
eventName=Tết Nguyên Đán 2026
startDate=2026-01-28T00:00:00
endDate=2026-02-03T23:59:59
isTimeRestricted=true

# Bước 4: Bật vòng quay
POST /api/v1/admin/lucky-wheel/toggle?active=true
```

### **Case 2: Tặng thưởng cho VIP user**
```bash
# User ID 123 là VIP → tặng 10 lượt quay
POST /api/v1/admin/lucky-wheel/user-spins/123/grant?bonusSpins=10
```

### **Case 3: User báo lỗi không quay được**
```bash
# Reset lượt quay của user để họ thử lại
POST /api/v1/admin/lucky-wheel/user-spins/456/reset
```

### **Case 4: Giảm tỷ lệ trúng khi hết ngân sách**
```bash
POST /api/v1/admin/lucky-wheel/prizes/batch-probability
{
  "prizes": [
    {"prizeId": 1, "probability": 0.02},  // Giảm 30% → 2% (giảm từ 10%)
    {"prizeId": 2, "probability": 0.08},  // Giảm 20% → 8% (giảm từ 20%)
    {"prizeId": 5, "probability": 0.60}   // Chúc may mắn → 60% (tăng từ 25%)
  ]
}
```

---

## 🗄️ Database Schema Updates

### **wheel_config** - Thêm event management fields
```sql
event_name VARCHAR(255)           -- Tên sự kiện
start_date TIMESTAMP               -- Ngày bắt đầu
end_date TIMESTAMP                 -- Ngày kết thúc  
is_time_restricted BOOLEAN         -- Giới hạn theo thời gian
```

### **New DTOs**
- `BatchProbabilityRequest` - Update xác suất nhiều prizes
- `PrizeProbabilityUpdate` - Update xác suất 1 prize
- `UserSpinManagement` - Quản lý lượt quay user
- `LuckyWheelStatistics` - Thống kê tổng quan

---

## 🔒 Security

Tất cả endpoints đều yêu cầu:
- `Authorization: Bearer {admin_token}`
- Role: `ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`)

---

## 📊 Business Logic

### **Kiểm tra vòng quay có hoạt động không:**
```java
public boolean isCurrentlyActive() {
    if (!isActive) return false;
    if (!isTimeRestricted) return true;
    
    LocalDateTime now = LocalDateTime.now();
    if (startDate != null && now.isBefore(startDate)) return false;
    if (endDate != null && now.isAfter(endDate)) return false;
    
    return true;
}
```

### **Tính xác suất trúng:**
- Random từ 0.0 → 1.0
- So sánh với cumulative probability
- Ví dụ: 5% + 20% + 15% + 20% + 40% = 100%

---

## 🚀 Testing với Postman

### **Collection: Admin Lucky Wheel**

```json
{
  "name": "Admin Lucky Wheel",
  "item": [
    {
      "name": "1. Get All Prizes",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/api/v1/admin/lucky-wheel/prizes",
        "header": [{"key": "Authorization", "value": "Bearer {{adminToken}}"}]
      }
    },
    {
      "name": "2. Create Prize",
      "request": {
        "method": "POST",
        "url": "{{baseUrl}}/api/v1/admin/lucky-wheel/prizes",
        "header": [{"key": "Authorization", "value": "Bearer {{adminToken}}"}],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"name\": \"Giảm 50%\",\n  \"type\": \"VOUCHER\",\n  \"discountValue\": 50.0,\n  \"probability\": 0.01,\n  \"quantity\": 10,\n  \"remainingQuantity\": 10,\n  \"isActive\": true\n}"
        }
      }
    },
    {
      "name": "3. Batch Update Probability",
      "request": {
        "method": "POST",
        "url": "{{baseUrl}}/api/v1/admin/lucky-wheel/prizes/batch-probability",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"prizes\": [\n    {\"prizeId\": 1, \"probability\": 0.05},\n    {\"prizeId\": 2, \"probability\": 0.20}\n  ]\n}"
        }
      }
    },
    {
      "name": "4. Toggle Wheel",
      "request": {
        "method": "POST",
        "url": "{{baseUrl}}/api/v1/admin/lucky-wheel/toggle?active=true"
      }
    },
    {
      "name": "5. Get Statistics",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/api/v1/admin/lucky-wheel/statistics"
      }
    }
  ]
}
```

---

## ✅ Checklist triển khai

- [x] Entity: Thêm event fields vào WheelConfig
- [x] DTOs: BatchProbabilityRequest, UserSpinManagement, LuckyWheelStatistics
- [x] Repository: Thêm queries thống kê
- [x] Service: Implement tất cả logic quản lý
- [x] Controller: 20+ endpoints cho admin
- [x] Migration: wheel_config_event_migration.sql
- [ ] Frontend Admin Panel
- [ ] Test với Postman
- [ ] Implement bảng user_bonus_spins

---

🎉 **Admin có đầy đủ quyền kiểm soát vòng quay may mắn!**
