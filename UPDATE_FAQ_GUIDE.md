# 📝 HƯỚNG DẪN CẬP NHẬT FAQs CHATBOT

## 🔄 Quy trình cập nhật FAQs

### BƯỚC 1: Sửa code
```java
// File: DataSeedService.java
// Sửa nội dung trong faqs.put(...)
```

### BƯỚC 2: Xóa dữ liệu cũ trong database
```powershell
docker exec -it cps_postgres psql -U cps_user -d cps_db -c "DELETE FROM vector_documents;"
```

### BƯỚC 3: Restart backend
- Stop backend hiện tại (Ctrl+C)
- Chạy lại: `.\mvnw.cmd spring-boot:run`

### BƯỚC 4: Seed lại dữ liệu
```powershell
# Seed toàn bộ (products + FAQs + policies)
Invoke-WebRequest -Uri "http://localhost:8080/api/chat/admin/reseed-all" -Method POST

# Hoặc chỉ seed FAQs
Invoke-WebRequest -Uri "http://localhost:8080/api/chat/admin/seed-knowledge" -Method POST
```

### BƯỚC 5: Kiểm tra
```powershell
# Kiểm tra số lượng documents
docker exec -it cps_postgres psql -U cps_user -d cps_db -c "SELECT COUNT(*) FROM vector_documents;"

# Test chatbot bằng cách hỏi: "Về đội ngũ phát triển Nike Store"
```

---

## ⚡ Lệnh nhanh (All-in-one)

```powershell
# Xóa data cũ + Restart backend + Seed lại
docker exec -it cps_postgres psql -U cps_user -d cps_db -c "DELETE FROM vector_documents;"; Start-Sleep -Seconds 5; Invoke-WebRequest -Uri "http://localhost:8080/api/chat/admin/reseed-all" -Method POST
```

---

## 📊 Các lệnh hữu ích

### Kiểm tra PostgreSQL
```powershell
# Xem container đang chạy
docker ps | Select-String "postgres"

# Kiểm tra số lượng documents
docker exec -it cps_postgres psql -U cps_user -d cps_db -c "SELECT COUNT(*) FROM vector_documents;"

# Xem 10 documents mới nhất
docker exec -it cps_postgres psql -U cps_user -d cps_db -c "SELECT id, LEFT(content, 100) FROM vector_documents ORDER BY created_at DESC LIMIT 10;"
```

### Xóa dữ liệu cụ thể
```powershell
# Xóa toàn bộ
docker exec -it cps_postgres psql -U cps_user -d cps_db -c "DELETE FROM vector_documents;"

# Xóa lịch sử chat
docker exec -it cps_postgres psql -U cps_user -d cps_db -c "DELETE FROM chat_messages;"
```

### API Endpoints
```
POST   /api/chat/admin/seed-products      - Seed products only
POST   /api/chat/admin/seed-knowledge     - Seed FAQs + policies only
POST   /api/chat/admin/reseed-all         - Seed toàn bộ (xóa + seed lại)
POST   /api/chat                           - Chat với chatbot
```

---

## ⚠️ Lưu ý quan trọng

1. **Luôn xóa data cũ** trước khi seed lại FAQs đã sửa
2. **Không cần restart backend** khi chỉ test chatbot
3. **Data KHÔNG mất** khi restart backend (lưu trong PostgreSQL)
4. **Mỗi lần sửa FAQs** trong code → Phải xóa + seed lại
5. **Products tự động sync** từ database, không cần sửa code

---

## 🎯 Thông tin team hiện tại

```
👥 THÀNH VIÊN:
• Phát - Thợ săn: Full Stack Developer + AI Engineer
• Kiệt - Máy bào: Chuyên gia xử lý dữ liệu + UI/UX Designer
• Lâm - Thợ điện bất ổn: Chuyên hệ thống nhúng (nhưng hơi bất ổn)
• Đạt - Quả tạ: Đang nạp thêm kiến thức và rèn luyện kỹ năng

🏆 Đại học Sư phạm Kỹ thuật TP.HCM
🎯 Mục tiêu: Đồ án Android cuối kỳ 10 điểm
```
