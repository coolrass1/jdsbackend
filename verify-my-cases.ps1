# verify-my-cases.ps1

function Get-AuthToken {
    param (
        [string]$Username,
        [string]$Password
    )
    $loginUrl = "http://localhost:5000/api/auth/login"
    $body = @{
        username = $Username
        password = $Password
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
        return $response.accessToken
    }
    catch {
        $exception = $_.Exception
        $response = $exception.Response
        if ($response) {
            $reader = New-Object System.IO.StreamReader($response.GetResponseStream())
            $body = $reader.ReadToEnd()
            Write-Error "Login failed. Status: $($response.StatusCode). Body: $body"
        }
        else {
            Write-Error "Login failed. Error: $_"
        }
        exit 1
    }
}

# 1. Login as admin (ID 1)
$adminToken = Get-AuthToken -Username "admin" -Password "password123"
$headers = @{
    Authorization  = "Bearer $adminToken"
    "Content-Type" = "application/json"
}

Write-Host "`nStep 1: Fetching Admin's cases (My Cases)..."
try {
    $myCases = Invoke-RestMethod -Uri "http://localhost:5000/api/cases/my-cases" -Method Get -Headers $headers -ErrorAction Stop
    Write-Host "Successfully fetched $($myCases.Count) cases for Admin."
    
    # Validation: Check if admin is assigned OR participant
    $adminId = 1
    foreach ($case in $myCases) {
        $isAssigned = ($case.assignedUser.id -eq $adminId)
        $isParticipant = $false
        if ($case.participants) {
            $isParticipant = ($case.participants.id -contains $adminId)
        }
        
        if (-not ($isAssigned -or $isParticipant)) {
            Write-Error "Case logic failure: Case ID $($case.id) returned but user is neither assigned nor participant."
        }
    }
    Write-Host "Validation Passed: All returned cases are relevant to the user."
}
catch {
    Write-Error "Failed to fetch my cases. Error: $_"
    exit 1
}

Write-Host "`nVerification Complete!"
