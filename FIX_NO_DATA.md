# FIX CHATBOT KHÔNG CÓ THÔNG TIN

## Vấn đề
Chatbot trả lời "không có thông tin" về mọi thứ.

## Nguyên nhân
Backend đang chạy code CŨ, chưa có các cải thiện mới.

## Giải pháp (3 BƯỚC ĐƠN GIẢN)

### CÁCH 1: Tự động (KHUYÊN DÙNG)

#### Bước 1: Dừng backend cũ
- Tìm cửa sổ terminal đang chạy backend
- Nhấn `Ctrl + C` để dừng

#### Bước 2: Chạy lại backend với code mới
Mở terminal mới và chạy:
```powershell
cd "d:\project-cuoi-ky\nike  UI update moi nhat\BE-Nice-develop"
.\mvnw.cmd spring-boot:run
```

Đợi đến khi thấy dòng:
```
Started WebprojrctApplication in X.XXX seconds
```

#### Bước 3: Seed lại dữ liệu
Mở terminal MỚI (giữ backend chạy) và chạy:
```powershell
cd "d:\project-cuoi-ky\nike  UI update moi nhat\BE-Nice-develop"
.\reseed-chatbot-data.ps1
```

---

### CÁCH 2: Thủ công

#### Bước 1: Dừng backend
Nhấn `Ctrl + C` ở terminal đang chạy backend

#### Bước 2: Compile code mới
```powershell
.\mvnw.cmd clean compile -DskipTests
```

#### Bước 3: Chạy backend
```powershell
.\mvnw.cmd spring-boot:run
```

#### Bước 4: Đợi backend khởi động (khoảng 30-60 giây)

#### Bước 5: Seed dữ liệu (trong terminal mới)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/chat/admin/reseed-all" -Method Post
```

---

## Kiểm tra xem đã OK chưa

### 1. Kiểm tra backend
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/chat/health"
```
Kết quả: `status: UP`

### 2. Kiểm tra số lượng dữ liệu
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/chat/admin/stats"
```
Kết quả: Phải có `product_documents > 0`

### 3. Test chatbot
```powershell
$body = @{
    message = "Có những sản phẩm giày nào?"
    sessionId = "test-123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/chat" -Method Post -Body $body -ContentType "application/json"
$response.message
```

Nếu kết quả có liệt kê sản phẩm với tên, giá, size → **THÀNH CÔNG!** ✅

---

## TÓM TẮT NHANH

```powershell
# Terminal 1: Khởi động backend
cd "d:\project-cuoi-ky\nike  UI update moi nhat\BE-Nice-develop"
.\mvnw.cmd spring-boot:run

# Đợi 30-60 giây...

# Terminal 2: Seed dữ liệu
cd "d:\project-cuoi-ky\nike  UI update moi nhat\BE-Nice-develop"
.\reseed-chatbot-data.ps1

# Xong! Test chatbot ngay
```

---

## Nếu vẫn lỗi

### Lỗi: "Cannot connect"
→ Backend chưa chạy hoặc PostgreSQL chưa khởi động
```powershell
docker ps  # Kiểm tra PostgreSQL
```

### Lỗi: "500 Internal Server Error" khi seed
→ Backend chưa compile code mới
```powershell
.\mvnw.cmd clean compile -DskipTests
# Sau đó restart backend
```

### Chatbot vẫn nói "không có thông tin"
→ Dữ liệu chưa được seed
```powershell
# Xem log backend có dòng này không:
# "✅ Indexed XX products"
# Nếu không có → chạy lại reseed
```

---

## Lưu ý quan trọng

⚠️ **PHẢI restart backend** sau khi sửa code Java!
⚠️ **PHẢI seed lại dữ liệu** sau khi restart backend!
⚠️ Backend cần ~30-60 giây để khởi động hoàn toàn!

✅ Sau khi làm xong 3 bước, chatbot sẽ có đầy đủ thông tin về:
- Sản phẩm (tên, giá, size, tồn kho)
- Chính sách (giao hàng, đổi trả, bảo hành)
- FAQ (câu hỏi thường gặp)
