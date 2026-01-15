$baseUrl = "http://localhost:5000/api"
$ErrorActionPreference = "Stop"

Write-Host "=== Seeded Participants Verification ===" -ForegroundColor Cyan

try {
    # Login
    Write-Host "Logging in as admin..."
    $token = (Invoke-RestMethod "$baseUrl/auth/login" -Method Post -Body (@{username = "admin"; password = "password123" } | ConvertTo-Json) -ContentType "application/json").accessToken
    $headers = @{Authorization = "Bearer $token"; "Content-Type" = "application/json" }
    
    # Get Cases
    Write-Host "Fetching cases..."
    $cases = Invoke-RestMethod "$baseUrl/cases" -Method Get -Headers $headers
    
    # Verify Case 1 Participants
    $case1 = $cases | Where-Object { $_.title -eq "Contract Dispute Resolution" }
    if ($case1) {
        Write-Host "Checking Case: $($case1.title)" -ForegroundColor Yellow
        if ($case1.participants.Count -gt 0) {
            Write-Host "PASS: Has $($case1.participants.Count) participants" -ForegroundColor Green
            $case1.participants | ForEach-Object { Write-Host "  - $($_.username)" -ForegroundColor Gray }
        }
        else {
            Write-Host "FAIL: No participants found" -ForegroundColor Red
        }
    }
    else {
        Write-Host "FAIL: Case 'Contract Dispute Resolution' not found" -ForegroundColor Red
    }

    # Verify Case 3 Participants
    $case3 = $cases | Where-Object { $_.title -eq "Business Partnership Dissolution" }
    if ($case3) {
        Write-Host "Checking Case: $($case3.title)" -ForegroundColor Yellow
        if ($case3.participants.Count -gt 0) {
            Write-Host "PASS: Has $($case3.participants.Count) participants" -ForegroundColor Green
            $case3.participants | ForEach-Object { Write-Host "  - $($_.username)" -ForegroundColor Gray }
        }
        else {
            Write-Host "FAIL: No participants found" -ForegroundColor Red
        }
    }
    else {
        Write-Host "FAIL: Case 'Business Partnership Dissolution' not found" -ForegroundColor Red
    }

}
catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
