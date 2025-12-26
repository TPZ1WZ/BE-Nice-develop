# 🔐 CHỨC NĂNG QUÊN MẬT KHẨU - HOÀN THÀNH

## 📋 Tổng quan

Đã implement thành công chức năng **Quên mật khẩu với OTP** cho Nike Store Backend.

---

## 🎯 Flow hoàn chỉnh

```
1. User nhập email 
   ↓
2. POST /api/v1/auth/forgot-password-otp
   → Kiểm tra email tồn tại
   → Generate OTP 6 số
   → Gửi email OTP
   ↓
3. User nhập OTP (trong vòng 5 phút)
   ↓
4. POST /api/v1/auth/verify-password-reset-otp
   → Verify OTP
   → Mark as verified
   ↓
5. User nhập mật khẩu mới
   ↓
6. POST /api/v1/auth/reset-password-with-otp
   → Kiểm tra OTP đã verified
   → Đổi mật khẩu
   → Xóa pending reset
   ↓
7. Quay về trang Login
```

---

## 🆕 Files đã tạo

### 1. DTOs
- ✅ `ForgotPasswordRequest.java` - Request với email
- ✅ `ForgotPasswordResponse.java` - Response (success, message, email)
- ✅ `ResetPasswordWithOtpRequest.java` - Reset với email, OTP, newPassword

### 2. Models
- ✅ `PendingPasswordReset.java` - Model lưu trạng thái reset tạm thời

### 3. Services
- ✅ `PendingPasswordResetService.java` - Quản lý pending reset (in-memory)
  - `savePending()` - Lưu pending reset
  - `findByEmail()` - Tìm pending reset
  - `verifyOtp()` - Verify OTP và đánh dấu verified
  - `isOtpVerified()` - Check OTP đã verified chưa
  - `removePending()` - Xóa sau khi reset thành công
  - `cleanExpired()` - Auto cleanup mỗi 10 phút

### 4. Email Templates
- ✅ `sendPasswordResetOtp()` trong EmailService.java
- ✅ `buildPasswordResetOtpEmail()` - Template đẹp với màu đỏ

---

## 🔌 API Endpoints

### 1. **Yêu cầu OTP reset password**

```http
POST /api/v1/auth/forgot-password-otp
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "message": "Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra và nhập trong vòng 5 phút.",
  "email": "user@example.com"
}
```

**Response Error (404 - Email không tồn tại):**
```json
{
  "success": false,
  "message": "Email không tồn tại trong hệ thống",
  "email": "user@example.com"
}
```

---

### 2. **Verify OTP**

```http
POST /api/v1/auth/verify-password-reset-otp
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": 123456
}
```

**Response Success (200):**
```json
{
  "success": true,
  "message": "Xác thực OTP thành công. Bạn có thể đặt mật khẩu mới.",
  "email": "user@example.com"
}
```

**Response Error (400 - OTP sai/hết hạn):**
```json
{
  "success": false,
  "message": "Mã OTP không đúng hoặc đã hết hạn",
  "email": "user@example.com"
}
```

---

### 3. **Đặt lại mật khẩu mới**

```http
POST /api/v1/auth/reset-password-with-otp
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": 123456,
  "newPassword": "newSecurePassword123"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "message": "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập với mật khẩu mới.",
  "email": "user@example.com"
}
```

**Response Error (400 - OTP chưa verify):**
```json
{
  "success": false,
  "message": "Vui lòng xác thực OTP trước khi đặt lại mật khẩu",
  "email": "user@example.com"
}
```

---

## 📧 Email Template

Email OTP sẽ có:
- Header màu đỏ với icon 🔐
- OTP 6 số in đậm trong box màu hồng nhạt
- Cảnh báo không chia sẻ OTP
- Thời gian hết hạn: **5 phút**
- Footer với copyright

---

## 🔒 Security Features

1. ✅ **OTP expiry**: 5 phút tự động hết hạn
2. ✅ **In-memory storage**: Không lưu DB (an toàn)
3. ✅ **2-step verification**: Phải verify OTP trước khi đổi password
4. ✅ **Auto cleanup**: Xóa expired OTP mỗi 10 phút
5. ✅ **Password encoding**: BCrypt hash password mới
6. ✅ **Logging**: Chi tiết log mọi bước

---

## 🧪 Testing với Postman

### Test Flow đầy đủ:

1. **Step 1: Request OTP**
```bash
POST http://localhost:8080/api/v1/auth/forgot-password-otp
Body: {"email": "test@example.com"}
```

2. **Kiểm tra email** → Lấy OTP 6 số

3. **Step 2: Verify OTP**
```bash
POST http://localhost:8080/api/v1/auth/verify-password-reset-otp
Body: {"email": "test@example.com", "otp": 123456}
```

4. **Step 3: Reset Password**
```bash
POST http://localhost:8080/api/v1/auth/reset-password-with-otp
Body: {
  "email": "test@example.com",
  "otp": 123456,
  "newPassword": "newPassword123"
}
```

5. **Step 4: Login với password mới**
```bash
POST http://localhost:8080/api/v1/auth/login
Body: {
  "username": "test@example.com",
  "password": "newPassword123"
}
```

---

## ✅ Checklist hoàn thành

- [x] DTOs (Request/Response)
- [x] Model PendingPasswordReset
- [x] Service quản lý pending reset
- [x] Email service với template đẹp
- [x] 3 API endpoints
- [x] Validation (email format, password min 6 chars)
- [x] Error handling
- [x] Logging chi tiết
- [x] Auto cleanup expired OTP
- [x] Security (OTP verification required)

---

## 🚀 Cách sử dụng cho Frontend

### Android Flow:

```kotlin
// 1. ForgotPasswordActivity - Nhập email
val email = etEmail.text.toString()
api.forgotPasswordWithOtp(ForgotPasswordRequest(email))
    .enqueue { response ->
        if (response.success) {
            // Chuyển sang OTP screen
            navigateToOtpScreen(email)
        }
    }

// 2. VerifyOtpActivity - Nhập OTP
val otp = getOtpFromInputs()
api.verifyPasswordResetOtp(VerifyOtpDTO(email, otp))
    .enqueue { response ->
        if (response.success) {
            // Chuyển sang Reset Password screen
            navigateToResetPasswordScreen(email, otp)
        }
    }

// 3. ResetPasswordActivity - Nhập password mới
val newPassword = etNewPassword.text.toString()
api.resetPasswordWithOtp(
    ResetPasswordWithOtpRequest(email, otp, newPassword)
).enqueue { response ->
    if (response.success) {
        // Quay về Login
        navigateToLogin()
    }
}
```

---

## 📝 Notes

- OTP có hiệu lực **5 phút**
- Phải verify OTP trước khi đổi password
- Email phải tồn tại trong hệ thống
- Password mới tối thiểu **6 ký tự**
- Auto cleanup pending reset mỗi 10 phút

---

## 🎉 Status: HOÀN THÀNH 100%

Backend đã sẵn sàng để Frontend integrate!
