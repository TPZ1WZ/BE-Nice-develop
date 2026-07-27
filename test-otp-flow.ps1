# Test OTP Flow for shincam2095@gmail.com
$baseUrl = "http://localhost:8080"
$email = "shincam2095@gmail.com"

Write-Host "================================" -ForegroundColor Cyan
Write-Host "🧪 TESTING OTP FLOW" -ForegroundColor Cyan
Write-Host "Email: $email" -ForegroundColor Yellow
Write-Host "================================`n" -ForegroundColor Cyan

# Step 1: Register (or resend OTP if already registered)
Write-Host "📝 Step 1: Registering user..." -ForegroundColor Green

$registerBody = @{
    fullName = "Test User"
    email = $email
    phone = "0123456789"
    password = "Test123456"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerBody `
        -ErrorAction Stop
    
    Write-Host "Registration response:" -ForegroundColor Green
    Write-Host ($registerResponse | ConvertTo-Json -Depth 3) -ForegroundColor White
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorBody = $_.ErrorDetails.Message | ConvertFrom-Json
    
    if ($statusCode -eq 400 -and $errorBody.message -like "*already registered*") {
        Write-Host "User already registered, trying to resend OTP..." -ForegroundColor Yellow
        
        # Try resend OTP
        $resendBody = @{
            email = $email
        } | ConvertTo-Json
        
        try {
            $resendResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/resend-registration-otp" `
                -Method POST `
                -ContentType "application/json" `
                -Body $resendBody `
                -ErrorAction Stop
            
            Write-Host "OTP resent successfully!" -ForegroundColor Green
            Write-Host ($resendResponse | ConvertTo-Json -Depth 3) -ForegroundColor White
        } catch {
            Write-Host "Failed to resend OTP: $($_.Exception.Message)" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "Registration failed: $($errorBody.message)" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n================================" -ForegroundColor Cyan
Write-Host "📧 Check your email for OTP code" -ForegroundColor Yellow
Write-Host "Or check MailHog at: http://localhost:8025" -ForegroundColor Yellow
Write-Host "================================`n" -ForegroundColor Cyan

# Step 2: Get OTP from user
$otp = Read-Host "Enter the 6-digit OTP code"

if ($otp.Length -ne 6) {
    Write-Host "OTP must be 6 digits!" -ForegroundColor Red
    exit 1
}

Write-Host "`n🔢 Step 2: Verifying OTP: $otp" -ForegroundColor Green

$verifyBody = @{
    email = $email
    otp = [long]$otp
} | ConvertTo-Json

try {
    $verifyResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/verify-registration-otp" `
        -Method POST `
        -ContentType "application/json" `
        -Body $verifyBody `
        -ErrorAction Stop
    
    Write-Host "OTP Verification SUCCESS!" -ForegroundColor Green
    Write-Host ($verifyResponse | ConvertTo-Json -Depth 3) -ForegroundColor White
    
    if ($verifyResponse.success) {
        Write-Host "`nRegistration completed! You can now login." -ForegroundColor Green
    }
} catch {
    $errorBody = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "OTP Verification FAILED!" -ForegroundColor Red
    Write-Host "Error: $($errorBody.message)" -ForegroundColor Red
    Write-Host "Full response:" -ForegroundColor Yellow
    Write-Host ($errorBody | ConvertTo-Json -Depth 3) -ForegroundColor White
    exit 1
}

Write-Host "`n================================" -ForegroundColor Cyan
Write-Host "Test completed successfully!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Cyan
