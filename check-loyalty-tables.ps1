# Check if loyalty tables exist and have data

Write-Host "Checking loyalty system tables..." -ForegroundColor Cyan

# Database connection details (adjust if needed)
$env:PGPASSWORD = "your_password_here"
$dbHost = "localhost"
$dbPort = "5432"
$dbName = "nike_store"
$dbUser = "postgres"

# Check if daily_checkin_rewards table exists and has data
Write-Host "`nChecking daily_checkin_rewards table:" -ForegroundColor Yellow
psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -c "SELECT COUNT(*) as total_rewards FROM daily_checkin_rewards;"
psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -c "SELECT * FROM daily_checkin_rewards ORDER BY day_number;"

# Check if daily_checkins table exists
Write-Host "`nChecking daily_checkins table:" -ForegroundColor Yellow
psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -c "SELECT COUNT(*) as total_checkins FROM daily_checkins;"

# Check if loyalty_transactions table exists
Write-Host "`nChecking loyalty_transactions table:" -ForegroundColor Yellow
psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -c "SELECT COUNT(*) as total_transactions FROM loyalty_transactions;"

Write-Host "`nDone!" -ForegroundColor Green
