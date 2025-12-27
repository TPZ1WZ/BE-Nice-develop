# Test Chatbot API với PowerShell

Write-Host "🤖 Testing Nike Store RAG Chatbot" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api/v1/chat"

# Test 1: Health Check
Write-Host "Test 1: Health Check" -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/health" -Method Get
    Write-Host "✅ Status: $($health.status)" -ForegroundColor Green
    Write-Host "   Message: $($health.message)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 2: Get Stats
Write-Host "Test 2: Knowledge Base Statistics" -ForegroundColor Yellow
try {
    $stats = Invoke-RestMethod -Uri "$baseUrl/admin/stats" -Method Get
    Write-Host "✅ Total Documents: $($stats.total_documents)" -ForegroundColor Green
    Write-Host "   Products: $($stats.product_documents)" -ForegroundColor Gray
    Write-Host "   FAQs: $($stats.faq_documents)" -ForegroundColor Gray
    Write-Host "   Policies: $($stats.policy_documents)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 3: Simple Chat Query
Write-Host "Test 3: Chat - Ask about products" -ForegroundColor Yellow
$sessionId = [guid]::NewGuid().ToString()
$request1 = @{
    message = "Có giày chạy bộ nào không?"
    sessionId = $sessionId
} | ConvertTo-Json

try {
    $response1 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $request1 -ContentType "application/json"
    Write-Host "✅ Bot Response:" -ForegroundColor Green
    Write-Host "   $($response1.message)" -ForegroundColor Gray
    Write-Host "   Sources: $($response1.sources.Count) documents" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 4: Price Query
Write-Host "Test 4: Chat - Ask about pricing" -ForegroundColor Yellow
$request2 = @{
    message = "Giày Jordan 1 giá bao nhiêu?"
    sessionId = $sessionId
} | ConvertTo-Json

try {
    $response2 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $request2 -ContentType "application/json"
    Write-Host "✅ Bot Response:" -ForegroundColor Green
    Write-Host "   $($response2.message)" -ForegroundColor Gray
    if ($response2.sources) {
        Write-Host "   Top Source: $($response2.sources[0].sourceType) (similarity: $($response2.sources[0].similarity))" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 5: FAQ Query
Write-Host "Test 5: Chat - Ask about policy" -ForegroundColor Yellow
$request3 = @{
    message = "Đổi trả trong bao lâu?"
    sessionId = $sessionId
} | ConvertTo-Json

try {
    $response3 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $request3 -ContentType "application/json"
    Write-Host "✅ Bot Response:" -ForegroundColor Green
    Write-Host "   $($response3.message)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 6: Get Conversation History
Write-Host "Test 6: Get Conversation History" -ForegroundColor Yellow
try {
    $history = Invoke-RestMethod -Uri "$baseUrl/conversations/$sessionId" -Method Get
    Write-Host "✅ Messages in conversation: $($history.Count)" -ForegroundColor Green
    foreach ($msg in $history) {
        $role = if ($msg.role -eq "user") { "👤 User" } else { "🤖 Bot" }
        Write-Host "   $role : $($msg.content.Substring(0, [Math]::Min(50, $msg.content.Length)))..." -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "=================================" -ForegroundColor Cyan
Write-Host "✅ All tests completed!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Open Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor Gray
Write-Host "2. Test chat endpoint with different queries" -ForegroundColor Gray
Write-Host "3. Upload PDFs via: POST /api/v1/chat/admin/upload-pdf" -ForegroundColor Gray
