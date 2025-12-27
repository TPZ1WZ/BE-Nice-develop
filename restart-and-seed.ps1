# Script tự động restart backend và seed lại dữ liệu

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "🔄 RESTART BACKEND VÀ SEED LẠI DỮ LIỆU" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Bước 1: Compile code mới
Write-Host "📦 Bước 1: Compile code..." -ForegroundColor Yellow
.\mvnw.cmd clean compile -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Lỗi compile! Vui lòng kiểm tra lại code." -ForegroundColor Red
    exit 1
}
Write-Host "✅ Compile thành công!" -ForegroundColor Green
Write-Host ""

# Bước 2: Chờ backend khởi động
Write-Host "⏳ Bước 2: Đang khởi động backend..." -ForegroundColor Yellow
Write-Host "   (Nếu backend đã chạy, hãy DỪNG nó và chạy lại: .\mvnw.cmd spring-boot:run)" -ForegroundColor Yellow
Write-Host ""
Write-Host "Đợi 10 giây để backend khởi động..." -ForegroundColor Gray
Start-Sleep -Seconds 10

# Bước 3: Kiểm tra backend
Write-Host "🔍 Bước 3: Kiểm tra backend..." -ForegroundColor Yellow
$maxAttempts = 12
$attempt = 0
$backendReady = $false

while ($attempt -lt $maxAttempts -and -not $backendReady) {
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/chat/health" -Method Get -TimeoutSec 2
        if ($health.status -eq "UP") {
            $backendReady = $true
            Write-Host "✅ Backend đã sẵn sàng!" -ForegroundColor Green
        }
    } catch {
        $attempt++
        Write-Host "   Đợi backend khởi động... ($attempt/$maxAttempts)" -ForegroundColor Gray
        Start-Sleep -Seconds 5
    }
}

if (-not $backendReady) {
    Write-Host ""
    Write-Host "❌ Backend chưa sẵn sàng sau $($maxAttempts * 5) giây!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Vui lòng:" -ForegroundColor Yellow
    Write-Host "  1. Mở terminal mới" -ForegroundColor White
    Write-Host "  2. Chạy: cd `"d:\project-cuoi-ky\nike  UI update moi nhat\BE-Nice-develop`"" -ForegroundColor White
    Write-Host "  3. Chạy: .\mvnw.cmd spring-boot:run" -ForegroundColor White
    Write-Host "  4. Sau đó chạy lại script này" -ForegroundColor White
    Write-Host ""
    exit 1
}

Write-Host ""

# Bước 4: Seed lại dữ liệu
Write-Host "🌱 Bước 4: Reset và seed lại dữ liệu..." -ForegroundColor Yellow
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/chat/admin/reseed-all" -Method Post -ContentType "application/json"
    
    Write-Host ""
    Write-Host "✅ HOÀN THÀNH!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📊 KẾT QUẢ:" -ForegroundColor Cyan
    Write-Host "  - Trạng thái: $($result.success)" -ForegroundColor White
    Write-Host "  - Sản phẩm đã index: $($result.products_indexed)" -ForegroundColor White
    Write-Host "  - Lỗi: $($result.products_errors)" -ForegroundColor White
    Write-Host ""
    
    if ($result.success) {
        Write-Host "🎉 Chatbot giờ đã có đầy đủ thông tin!" -ForegroundColor Green
        Write-Host ""
        Write-Host "💬 Test ngay:" -ForegroundColor Cyan
        Write-Host '  $body = @{ message = "Có những sản phẩm giày nào?"; sessionId = "test-123" } | ConvertTo-Json' -ForegroundColor Gray
        Write-Host '  Invoke-RestMethod -Uri "http://localhost:8080/api/v1/chat" -Method Post -Body $body -ContentType "application/json"' -ForegroundColor Gray
    }
    
} catch {
    Write-Host "❌ LỖI khi seed dữ liệu: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Chi tiết lỗi:" -ForegroundColor Yellow
    Write-Host $_.Exception -ForegroundColor Red
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
