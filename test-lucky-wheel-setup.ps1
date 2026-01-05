# Test Lucky Wheel Setup Script
Write-Host "=== Testing Lucky Wheel Setup ===" -ForegroundColor Cyan

# Test if rewards exist
Write-Host "`nChecking lucky wheel rewards in database..." -ForegroundColor Yellow

$query = "SELECT COUNT(*) as count FROM lucky_wheel_rewards WHERE is_active = true;"
docker exec -i nike_postgres psql -U root -d nike_store -c "$query"

Write-Host "`nChecking all rewards with details..." -ForegroundColor Yellow
$detailQuery = "SELECT id, position, reward_type, coin_amount, probability, label, is_active FROM lucky_wheel_rewards ORDER BY position;"
docker exec -i nike_postgres psql -U root -d nike_store -c "$detailQuery"

# If no rewards, insert them
$countResult = docker exec -i nike_postgres psql -U root -d nike_store -t -c "SELECT COUNT(*) FROM lucky_wheel_rewards WHERE is_active = true;" | ForEach-Object { $_.Trim() }

if ($countResult -eq "0") {
    Write-Host "`nNo active rewards found. Inserting rewards..." -ForegroundColor Red
    
    $insertQuery = @"
INSERT INTO lucky_wheel_rewards (position, reward_type, coin_amount, probability, icon_name, label, is_active)
VALUES
    (0, 'COIN', 1000, 30.00, 'ic_coin', '1,000 Coin', true),
    (1, 'NOTHING', NULL, 20.00, 'ic_sad', 'Chúc bạn may mắn lần sau', true),
    (2, 'COIN', 2000, 15.00, 'ic_coin_stack', '2,000 Coin', true),
    (3, 'COIN', 500, 15.00, 'ic_coin_small', '500 Coin', true),
    (4, 'COIN', 100, 10.00, 'ic_coin_tiny', '100 Coin', true),
    (5, 'COIN', 1000, 5.00, 'ic_coin', '1,000 Coin', true),
    (6, 'COIN', 50, 3.00, 'ic_coin_micro', '50 Coin', true),
    (7, 'COIN', 200, 2.00, 'ic_coin_mini', '200 Coin', true)
ON CONFLICT DO NOTHING;
"@
    
    docker exec -i nike_postgres psql -U root -d nike_store -c "$insertQuery"
    Write-Host "Rewards inserted successfully!" -ForegroundColor Green
} else {
    Write-Host "`nFound $countResult active rewards. Database is ready!" -ForegroundColor Green
}

Write-Host "`n=== Setup Complete ===" -ForegroundColor Cyan
