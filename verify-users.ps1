# Test script to fetch all users
$baseUrl = "http://localhost:5000/api"

Write-Host "=== Fetch All Users Test ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Login
Write-Host "Step 1: Logging in..." -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.accessToken
    Write-Host "✓ Login successful" -ForegroundColor Green
}
catch {
    Write-Host "✗ Login failed" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type"  = "application/json"
}

Write-Host ""

# Step 2: Get All Users
Write-Host "Step 2: Fetching all users from /api/users..." -ForegroundColor Yellow
try {
    $users = Invoke-RestMethod -Uri "$baseUrl/users" -Method Get -Headers $headers
    Write-Host "✓ Success" -ForegroundColor Green
    Write-Host "Found $($users.Count) users:" -ForegroundColor Gray
    
    $users | ForEach-Object {
        Write-Host "  - ID: $($_.id) | User: $($_.username) | Email: $($_.email)" -ForegroundColor Cyan
        if ($_.assignedClients) {
            Write-Host "    Clients: $($_.assignedClients.Count)" -ForegroundColor Gray
        }
    }
}
catch {
    Write-Host "✗ Failed to fetch users: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Cyan
