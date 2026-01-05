# Test Nike Coin Checkin API

Write-Host "Testing Nike Coin Loyalty API..." -ForegroundColor Cyan

# Test 1: Login
Write-Host "[1] Login..." -ForegroundColor Yellow
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -ContentType "application/json" -Body '{"email":"tanphat123@example.com","password":"password123"}' -SessionVariable session

if ($loginResponse) {
    Write-Host "Login successful!" -ForegroundColor Green
    Write-Host "User: $($loginResponse.email)"
    
    # Extract token
    $token = $null
    foreach ($cookie in $session.Cookies.GetCookies("http://localhost:8080")) {
        if ($cookie.Name -eq "access_token") {
            $token = $cookie.Value
            Write-Host "Token found" -ForegroundColor Green
            break
        }
    }
    
    if ($token) {
        # Test 2: Get points
        Write-Host "[2] Getting points..." -ForegroundColor Yellow
        try {
            $headers = @{ "Authorization" = "Bearer $token" }
            $pointsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/loyalty/points" -Method GET -Headers $headers
            Write-Host "Current points: $($pointsResponse.currentPoints)" -ForegroundColor Green
        } catch {
            Write-Host "Failed: $_" -ForegroundColor Red
        }
        
        # Test 3: Get streak
        Write-Host "[3] Getting streak..." -ForegroundColor Yellow
        try {
            $streakResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/loyalty/checkin/streak" -Method GET -Headers $headers
            Write-Host "Current streak: $($streakResponse.currentStreak)" -ForegroundColor Green
            Write-Host "Today reward: $($streakResponse.todayReward)" -ForegroundColor Cyan
        } catch {
            Write-Host "Failed: $_" -ForegroundColor Red
            Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        }
        
        # Test 4: Checkin
        Write-Host "[4] Performing checkin..." -ForegroundColor Yellow
        try {
            $checkinResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/loyalty/checkin" -Method POST -Headers $headers
            Write-Host "Success: $($checkinResponse.success)" -ForegroundColor Cyan
            Write-Host "Message: $($checkinResponse.message)" -ForegroundColor Cyan
        } catch {
            Write-Host "Failed!" -ForegroundColor Red
            Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        }
        
    } else {
        Write-Host "No token" -ForegroundColor Red
    }
} else {
    Write-Host "Login failed!" -ForegroundColor Red
}

Write-Host "Done!" -ForegroundColor Green
