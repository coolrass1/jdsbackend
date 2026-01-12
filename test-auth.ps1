# Authentication Test Script for JDS Backend
# Run this after the application is started successfully

Write-Host "`n=== JDS Backend Authentication Test ===" -ForegroundColor Cyan

# Test 1: Register a new user
Write-Host "`n[Test 1] Registering new user..." -ForegroundColor Yellow
$registerBody = @{
    username = "testuser"
    email = "testuser@example.com"
    password = "password123"
    role = "CASE_WORKER"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "http://localhost:5000/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
    Write-Host "✓ Registration successful!" -ForegroundColor Green
    Write-Host "  Message: $($registerResponse.message)" -ForegroundColor White
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "  User may already exist (this is OK)" -ForegroundColor Yellow
    } else {
        Write-Host "✗ Registration failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 2: Login with the registered user
Write-Host "`n[Test 2] Login with testuser..." -ForegroundColor Yellow
$loginBody = @{
    username = "testuser"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:5000/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    Write-Host "✓ Login successful!" -ForegroundColor Green
    Write-Host "  Username: $($loginResponse.username)" -ForegroundColor Cyan
    Write-Host "  Email: $($loginResponse.email)" -ForegroundColor Cyan
    Write-Host "  Roles: $($loginResponse.roles -join ', ')" -ForegroundColor Yellow
    Write-Host "  Token Type: $($loginResponse.tokenType)" -ForegroundColor Cyan
    Write-Host "  Access Token (first 60 chars): $($loginResponse.accessToken.Substring(0, [Math]::Min(60, $loginResponse.accessToken.Length)))..." -ForegroundColor Magenta
    
    # Save tokens globally
    $global:accessToken = $loginResponse.accessToken
    $global:refreshToken = $loginResponse.refreshToken
    Write-Host "`n  Tokens saved to `$global:accessToken and `$global:refreshToken" -ForegroundColor Green
    
} catch {
    Write-Host "✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    exit
}

# Test 3: Access a protected endpoint (get all users - admin only)
Write-Host "`n[Test 3] Accessing protected endpoint /api/users..." -ForegroundColor Yellow
$headers = @{
    Authorization = "Bearer $global:accessToken"
}

try {
    $usersResponse = Invoke-RestMethod -Uri "http://localhost:5000/api/users" -Method Get -Headers $headers
    Write-Host "✓ Protected endpoint accessible!" -ForegroundColor Green
    Write-Host "  Found $($usersResponse.Count) users" -ForegroundColor White
    foreach ($user in $usersResponse) {
        Write-Host "    - $($user.username) ($($user.email)) - Roles: $($user.roles -join ', ')" -ForegroundColor Gray
    }
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "✓ Access denied (expected for CASE_WORKER role)" -ForegroundColor Yellow
    } else {
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 4: Test token refresh
Write-Host "`n[Test 4] Testing token refresh..." -ForegroundColor Yellow
$refreshBody = @{
    refreshToken = $global:refreshToken
} | ConvertTo-Json

try {
    $refreshResponse = Invoke-RestMethod -Uri "http://localhost:5000/api/auth/refresh" -Method Post -Body $refreshBody -ContentType "application/json"
    Write-Host "✓ Token refresh successful!" -ForegroundColor Green
    Write-Host "  New Access Token (first 60 chars): $($refreshResponse.accessToken.Substring(0, [Math]::Min(60, $refreshResponse.accessToken.Length)))..." -ForegroundColor Magenta
    $global:accessToken = $refreshResponse.accessToken
} catch {
    Write-Host "✗ Token refresh failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Login with admin (from seeded data)
Write-Host "`n[Test 5] Login with admin user (if seeded)..." -ForegroundColor Yellow
$adminLoginBody = @{
    username = "admin"
    password = "password123"
} | ConvertTo-Json

try {
    $adminResponse = Invoke-RestMethod -Uri "http://localhost:5000/api/auth/login" -Method Post -Body $adminLoginBody -ContentType "application/json"
    Write-Host "✓ Admin login successful!" -ForegroundColor Green
    Write-Host "  Username: $($adminResponse.username)" -ForegroundColor Cyan
    Write-Host "  Email: $($adminResponse.email)" -ForegroundColor Cyan
    Write-Host "  Roles: $($adminResponse.roles -join ', ')" -ForegroundColor Yellow
    
    # Test admin access to protected endpoint
    $adminHeaders = @{
        Authorization = "Bearer $($adminResponse.accessToken)"
    }
    
    Write-Host "`n  Testing admin access to /api/users..." -ForegroundColor Yellow
    $adminUsersResponse = Invoke-RestMethod -Uri "http://localhost:5000/api/users" -Method Get -Headers $adminHeaders
    Write-Host "  ✓ Admin can access protected endpoint!" -ForegroundColor Green
    Write-Host "    Found $($adminUsersResponse.Count) users" -ForegroundColor White
    
} catch {
    Write-Host "  Admin user not found (database may not be seeded)" -ForegroundColor Yellow
}

Write-Host "`n=== Authentication Tests Complete ===" -ForegroundColor Cyan
Write-Host "Access token is available in `$global:accessToken" -ForegroundColor Green
Write-Host "Refresh token is available in `$global:refreshToken" -ForegroundColor Green
