# Test script to verify seeded data
$baseUrl = "http://localhost:5000/api"

Write-Host "=== Seed Verification Script ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Login
Write-Host "Step 1: Logging in as Admin..." -ForegroundColor Yellow
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
    Write-Host "✗ Login failed. Make sure the application is running!" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type"  = "application/json"
}

Write-Host ""

# Step 2: Check Users
Write-Host "Step 2: Checking Users..." -ForegroundColor Yellow
try {
    $users = Invoke-RestMethod -Uri "$baseUrl/users" -Method Get -Headers $headers
    Write-Host "✓ Found $($users.Count) users" -ForegroundColor Green
    $users | ForEach-Object {
        Write-Host "  - $($_.username) ($($_.email))" -ForegroundColor Gray
    }
}
catch {
    Write-Host "✗ Failed to get users: $_" -ForegroundColor Red
}

Write-Host ""

# Step 3: Check Clients and Assignments
Write-Host "Step 3: Checking Clients and Assignments..." -ForegroundColor Yellow
try {
    $clients = Invoke-RestMethod -Uri "$baseUrl/clients" -Method Get -Headers $headers
    Write-Host "✓ Found $($clients.Count) clients" -ForegroundColor Green
    
    foreach ($client in $clients) {
        Write-Host "  Client: $($client.firstname) $($client.lastname) ($($client.company))" -ForegroundColor Cyan
        
        try {
            $clientDetail = Invoke-RestMethod -Uri "$baseUrl/clients/$($client.id)" -Method Get -Headers $headers
            $assignedCount = $clientDetail.assignedUsers.Count
            Write-Host "    Assigned Users ($assignedCount):" -ForegroundColor Gray
             
            if ($assignedCount -gt 0) {
                $clientDetail.assignedUsers | ForEach-Object {
                    Write-Host "      - $($_.username)" -ForegroundColor Green
                }
            }
            else {
                Write-Host "      (None)" -ForegroundColor DarkGray
            }
        }
        catch {
            Write-Host "    ✗ Failed to fetch details" -ForegroundColor Red
        }
    }
}
catch {
    Write-Host "✗ Failed to get clients: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Verification Complete ===" -ForegroundColor Cyan
