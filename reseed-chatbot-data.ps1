# Script để reset và seed lại dữ liệu chatbot
# Chạy script này khi muốn cập nhật lại toàn bộ dữ liệu vector

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "🔄 RESEED CHATBOT DATA" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api/v1/chat"

Write-Host "Đang gửi yêu cầu reset và reseed toàn bộ dữ liệu..." -ForegroundColor Yellow
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/admin/reseed-all" -Method Post -ContentType "application/json"
    
    Write-Host "✅ Hoàn thành!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📊 KẾT QUẢ:" -ForegroundColor Cyan
    Write-Host "  - Trạng thái: $($response.success)" -ForegroundColor White
    Write-Host "  - Sản phẩm đã index: $($response.products_indexed)" -ForegroundColor White
    Write-Host "  - Lỗi: $($response.products_errors)" -ForegroundColor White
    Write-Host ""
    
    if ($response.success) {
        Write-Host "🎉 Dữ liệu chatbot đã được cập nhật thành công!" -ForegroundColor Green
        Write-Host "Chatbot giờ đây sẽ có thông tin đầy đủ hơn về sản phẩm." -ForegroundColor Green
    }
    
} catch {
    Write-Host "❌ LỖI: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Đảm bảo rằng:" -ForegroundColor Yellow
    Write-Host "  1. Backend đang chạy trên http://localhost:8080" -ForegroundColor White
    Write-Host "  2. PostgreSQL đang chạy và có dữ liệu" -ForegroundColor White
    Write-Host "  3. API endpoint /api/v1/chat/admin/reseed-all hoạt động" -ForegroundColor White
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Nhấn phím bất kỳ để thoát..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
