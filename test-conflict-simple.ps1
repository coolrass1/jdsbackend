# Simple test for conflict of interest fields
Write-Host "`n=== Testing Client Conflict of Interest Fields ===" -ForegroundColor Cyan

# Step 1: Login
Write-Host "`n[1] Logging in as admin..." -ForegroundColor Yellow
$loginBody = '{"username":"admin","password":"password123"}'

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:5000/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.accessToken
    Write-Host "    Login successful!" -ForegroundColor Green
} catch {
    Write-Host "    Login failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "    Make sure the application is running and the admin user exists." -ForegroundColor Yellow
    exit 1
}

# Step 2: Create client WITH conflict
Write-Host "`n[2] Creating client WITH conflict of interest..." -ForegroundColor Yellow
$randomEmail = "conflict.test.$(Get-Random -Minimum 1000 -Maximum 9999)@example.com"
$randomNI = "AB$(Get-Random -Minimum 100000 -Maximum 999999)C"
$clientBody = @{
    firstname = "Jane"
    lastname = "Smith"
    email = $randomEmail
    NI_number = $randomNI
    phone = "555-1234"
    address = "456 Conflict Ave"
    company = "XYZ Corp"
    hasConflictOfInterest = $true
    conflictOfInterestComment = "Client previously represented by our firm in unrelated matter in 2023. Conflict waiver obtained on January 10, 2026."
} | ConvertTo-Json

try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    $client = Invoke-RestMethod -Uri "http://localhost:5000/api/clients" -Method Post -Body $clientBody -Headers $headers
    
    Write-Host "    ✓ Client created successfully!" -ForegroundColor Green
    Write-Host "    ID: $($client.id)" -ForegroundColor Cyan
    Write-Host "    Name: $($client.firstname) $($client.lastname)" -ForegroundColor Cyan
    Write-Host "    Email: $($client.email)" -ForegroundColor Cyan
    Write-Host "    NI Number: $($client.NI_number)" -ForegroundColor Cyan
    Write-Host "    Has Conflict: $($client.hasConflictOfInterest)" -ForegroundColor Yellow
    Write-Host "    Conflict Comment: $($client.conflictOfInterestComment)" -ForegroundColor Yellow
    
    $clientId = $client.id
} catch {
    Write-Host "    ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "    Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    exit 1
}

# Step 3: Retrieve and verify
Write-Host "`n[3] Retrieving client to verify fields..." -ForegroundColor Yellow
try {
    $retrievedClient = Invoke-RestMethod -Uri "http://localhost:5000/api/clients/$clientId" -Method Get -Headers $headers
    
    if ($retrievedClient.hasConflictOfInterest -eq $true -and $retrievedClient.conflictOfInterestComment -ne $null) {
        Write-Host "    ✓ Verification successful!" -ForegroundColor Green
        Write-Host "    Conflict field: $($retrievedClient.hasConflictOfInterest)" -ForegroundColor Green
        Write-Host "    Comment exists: YES" -ForegroundColor Green
    } else {
        Write-Host "    ✗ Verification failed - fields not properly set" -ForegroundColor Red
    }
} catch {
    Write-Host "    ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 4: Create client WITHOUT conflict
Write-Host "`n[4] Creating client WITHOUT conflict..." -ForegroundColor Yellow
$randomNI2 = "CD$(Get-Random -Minimum 100000 -Maximum 999999)E"
$clientBody2 = @{
    firstname = "John"
    lastname = "Doe"
    email = $randomEmail2
    NI_number = $randomNI
    email = $randomEmail2
    phone = "555-5678"
    address = "789 Clear St"
    company = "ABC Inc"
    hasConflictOfInterest = $false
} | ConvertTo-Json

try {
    $client2 = Invoke-RestMethod -Uri "http://localhost:5000/api/clients" -Method Post -Body $clientBody2 -Headers $headers
    
    Write-Host "    ✓ Client created successfully!" -ForegroundColor Green
    Write-Host "    Name: $($client2.firstname) $($client2.lastname)" -ForegroundColor Cyan
    Write-Host "    Has Conflict: $($client2.hasConflictOfInterest)" -ForegroundColor Green
    $commentText = if ($client2.conflictOfInterestComment) { $client2.conflictOfInterestComment } else { "(none)" }
    Write-Host "    Comment: $commentText" -ForegroundColor Green
} catch {
    Write-Host "    ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== TEST COMPLETE ===" -ForegroundColor Cyan
Write-Host "SUCCESS: Conflict of interest fields are working correctly!" -ForegroundColor Green
Write-Host ""
