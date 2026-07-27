# CÁCH FIX CHATBOT - ĐƠN GIẢN NHẤT

## Vấn đề
- Chatbot không có thông tin
- Backend cần chạy code mới
- Cần seed dữ liệu

## GIẢI PHÁP (KHÔNG ĐƯỢC NHẤN CTRL+C!)

### 1. Mở terminal này và chạy:
```powershell
cd "d:\project-cuoi-ky\nike  UI update moi nhat\BE-Nice-develop"
.\mvnw.cmd spring-boot:run
```

### 2. ĐỢI 1-2 PHÚT (KHÔNG NHẤN CTRL+C!)

Backend sẽ:
- Compile code
- Khởi động Spring Boot  
- Khởi động embedding model
- TỰ ĐỘNG seed dữ liệu (33 sản phẩm, FAQs, policies)

Bạn sẽ thấy:
```
✅ Indexed 33 products
✅ Indexed 6 FAQs  
✅ Indexed 2 policies
✅ Data seeding completed successfully
```

### 3. Khi thấy "Data seeding completed", MỞ TERMINAL MỚI và test:

```powershell
# Test chatbot
$body = @{
    message = "Có những sản phẩm giày nào?"
    sessionId = "test-123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/chat" -Method Post -Body $body -ContentType "application/json"
$response.message
```

## LƯU Ý QUAN TRỌNG

❌ **KHÔNG NHẤN CTRL+C** khi thấy "Ingesting text from source: product-X"
❌ **KHÔNG TERMINATE** batch job
✅ **ĐỂ NÓ CHẠY XONG** (1-2 phút)
✅ **MỞ TERMINAL MỚI** để test (đừng đóng terminal backend)

## Nếu đã nhấn Ctrl+C nhiều lần

Database đã sạch rồi (đã xóa 436 records cũ).
Chỉ cần:
1. Chạy backend LẦN CUỐI
2. Đợi nó seed xong
3. XONG!

## Khi backend đã chạy xong

Terminal backend sẽ KHÔNG CÓ GÌ THÊM - đó là bình thường!
Backend đang chạy và đợi request.

Hãy MỞ TERMINAL MỚI để test chatbot.
