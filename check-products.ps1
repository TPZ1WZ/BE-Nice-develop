# Kiem tra danh sach san pham trong shop
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$baseUrl = "http://localhost:8080"

Write-Host "=== KIEM TRA SAN PHAM NIKE STORE ===" -ForegroundColor Cyan
Write-Host ""

# Get products from API
Write-Host "Dang lay danh sach san pham..." -ForegroundColor Yellow
try {
    $products = Invoke-RestMethod -Uri "$baseUrl/api/v1/products" -Method Get
    
    Write-Host "Tong so san pham: $($products.Count)" -ForegroundColor Green
    Write-Host ""
    
    Write-Host "DANH SACH SAN PHAM:" -ForegroundColor Cyan
    Write-Host "=====================" -ForegroundColor Cyan
    
    $count = 0
    foreach ($product in $products) {
        $count++
        if ($count -le 20) {  # Chi hien thi 20 san pham dau
            $price = "{0:N0}" -f $product.price
            $stock = if ($product.stock) { $product.stock } else { "N/A" }
            
            Write-Host ""
            Write-Host "[$count] $($product.name)" -ForegroundColor White
            Write-Host "    ID: $($product.id)" -ForegroundColor Gray
            Write-Host "    Gia: $price VND" -ForegroundColor Yellow
            Write-Host "    Ton kho: $stock" -ForegroundColor Gray
            
            if ($product.category) {
                Write-Host "    Loai: $($product.category.name)" -ForegroundColor Gray
            }
        }
    }
    
    if ($products.Count -gt 20) {
        Write-Host ""
        Write-Host "... va $($products.Count - 20) san pham khac" -ForegroundColor Gray
    }
    
    Write-Host ""
    Write-Host "=====================" -ForegroundColor Cyan
    
    # Thong ke
    $avgPrice = ($products | Measure-Object -Property price -Average).Average
    $minPrice = ($products | Measure-Object -Property price -Minimum).Minimum
    $maxPrice = ($products | Measure-Object -Property price -Maximum).Maximum
    
    Write-Host ""
    Write-Host "THONG KE:" -ForegroundColor Cyan
    Write-Host "  Gia thap nhat: {0:N0} VND" -f $minPrice -ForegroundColor Green
    Write-Host "  Gia cao nhat: {0:N0} VND" -f $maxPrice -ForegroundColor Green
    Write-Host "  Gia trung binh: {0:N0} VND" -f $avgPrice -ForegroundColor Green
    
    # Dem san pham duoi 2 trieu
    $under2M = ($products | Where-Object { $_.price -lt 2000000 }).Count
    Write-Host "  San pham duoi 2 trieu: $under2M" -ForegroundColor Yellow
    
} catch {
    Write-Host "LOI: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Chi tiet: $($_.ErrorDetails.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== KET THUC ===" -ForegroundColor Cyan
