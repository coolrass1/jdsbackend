# Test script to verify client creation with assigned users
$baseUrl = "http://localhost:5000/api"

Write-Host "=== Create Client With Users Test ===" -ForegroundColor Cyan
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

# Step 2: Get User ID (admin) /api/users
try {
    $users = Invoke-RestMethod -Uri "$baseUrl/users" -Method Get -Headers $headers
    $userId = $users[0].id
    Write-Host "✓ Will assign User ID: $userId ($($users[0].username))" -ForegroundColor Gray
}
catch {
    Write-Host "✗ Failed to get users" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 3: Create Client with Assigned User
Write-Host "Step 3: Creating client with assigned user..." -ForegroundColor Yellow
$randomInt = Get-Random -Minimum 1000 -Maximum 9999
$clientBody = @{
    firstname       = "Test"
    lastname        = "UserAssigned$randomInt"
    email           = "test.assigned$randomInt@example.com"
    ni_number       = "AB${randomInt}CD"
    phone           = "0123456789"
    assignedUserIds = @($userId)
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/clients" -Method Post -Body $clientBody -Headers $headers -ContentType "application/json"
    Write-Host "✓ Client created: $($response.id)" -ForegroundColor Green
    
    if ($response.assignedUsers.Count -gt 0) {
        Write-Host "✓ Verified: Client has $($response.assignedUsers.Count) assigned users." -ForegroundColor Green
        $response.assignedUsers | ForEach-Object {
            if ($_.id -eq $userId) {
                Write-Host "  - Match: $($_.username)" -ForegroundColor Cyan
            }
            else {
                Write-Host "  - Unexpected user: $($_.username)" -ForegroundColor Red
            }
        }
    }
    else {
        Write-Host "✗ Failed: No users assigned!" -ForegroundColor Red
    }

}
catch {
    Write-Host "✗ Failed to create client: $_" -ForegroundColor Red
    # Print error details if available
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "  Details: $($reader.ReadToEnd())" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Cyan
