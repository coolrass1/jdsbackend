# Test Client Creation
$baseUrl = "http://localhost:5000/api"

Write-Host "Testing Client Creation..." -ForegroundColor Cyan

# First, login to get a token
Write-Host "`n1. Logging in..." -ForegroundColor Yellow
$loginBody = '{"username":"admin","password":"password123"}'

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.accessToken
    Write-Host "Login successful" -ForegroundColor Green
} catch {
    Write-Host "Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Create a new client
Write-Host "`n2. Creating a new client..." -ForegroundColor Yellow
$clientBody = '{"firstname":"John","lastname":"Doe","email":"john.doe@example.com","ni_number":"AB123456C","phone":"+1-555-0123","address":"123 Main St, New York, NY 10001","company":"Tech Corp","hasConflictOfInterest":false}'

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $clientResponse = Invoke-RestMethod -Uri "$baseUrl/clients" -Method POST -Body $clientBody -Headers $headers
    Write-Host "Client created successfully!" -ForegroundColor Green
    Write-Host "`nClient Details:" -ForegroundColor Cyan
    Write-Host "  ID: $($clientResponse.id)"
    Write-Host "  Name: $($clientResponse.firstname) $($clientResponse.lastname)"
    Write-Host "  Email: $($clientResponse.email)"
    Write-Host "  NI Number: $($clientResponse.ni_number)"
    Write-Host "  Phone: $($clientResponse.phone)"
    Write-Host "  Has Conflict: $($clientResponse.hasConflictOfInterest)"
} catch {
    Write-Host "Client creation failed" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    exit 1
}

Write-Host "`nAll tests completed!" -ForegroundColor Green
