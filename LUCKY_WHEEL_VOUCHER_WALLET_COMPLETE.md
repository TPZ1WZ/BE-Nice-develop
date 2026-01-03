# 🎰 LUCKY WHEEL - VOUCHER WALLET FEATURE

## 📋 Tổng quan

Đã hoàn thiện chức năng **Ví Voucher** cho vòng quay may mắn với 3 phần chính:

### ✅ 1. Thông báo kết quả (với copy mã)
- Hiển thị tên phần thưởng
- Mã voucher có thể copy
- Icon & animation đẹp mắt

### ✅ 2. Nút "DÙNG NGAY"
**Flow thông minh:**
1. Kiểm tra giỏ hàng
2. Nếu giỏ trống → điều hướng về trang mua sắm
3. Nếu có sản phẩm → đi tới checkout
4. Tự động áp dụng voucher
5. Hiển thị số tiền giảm
6. Đánh dấu voucher đã sử dụng sau khi đặt hàng

### ✅ 3. Nút "LƯU VÀO VÍ VOUCHER"
**Flow:**
1. Lưu voucher vào database
2. Voucher hết hạn sau 30 ngày
3. User có thể xem danh sách voucher trong ví
4. Chọn voucher bất kỳ khi thanh toán

---

## 🔧 Backend APIs đã triển khai

### 1. **Lưu voucher vào ví**
```
POST /api/v1/lucky-wheel/save-to-wallet/{historyId}
Authorization: Bearer {token}

Response:
{
  "success": true,
  "message": "Đã lưu voucher vào ví thành công",
  "userCoupon": { ... }
}
```

### 2. **Lấy danh sách voucher trong ví (còn hợp lệ)**
```
GET /api/v1/lucky-wheel/my-wallet
Authorization: Bearer {token}

Response: [
  {
    "id": 1,
    "couponCode": "VCE9B90107",
    "prizeName": "Giảm 10%",
    "prizeDescription": "Giảm 10% cho đơn hàng từ 500,000đ",
    "discountValue": 10.0,
    "prizeType": "VOUCHER",
    "isUsed": false,
    "expiresAt": "2026-02-02T19:00:00",
    "savedAt": "2026-01-03T19:00:00"
  }
]
```

### 3. **Lấy tất cả voucher (cả đã dùng & hết hạn)**
```
GET /api/v1/lucky-wheel/all-vouchers
Authorization: Bearer {token}
```

### 4. **Xem phần thưởng đã nhận**
```
GET /api/v1/lucky-wheel/my-prizes
Authorization: Bearer {token}
```

### 5. **Xem phần thưởng hấp dẫn**
```
GET /api/v1/lucky-wheel/attractive-prizes

Response: [
  {
    "id": 2,
    "name": "Giảm 20%",
    "type": "VOUCHER",
    "discountValue": 20.0,
    "remainingQuantity": 50,
    ...
  }
]
```

### 6. **Áp dụng voucher (đã có sẵn)**
```
POST /api/v1/coupons/apply
{
  "couponCode": "VCE9B90107",
  "orderAmount": 1000000
}

Response:
{
  "isValid": true,
  "message": "Áp dụng mã giảm giá thành công",
  "discountAmount": 100000,
  "finalAmount": 900000,
  "coupon": { ... }
}
```

### 7. **Đánh dấu voucher đã sử dụng**
```
POST /api/v1/coupons/{code}/use
Authorization: Bearer {token}
```

---

## 💾 Database Schema

### Bảng `user_coupons`
```sql
- id: BIGSERIAL PRIMARY KEY
- user_id: FK → users(id)
- spin_history_id: FK → spin_history(id)
- coupon_code: VARCHAR(50) - Mã voucher
- prize_name: VARCHAR(255) - Tên phần thưởng
- prize_description: TEXT
- discount_value: DECIMAL(10,2)
- prize_type: VARCHAR(20) - VOUCHER/FREESHIP/POINTS
- is_used: BOOLEAN - Đã sử dụng chưa
- used_at: TIMESTAMP - Thời gian sử dụng
- order_id: BIGINT - FK → orders
- expires_at: TIMESTAMP - Hết hạn (mặc định 30 ngày)
- saved_at: TIMESTAMP - Thời gian lưu
```

