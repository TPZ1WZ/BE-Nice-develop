# Setup Lucky Wheel System

Write-Host "Setting up Lucky Wheel System..." -ForegroundColor Cyan

$dbHost = "localhost"
$dbPort = "5432"
$dbName = "nike_store"
$dbUser = "postgres"
$dbPassword = "123"

$env:PGPASSWORD = $dbPassword

Write-Host "[1] Creating tables..." -ForegroundColor Yellow
psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -f "db\lucky_wheel_migration.sql"

if ($LASTEXITCODE -eq 0) {
    Write-Host "SUCCESS! Lucky Wheel tables created!" -ForegroundColor Green
    
    Write-Host "`n[2] Verifying rewards data..." -ForegroundColor Yellow
    psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -c "SELECT position, reward_type, coin_amount, label, probability FROM lucky_wheel_rewards ORDER BY position;"
    
    Write-Host "`nLucky Wheel System is ready!" -ForegroundColor Green
    Write-Host "Features:" -ForegroundColor Cyan
    Write-Host "  - 1 free spin per day" -ForegroundColor White
    Write-Host "  - 500 coin per additional spin" -ForegroundColor White
    Write-Host "  - 8 reward slots with different probabilities" -ForegroundColor White
    Write-Host "  - Jackpot: 10,000 coin (1% chance)" -ForegroundColor Yellow
} else {
    Write-Host "ERROR: Failed to create tables" -ForegroundColor Red
}

$env:PGPASSWORD = $null
