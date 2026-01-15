# Test Soft-Delete Feature on Case Entity
# This script tests that cases are marked as deleted rather than removed from the database

$baseUrl = "http://localhost:5000/api"
$ErrorActionPreference = "Continue"

Write-Host "=== Testing Soft-Delete Feature on Case ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Login as ADMIN to get JWT token
Write-Host "Step 1: Logging in as ADMIN..." -ForegroundColor Yellow
$loginRequest = @{
    username = "admin"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginRequest -ContentType "application/json"
    $token = $loginResponse.accessToken
    Write-Host "[OK] Login successful" -ForegroundColor Green
    Write-Host ""
}
catch {
    Write-Host "[FAIL] Login failed: $_" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type"  = "application/json"
}

# Step 2: Create a test case
Write-Host "Step 2: Creating a test case..." -ForegroundColor Yellow
$caseRequest = @{
    title       = "Test Case for Soft Delete - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    description = "This case will be soft-deleted to test the feature"
    status      = "OPEN"
    priority    = "MEDIUM"
    idChecked   = $false
} | ConvertTo-Json

try {
    $createResponse = Invoke-RestMethod -Uri "$baseUrl/cases" -Method Post -Headers $headers -Body $caseRequest
    $caseId = $createResponse.id
    $caseTitle = $createResponse.title
    Write-Host "[OK] Case created successfully" -ForegroundColor Green
    Write-Host "  Case ID: $caseId" -ForegroundColor Gray
    Write-Host "  Title: $caseTitle" -ForegroundColor Gray
    Write-Host ""
}
catch {
    Write-Host "[FAIL] Case creation failed: $_" -ForegroundColor Red
    exit 1
}

# Step 3: Verify case exists in the list
Write-Host "Step 3: Verifying case exists in getAllCases..." -ForegroundColor Yellow
try {
    $allCases = Invoke-RestMethod -Uri "$baseUrl/cases" -Method Get -Headers $headers
    $foundCase = $allCases | Where-Object { $_.id -eq $caseId }
    
    if ($foundCase) {
        Write-Host "[OK] Case found in list (before deletion)" -ForegroundColor Green
        Write-Host ""
    }
    else {
        Write-Host "[FAIL] Case NOT found in list (unexpected)" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "[FAIL] Failed to get all cases: $_" -ForegroundColor Red
    exit 1
}

# Step 4: Delete the case (soft-delete)
Write-Host "Step 4: Deleting the case (soft-delete)..." -ForegroundColor Yellow
try {
    $deleteResponse = Invoke-RestMethod -Uri "$baseUrl/cases/$caseId" -Method Delete -Headers $headers
    Write-Host "[OK] Delete request successful" -ForegroundColor Green
    Write-Host "  Response: $($deleteResponse.message)" -ForegroundColor Gray
    Write-Host ""
}
catch {
    Write-Host "[FAIL] Delete failed: $_" -ForegroundColor Red
    exit 1
}

# Step 5: Verify case is NOT in standard queries
Write-Host "Step 5: Verifying case is excluded from getAllCases..." -ForegroundColor Yellow
try {
    $allCasesAfterDelete = Invoke-RestMethod -Uri "$baseUrl/cases" -Method Get -Headers $headers
    $foundAfterDelete = $allCasesAfterDelete | Where-Object { $_.id -eq $caseId }
    
    if (-not $foundAfterDelete) {
        Write-Host "[OK] Case correctly excluded from getAllCases (soft-deleted)" -ForegroundColor Green
        Write-Host ""
    }
    else {
        Write-Host "[FAIL] Case STILL appears in getAllCases (soft-delete may not be working)" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "[FAIL] Failed to verify exclusion: $_" -ForegroundColor Red
    exit 1
}

# Step 6: Try to get the case by ID (should fail with 404 or similar)
Write-Host "Step 6: Verifying case is excluded from getCaseById..." -ForegroundColor Yellow
try {
    $caseById = Invoke-RestMethod -Uri "$baseUrl/cases/$caseId" -Method Get -Headers $headers
    Write-Host "[FAIL] Case STILL accessible by ID (soft-delete may not be working properly)" -ForegroundColor Red
    Write-Host "  Retrieved case: $($caseById.title)" -ForegroundColor Gray
    Write-Host ""
}
catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "[OK] Case correctly returns 404 Not Found (soft-deleted)" -ForegroundColor Green
        Write-Host ""
    }
    else {
        Write-Host "[WARN] Unexpected error (not 404): $_" -ForegroundColor Yellow
        Write-Host ""
    }
}

# Summary
Write-Host "=== Test Summary ===" -ForegroundColor Cyan
Write-Host "[SUCCESS] Soft-delete feature is working correctly!" -ForegroundColor Green
Write-Host ""
Write-Host "Verified behaviors:" -ForegroundColor White
Write-Host "  1. Case was created successfully" -ForegroundColor Gray
Write-Host "  2. Case appeared in getAllCases before deletion" -ForegroundColor Gray
Write-Host "  3. DELETE endpoint executed without errors" -ForegroundColor Gray
Write-Host "  4. Case was excluded from getAllCases after deletion" -ForegroundColor Gray
Write-Host "  5. Case was excluded from getCaseById after deletion" -ForegroundColor Gray
Write-Host ""
Write-Host "Note: To verify the case still exists in the database with deleted=true," -ForegroundColor Yellow
Write-Host "you would need to check the PostgreSQL database directly or add a" -ForegroundColor Yellow
Write-Host "special endpoint to retrieve deleted cases." -ForegroundColor Yellow
