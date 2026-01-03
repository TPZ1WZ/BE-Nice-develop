# Script để seed dữ liệu Lucky Wheel vào PostgreSQL
# Chạy script này để thêm phần thưởng vào database

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "LUCKY WHEEL - SEED PRIZES DATA" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Database configuration
$DB_HOST = "localhost"
$DB_PORT = "5433"
$DB_NAME = "nike_store"
$DB_USER = "postgres"
$DB_PASSWORD = "12345"
$SQL_FILE = "db\lucky_wheel_init.sql"

# Set password environment variable
$env:PGPASSWORD = $DB_PASSWORD

Write-Host "📊 Database: $DB_NAME" -ForegroundColor Yellow
Write-Host "🏠 Host: ${DB_HOST}:${DB_PORT}" -ForegroundColor Yellow
Write-Host "📁 SQL File: $SQL_FILE" -ForegroundColor Yellow
Write-Host ""

# Check if SQL file exists
if (-Not (Test-Path $SQL_FILE)) {
    Write-Host "❌ ERROR: SQL file not found: $SQL_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "🔄 Running SQL script..." -ForegroundColor Green
Write-Host ""

# Run the SQL script
try {
    & psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f $SQL_FILE
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✅ SUCCESS! Prizes data has been seeded successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "📋 Summary:" -ForegroundColor Cyan
        Write-Host "  - 6 prizes added to database" -ForegroundColor White
        Write-Host "  - Probability distribution:" -ForegroundColor White
        Write-Host "    • Giảm 10%: 25%" -ForegroundColor White
        Write-Host "    • Giảm 20%: 15%" -ForegroundColor White
        Write-Host "    • Freeship: 20%" -ForegroundColor White
        Write-Host "    • Điểm thưởng: 20%" -ForegroundColor White
        Write-Host "    • Quà tặng: 5%" -ForegroundColor White
        Write-Host "    • Chúc may mắn: 15%" -ForegroundColor White
        Write-Host ""
        Write-Host "🎮 Now you can test the Lucky Wheel in the app!" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "❌ ERROR: Failed to seed data. Check the error messages above." -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host ""
    Write-Host "❌ ERROR: $_" -ForegroundColor Red
    exit 1
} finally {
    # Clear password from environment
    Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
