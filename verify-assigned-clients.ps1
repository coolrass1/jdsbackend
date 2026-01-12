# Test script to verify the 'assigned clients' endpoint
$baseUrl = "http://localhost:5000/api"

Write-Host "=== Assigned Clients Endpoint Test ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Login as Caseworker
Write-Host "Step 1: Logging in as Caseworker..." -ForegroundColor Yellow
$loginBody = @{
    username = "caseworker"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.accessToken
    Write-Host "✓ Login successful" -ForegroundColor Green
} catch {
    Write-Host "✗ Login failed. Ensure app is running!" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host ""

# Step 2: Get Assigned Clients
Write-Host "Step 2: Fetching assigned clients..." -ForegroundColor Yellow
try {
    $clients = Invoke-RestMethod -Uri "$baseUrl/clients/assigned" -Method Get -Headers $headers
    Write-Host "✓ Request successful" -ForegroundColor Green
    Write-Host "Found $($clients.Count) assigned clients:" -ForegroundColor Gray
    
    $clients | ForEach-Object {
        Write-Host "  - $($_.firstname) $($_.lastname) ($($_.company))" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ Failed to get assigned clients: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Cyan
