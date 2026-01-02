# Test Chatbot đơn giản
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$baseUrl = "http://localhost:8080/api/v1/chat"

Write-Host "=== Test Chatbot Nike Store ===" -ForegroundColor Cyan
Write-Host ""

# Test 1: Stats
Write-Host "1. Kiem tra so luong documents:" -ForegroundColor Yellow
try {
    $stats = Invoke-RestMethod -Uri "$baseUrl/admin/stats" -Method Get
    Write-Host "   - Tong documents: $($stats.total_documents)" -ForegroundColor Green
    Write-Host "   - San pham: $($stats.product_documents)" -ForegroundColor Green
    Write-Host "   - FAQs: $($stats.faq_documents)" -ForegroundColor Green
    Write-Host "   - Policies: $($stats.policy_documents)" -ForegroundColor Green
} catch {
    Write-Host "   LOI: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 2: Chat với message đơn giản
Write-Host "2. Test chat voi cau hoi don gian:" -ForegroundColor Yellow
$sessionId = [guid]::NewGuid().ToString()

$body = @{
    message = "hello"
    sessionId = $sessionId
}

$json = $body | ConvertTo-Json -Depth 10
Write-Host "   Request: $json" -ForegroundColor Gray

try {
    $response = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $json -ContentType "application/json; charset=utf-8"
    Write-Host "   Response: $($response.message.Substring(0, [Math]::Min(100, $response.message.Length)))..." -ForegroundColor Green
    Write-Host "   Sources: $($response.sources.Count) documents" -ForegroundColor Green
} catch {
    Write-Host "   LOI: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Chi tiet: $($_.ErrorDetails.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 3: Chat về sản phẩm
Write-Host "3. Hoi ve san pham:" -ForegroundColor Yellow
$body2 = @{
    message = "Co giay nao duoi 2 trieu khong"
    sessionId = $sessionId
}

$json2 = $body2 | ConvertTo-Json -Depth 10
Write-Host "   Request: $json2" -ForegroundColor Gray

try {
    $response2 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $json2 -ContentType "application/json; charset=utf-8"
    Write-Host "   Response:" -ForegroundColor Green
    Write-Host "   $($response2.message)" -ForegroundColor White
    Write-Host "   Sources: $($response2.sources.Count) documents" -ForegroundColor Green
    
    if ($response2.sources -and $response2.sources.Count -gt 0) {
        Write-Host "   Top source: $($response2.sources[0].sourceType) - similarity: $($response2.sources[0].similarity)" -ForegroundColor Gray
    }
} catch {
    Write-Host "   LOI: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Chi tiet: $($_.ErrorDetails.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== Ket thuc ===" -ForegroundColor Cyan