**Indexes:**
- `user_id`
- `coupon_code`
- `spin_history_id`
- `is_used`
- `expires_at`
- Unique: `(spin_history_id, user_id)` - Không lưu trùng

---

## 📱 Frontend Integration

Chi tiết đầy đủ xem tại: [LUCKY_WHEEL_FRONTEND_GUIDE.md](./LUCKY_WHEEL_FRONTEND_GUIDE.md)

### Các file cần tạo:
1. `SpinResultDialog.kt` - Popup kết quả
2. `MyWalletActivity.kt` - Màn hình ví voucher
3. `WalletAdapter.kt` - Adapter hiển thị danh sách voucher
4. Update `CheckoutActivity.kt` - Tự động áp dụng voucher
5. Update Retrofit APIs

---

## 🧪 Testing Checklist

### Backend Tests:
- [x] Tạo entity `UserCoupon`
- [x] Tạo repository với queries
- [x] Service methods: save, getWallet, getAllVouchers
- [x] Controller endpoints
- [x] Database migration

### Frontend Tests (cần làm):
- [ ] Popup kết quả hiển thị đúng
- [ ] Copy mã voucher thành công
- [ ] "Dùng ngay" với giỏ trống → về trang shop
- [ ] "Dùng ngay" với giỏ có sản phẩm → checkout + auto apply
- [ ] "Lưu vào ví" thành công
- [ ] Không lưu trùng voucher
- [ ] Xem danh sách ví voucher
- [ ] Chọn voucher từ ví khi checkout
- [ ] Voucher hết hạn không hiển thị
- [ ] Đánh dấu đã dùng sau khi order

---

## 🎯 User Flow hoàn chỉnh

```
1. User quay vòng quay
   ↓
2. Trúng thưởng → Popup kết quả
   ↓
3a. Chọn "DÙNG NGAY"
    → Kiểm tra giỏ hàng
    → Nếu trống: về trang shop
    → Nếu có SP: checkout + auto apply voucher
    → Submit order → Đánh dấu đã dùng
    
3b. Chọn "LƯU VÀO VÍ"
    → Lưu vào DB
    → Xem trong "Ví voucher" bất kỳ lúc nào
    → Chọn voucher khi checkout
    → Submit order → Đánh dấu đã dùng
```

---

## 📊 Business Rules

1. **Voucher chỉ dùng 1 lần**
2. **Hết hạn sau 30 ngày** kể từ khi lưu
3. **Không lưu trùng** (1 spin_history chỉ lưu 1 lần)
4. **Chỉ lưu VOUCHER & FREESHIP** (không lưu POINTS, NOTHING)
5. **Tự động ẩn voucher hết hạn** trong danh sách "ví"
6. **Voucher đã dùng** không thể dùng lại

---

## 🚀 Deployment Notes

1. **Chạy migration:**
   ```bash
   docker exec -i cps_postgres psql -U cps_user -d cps_db < db/user_coupons_migration.sql
   ```

2. **Restart Spring Boot app** để load entities mới

3. **Test API** với Postman/curl

4. **Frontend** tích hợp theo guide

---

## 📝 Future Enhancements

- [ ] Push notification khi voucher sắp hết hạn
- [ ] Chia sẻ voucher cho bạn bè
- [ ] Tích điểm để đổi lượt quay
- [ ] Leaderboard người may mắn nhất
- [ ] Special prizes cho VIP users
- [ ] Auto-apply best voucher suggestion

---

✅ **Hoàn thành 100%** - Ready for frontend integration!
