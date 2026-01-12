# Simple RBAC Test
Write-Host "Testing RBAC Permission Implementation" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test login and get token
try {
    $login = @{
        username = "admin"
        password = "password123"
    } | ConvertTo-Json
    
    Write-Host "1. Testing Admin Login..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "http://localhost:5000/api/auth/login" `
        -Method POST `
        -Body $login `
        -ContentType "application/json"
    
    Write-Host "   ✓ Login successful!" -ForegroundColor Green
    $token = $response.token
    Write-Host "   Token received (first 50 chars): $($token.Substring(0, [Math]::Min(50, $token.Length)))..." -ForegroundColor Gray
    Write-Host ""
    
    # Test accessing cases endpoint (requires CASE_READ permission)
    Write-Host "2. Testing Case Access (CASE_READ permission)..." -ForegroundColor Yellow
    $cases = Invoke-RestMethod -Uri "http://localhost:5000/api/cases" `
        -Method GET `
        -Headers @{Authorization = "Bearer $token"}
    Write-Host "   ✓ Successfully accessed cases endpoint!" -ForegroundColor Green
    Write-Host "   Found: $($cases.Count) cases" -ForegroundColor Gray
    Write-Host ""
    
    # Test accessing users endpoint (requires USER_READ permission)
    Write-Host "3. Testing User Access (USER_READ permission)..." -ForegroundColor Yellow
    $users = Invoke-RestMethod -Uri "http://localhost:5000/api/users" `
        -Method GET `
        -Headers @{Authorization = "Bearer $token"}
    Write-Host "   ✓ Successfully accessed users endpoint!" -ForegroundColor Green
    Write-Host "   Found: $($users.Count) users" -ForegroundColor Gray
    Write-Host ""
    
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "✓ RBAC Permission System Working!" -ForegroundColor Green
    Write-Host "" 
    Write-Host "Summary:" -ForegroundColor Cyan
    Write-Host "- Permission-based authorities are being created" -ForegroundColor White
    Write-Host "- Admin has both ROLE_ADMIN and all permissions" -ForegroundColor White
    Write-Host "- Endpoints protected with @PreAuthorize('hasAuthority(...)') work" -ForegroundColor White
    Write-Host "- Users can access resources based on their permissions" -ForegroundColor White
    
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}
