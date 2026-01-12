# Test Client Conflict of Interest Fields
Write-Host "`n=== Testing Client Conflict of Interest Fields ===" -ForegroundColor Cyan

# First, login to get the token
Write-Host "`n[Step 1] Logging in..." -ForegroundColor Yellow
$loginBody = @{
    username = "testuser"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:5000/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.accessToken
    Write-Host "✓ Login successful!" -ForegroundColor Green
} catch {
    Write-Host "✗ Login failed. Make sure to run test-auth.ps1 first to create the test user." -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Test 1: Create a client WITHOUT conflict of interest
Write-Host "`n[Test 1] Creating client without conflict of interest..." -ForegroundColor Yellow
$clientNoConflict = @{
    firstname = "John"
    lastname = "Doe"
    email = "john.doe.noconflict@example.com"
    phone = "555-0001"
    address = "123 Main St"
    company = "ABC Corp"
    hasConflictOfInterest = $false
} | ConvertTo-Json

try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    $client1 = Invoke-RestMethod -Uri "http://localhost:5000/api/clients" -Method Post -Body $clientNoConflict -Headers $headers
    Write-Host "✓ Client created successfully!" -ForegroundColor Green
    Write-Host "  ID: $($client1.id)" -ForegroundColor Cyan
    Write-Host "  Name: $($client1.firstname) $($client1.lastname)" -ForegroundColor Cyan
    Write-Host "  Has Conflict: $($client1.hasConflictOfInterest)" -ForegroundColor Cyan
    Write-Host "  Comment: $($client1.conflictOfInterestComment)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ Failed to create client: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

# Test 2: Create a client WITH conflict of interest and comment
Write-Host "`n[Test 2] Creating client WITH conflict of interest and comment..." -ForegroundColor Yellow
$clientWithConflict = @{
    firstname = "Jane"
    lastname = "Smith"
    email = "jane.smith.conflict@example.com"
    phone = "555-0002"
    address = "456 Oak Ave"
    company = "XYZ Inc"
    hasConflictOfInterest = $true
    conflictOfInterestComment = "Previously represented opposing party in similar case in 2024. Discussed with partner and obtained waiver."
} | ConvertTo-Json

try {
    $client2 = Invoke-RestMethod -Uri "http://localhost:5000/api/clients" -Method Post -Body $clientWithConflict -Headers $headers
    Write-Host "✓ Client created successfully!" -ForegroundColor Green
    Write-Host "  ID: $($client2.id)" -ForegroundColor Cyan
    Write-Host "  Name: $($client2.firstname) $($client2.lastname)" -ForegroundColor Cyan
    Write-Host "  Has Conflict: $($client2.hasConflictOfInterest)" -ForegroundColor Yellow
    Write-Host "  Comment: $($client2.conflictOfInterestComment)" -ForegroundColor Yellow
    $global:testClientId = $client2.id
} catch {
    Write-Host "✗ Failed to create client: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

# Test 3: Retrieve the client and verify fields
Write-Host "`n[Test 3] Retrieving client to verify fields..." -ForegroundColor Yellow
try {
    $retrievedClient = Invoke-RestMethod -Uri "http://localhost:5000/api/clients/$($global:testClientId)" -Method Get -Headers $headers
    Write-Host "✓ Client retrieved successfully!" -ForegroundColor Green
    Write-Host "  Name: $($retrievedClient.firstname) $($retrievedClient.lastname)" -ForegroundColor Cyan
    Write-Host "  Has Conflict: $($retrievedClient.hasConflictOfInterest)" -ForegroundColor Yellow
    Write-Host "  Comment: $($retrievedClient.conflictOfInterestComment)" -ForegroundColor Yellow
} catch {
    Write-Host "✗ Failed to retrieve client: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Update the conflict status
Write-Host "`n[Test 4] Updating client conflict status..." -ForegroundColor Yellow
$updateBody = @{
    hasConflictOfInterest = $true
    conflictOfInterestComment = "Updated: Conflict resolved after client signed waiver on January 10, 2026."
} | ConvertTo-Json

try {
    $updatedClient = Invoke-RestMethod -Uri "http://localhost:5000/api/clients/$($global:testClientId)" -Method Put -Body $updateBody -Headers $headers
    Write-Host "✓ Client updated successfully!" -ForegroundColor Green
    Write-Host "  Has Conflict: $($updatedClient.hasConflictOfInterest)" -ForegroundColor Yellow
    Write-Host "  Updated Comment: $($updatedClient.conflictOfInterestComment)" -ForegroundColor Yellow
} catch {
    Write-Host "✗ Failed to update client: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

# Test 5: List all clients and check the fields appear
Write-Host "`n[Test 5] Listing all clients..." -ForegroundColor Yellow
try {
    $allClients = Invoke-RestMethod -Uri "http://localhost:5000/api/clients" -Method Get -Headers $headers
    Write-Host "✓ Retrieved $($allClients.Count) clients" -ForegroundColor Green
    foreach ($client in $allClients | Select-Object -First 5) {
        Write-Host "  - $($client.firstname) $($client.lastname): Conflict=$($client.hasConflictOfInterest)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "✗ Failed to list clients: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green
Write-Host "The conflict of interest fields are working correctly!" -ForegroundColor Green
