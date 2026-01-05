# 🎰 Lucky Wheel (Vòng Quay May Mắn) - Tài liệu

## Tính năng

Vòng quay may mắn cho phép user quay để nhận coin với các phần thưởng khác nhau.

### Quy tắc
- **1 lượt quay miễn phí mỗi ngày**
- Quay thêm: **500 coin/lượt**
- 8 ô phần thưởng với xác suất khác nhau
- Jackpot: **10,000 coin** (xác suất 1%)

### Các phần thưởng

| Vị trí | Phần thưởng | Xác suất |
|--------|-------------|----------|
| 0 | 1,000 Coin | 30% |
| 1 | Chúc may mắn lần sau | 20% |
| 2 | 2,000 Coin | 15% |
| 3 | 500 Coin | 15% |
| 4 | 5,000 Coin | 5% |
| 5 | 1,500 Coin | 10% |
| 6 | Chúc may mắn lần sau | 4% |
| 7 | **JACKPOT 10,000 Coin** | 1% |

## Cài đặt

### 1. Chạy Database Migration

```powershell
# Trong thư mục BE-Nice-develop
.\setup-lucky-wheel.ps1
```

### 2. Restart Backend

Sau khi chạy migration, restart Spring Boot để load entities mới.

### 3. Test API

```powershell
# Lấy thông tin vòng quay
curl http://localhost:8080/api/v1/lucky-wheel/info -H "Authorization: Bearer YOUR_TOKEN"

# Quay miễn phí (lượt đầu trong ngày)
curl -X POST http://localhost:8080/api/v1/lucky-wheel/spin -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" -d "{\"useFreeS pin\":true}"

# Quay bằng coin (500 coin/lượt)
curl -X POST http://localhost:8080/api/v1/lucky-wheel/spin -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" -d "{\"useFreeS pin\":false}"
```

## API Endpoints

### GET /api/v1/lucky-wheel/info

Lấy thông tin vòng quay của user

**Response:**
```json
{
  "hasFreeSpinToday": true,
  "spinCost": 500,
  "currentPoints": 5000,
  "todaySpins": 2,
  "totalCoinsWon": 15000,
  "rewards": [
    {
      "id": 1,
      "position": 0,
      "rewardType": "COIN",
      "coinAmount": 1000,
      "label": "1,000 Coin",
      "iconName": "ic_coin"
    }
  ]
}
```

### POST /api/v1/lucky-wheel/spin

Thực hiện quay thưởng

**Request Body:**
```json
{
  "useFreeS pin": true  // true = free spin, false = paid spin
}
```

**Response:**
```json
{
  "success": true,
  "message": "Chúc mừng! Bạn nhận được 1000 coin",
  "rewardPosition": 0,
  "rewardType": "COIN",
  "coinAmount": 1000,
  "totalPoints": 6000,
  "hasFreeSpinLeft": false
}
```

## Frontend (Android) Integration

### 1. Thêm Data Models

```java
// LuckyWheelInfoResponse.java
public class LuckyWheelInfoResponse {
    private Boolean hasFreeSpinToday;
    private Integer spinCost;
    private Integer currentPoints;
    private Long todaySpins;
    private Integer totalCoinsWon;
    private List<RewardItem> rewards;
    
    public static class RewardItem {
        private Long id;
        private Integer position;
        private String rewardType;
        private Integer coinAmount;
        private String label;
        private String iconName;
    }
}

// SpinResponse.java
public class SpinResponse {
    private Boolean success;
    private String message;
    private Integer rewardPosition;
    private String rewardType;
    private Integer coinAmount;
    private Integer totalPoints;
    private Boolean hasFreeSpinLeft;
}
```

### 2. Thêm API Interface

```java
public interface LuckyWheelApi {
    @GET("/api/v1/lucky-wheel/info")
    Call<LuckyWheelInfoResponse> getWheelInfo(@Header("Authorization") String token);
    
    @POST("/api/v1/lucky-wheel/spin")
    Call<SpinResponse> performSpin(
        @Header("Authorization") String token,
        @Body SpinRequest request
    );
}
```

### 3. UI Flow

1. **Màn hình vòng quay:**
   - Hiển thị 8 ô phần thưởng theo vòng tròn
   - Button "Quay miễn phí" (nếu còn lượt)
   - Button "Quay (500 coin)" (nếu đủ coin)
   - Hiển thị số coin hiện tại và số lần đã quay

2. **Animation quay:**
   - Rotate vòng quay hoặc kim chỉ
   - Dừng tại vị trí `rewardPosition` từ API
   - Hiển thị popup kết quả

3. **Xử lý kết quả:**
   - Update số coin
   - Hiển thị toast/dialog thông báo
   - Reload thông tin vòng quay

## Database Schema

### Table: `lucky_wheel_rewards`
- `id`: ID phần thưởng
- `position`: Vị trí (0-7)
- `reward_type`: COIN, COUPON, NOTHING
- `coin_amount`: Số coin
- `probability`: Xác suất (%)
- `label`: Nhãn hiển thị
- `icon_name`: Tên icon

### Table: `spin_history`
- `id`: ID lịch sử
- `user_id`: ID user
- `reward_id`: ID phần thưởng trúng
- `reward_type`: Loại thưởng
- `coin_amount`: Coin nhận được
- `spin_date`: Thời gian quay
- `cost`: Chi phí (0 = free, 500 = paid)

## Admin Functions

### Thay đổi xác suất

```sql
-- Tăng xác suất jackpot lên 2%
UPDATE lucky_wheel_rewards 
SET probability = 2.00 
WHERE position = 7;

-- Giảm xác suất ô khác để tổng vẫn = 100%
UPDATE lucky_wheel_rewards 
SET probability = 19.00 
WHERE position = 1;
```

### Xem thống kê

```sql
-- Top users thắng nhiều coin nhất
SELECT u.email, SUM(sh.coin_amount) as total_won
FROM spin_history sh
JOIN users u ON sh.user_id = u.id
WHERE sh.reward_type = 'COIN'
GROUP BY u.id, u.email
ORDER BY total_won DESC
LIMIT 10;

-- Thống kê phần thưởng
SELECT lwr.label, COUNT(*) as times_won
FROM spin_history sh
JOIN lucky_wheel_rewards lwr ON sh.reward_id = lwr.id
GROUP BY lwr.id, lwr.label
ORDER BY times_won DESC;
```

## Notes

- Mỗi user có **1 lượt free/ngày** (reset 00:00)
- Quay thêm mất **500 coin/lượt**
- Coin từ vòng quay hết hạn sau **30 ngày**
- Tổng xác suất phải = 100%
- Backend tự động chọn phần thưởng theo xác suất

---

**Ready to implement in Frontend! 🎰**
