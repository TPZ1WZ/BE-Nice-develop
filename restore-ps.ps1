# PowerShell script to restore database with proper encoding handling
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   RESTORE DATABASE - PowerShell Method" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$SQL_FILE = "nike_store_export_20260101_160831.sql"

if (-not (Test-Path $SQL_FILE)) {
    Write-Host "ERROR: File not found: $SQL_FILE" -ForegroundColor Red
    pause
    exit 1
}

Write-Host "[1/6] Reading SQL file and fixing encoding..." -ForegroundColor Yellow
# Read file with UTF8 and remove BOM
$content = Get-Content $SQL_FILE -Raw -Encoding UTF8
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
$fixedFile = "nike_store_fixed.sql"
[System.IO.File]::WriteAllText($fixedFile, $content, $utf8NoBom)
Write-Host "OK - Fixed file created: $fixedFile" -ForegroundColor Green
Write-Host ""

Write-Host "[2/6] Terminating active connections..." -ForegroundColor Yellow
docker exec cps_postgres psql -U cps_user -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'cps_db' AND pid <> pg_backend_pid();" 2>$null | Out-Null
Write-Host "OK" -ForegroundColor Green
Write-Host ""

Write-Host "[3/6] Recreating database..." -ForegroundColor Yellow
docker exec cps_postgres psql -U cps_user -d postgres -c "DROP DATABASE IF EXISTS cps_db;" 2>$null | Out-Null
docker exec cps_postgres psql -U cps_user -d postgres -c "CREATE DATABASE cps_db WITH ENCODING = 'UTF8' TEMPLATE = template0;" 2>&1 | Select-String -Pattern "CREATE DATABASE" | Out-Null
Write-Host "OK - Fresh database created" -ForegroundColor Green
Write-Host ""

Write-Host "[4/6] Copying fixed SQL file to container..." -ForegroundColor Yellow
docker cp $fixedFile cps_postgres:/tmp/restore.sql
Write-Host "OK" -ForegroundColor Green
Write-Host ""

Write-Host "[5/6] Restoring database (1-2 minutes)..." -ForegroundColor Yellow
docker exec cps_postgres psql -U cps_user -d cps_db -f /tmp/restore.sql 2>&1 | Where-Object { $_ -notmatch "WARNING" }
Write-Host ""

Write-Host "[6/6] Verifying data..." -ForegroundColor Yellow
Write-Host ""
Write-Host "Tables:" -ForegroundColor Cyan
docker exec cps_postgres psql -U cps_user -d cps_db -c "\dt" 2>$null
Write-Host ""
Write-Host "Record counts:" -ForegroundColor Cyan
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT 'Products' as table_name, COUNT(*)::text as records FROM product UNION ALL SELECT 'Users', COUNT(*)::text FROM users UNION ALL SELECT 'Orders', COUNT(*)::text FROM orders UNION ALL SELECT 'Categories', COUNT(*)::text FROM category;" 2>$null
Write-Host ""

Write-Host "========================================" -ForegroundColor Green
Write-Host "   RESTORE COMPLETED!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
pause
