# Test auto sync chatbot
Write-Host '=====================================' -ForegroundColor Cyan
Write-Host 'TEST TU DONG DONG BO CHATBOT' -ForegroundColor Cyan
Write-Host '=====================================' -ForegroundColor Cyan

$baseUrl = 'http://localhost:8080'
$testProductName = 'Nike Air Test ' + (Get-Date -Format 'HHmmss')

# Step 1: Check current product count
Write-Host '1. Kiem tra so luong san pham hien tai...' -ForegroundColor Yellow
try {
    $statsBefore = Invoke-RestMethod -Uri "$baseUrl/api/v1/chat/admin/stats" -Method Get
    Write-Host "   So san pham hien tai: $($statsBefore.product_documents)" -ForegroundColor White
} catch {
    Write-Host "   Loi: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Create new product
Write-Host '2. Tao san pham moi...' -ForegroundColor Yellow
$newProduct = @{
    name = $testProductName
    subTitle = 'Test Product for Auto Sync'
    description = 'San pham test tu dong dong bo chatbot'
    price = 1500000
    stock = 100
    categoryId = 1
    images = @('https://via.placeholder.com/500')
    sizes = @('39', '40', '41', '42')
} | ConvertTo-Json

try {
    $createdProduct = Invoke-RestMethod -Uri "$baseUrl/api/v1/products" -Method Post -Body $newProduct -ContentType 'application/json'
    Write-Host "   San pham da tao voi ID: $($createdProduct.id)" -ForegroundColor Green
    $productId = $createdProduct.id
    Start-Sleep -Seconds 2
} catch {
    Write-Host "   Loi: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 3: Check if chatbot updated
Write-Host '3. Kiem tra chatbot da cap nhat chua...' -ForegroundColor Yellow
try {
    $statsAfter = Invoke-RestMethod -Uri "$baseUrl/api/v1/chat/admin/stats" -Method Get
    Write-Host "   So san pham sau khi tao: $($statsAfter.product_documents)" -ForegroundColor White
    
    if ($statsAfter.product_documents -gt $statsBefore.product_documents) {
        Write-Host '   SUCCESS! Chatbot da tu dong cap nhat' -ForegroundColor Green
    } else {
        Write-Host '   Chatbot chua cap nhat' -ForegroundColor Yellow
    }
} catch {
    Write-Host "   Loi: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 4: Delete test product
Write-Host '4. Xoa san pham test...' -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$baseUrl/api/v1/products/$productId" -Method Delete
    Write-Host '   Da xoa san pham test' -ForegroundColor Green
    Start-Sleep -Seconds 2
} catch {
    Write-Host "   Loi: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 5: Final check
Write-Host '5. Kiem tra so luong cuoi cung...' -ForegroundColor Yellow
try {
    $statsFinal = Invoke-RestMethod -Uri "$baseUrl/api/v1/chat/admin/stats" -Method Get
    Write-Host "   So san pham cuoi: $($statsFinal.product_documents)" -ForegroundColor White
} catch {
    Write-Host "   Loi: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ''
Write-Host 'HOAN TAT TEST' -ForegroundColor Green
