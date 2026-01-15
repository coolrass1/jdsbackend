$baseUrl = "http://localhost:5000/api"
$ErrorActionPreference = "Stop"

function Assert-Success($condition, $message) {
    if ($condition) {
        Write-Host "PASS: $message" -ForegroundColor Green
    }
    else {
        Write-Host "FAIL: $message" -ForegroundColor Red
    }
}

try {
    # Login
    Write-Host "Logging in..."
    $loginBody = @{ username = "admin"; password = "password123" } | ConvertTo-Json
    $result = Invoke-RestMethod "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $result.accessToken
    $headers = @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" }
    Write-Host "Logged in as admin."

    # Get Users
    $u1Id = 1
    $u2Id = 2
    
    try {
        Write-Host "Fetching users..."
        $users = Invoke-RestMethod "$baseUrl/users" -Method Get -Headers $headers
        # Write-Host "Users fetched: $($users | ConvertTo-Json -Depth 2)"
        if ($users.Count -ge 2) {
            $u1Id = $users[0].id
            $u2Id = $users[1].id
            Write-Host "Using users from API: $($users[0].username) ($u1Id) and $($users[1].username) ($u2Id)"
        }
        else {
            Write-Host "Not enough users found, defaulting to IDs 1 and 2." -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "Failed to fetch users: $_" -ForegroundColor Yellow
        Write-Host "Defaulting to IDs 1 and 2." -ForegroundColor Yellow
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
            $body = $reader.ReadToEnd()
            Write-Host "Validation Error on Users: $body" -ForegroundColor Yellow
        }
    }

    # 1. Create Case with Participants
    Write-Host "Creating case with Participant ID $u1Id..."
    $createBody = @{
        title          = "Participant Test Case"
        description    = "Testing participants feature"
        status         = "OPEN"
        priority       = "MEDIUM"
        assignedUserId = $u1Id
        participantIds = @($u1Id)
        idChecked      = $false
    } | ConvertTo-Json

    $case = Invoke-RestMethod "$baseUrl/cases" -Method Post -Body $createBody -Headers $headers
    $hasP1 = ($case.participants | Where-Object { $_.id -eq $u1Id })
    Assert-Success ($case.participants.Count -eq 1 -and $hasP1) "Case created with participant"

    # 2. Add Participant
    Write-Host "Adding participant $u2Id..."
    $case = Invoke-RestMethod "$baseUrl/cases/$($case.id)/participants/$($u2Id)" -Method Post -Headers $headers
    $hasP2 = ($case.participants | Where-Object { $_.id -eq $u2Id })
    Assert-Success ($case.participants.Count -eq 2 -and $hasP2) "Participant added"

    # 3. Get Cases by Participant
    Write-Host "Querying by participant $u2Id..."
    $pCases = Invoke-RestMethod "$baseUrl/cases/participant/$($u2Id)" -Method Get -Headers $headers
    $found = ($pCases | Where-Object { $_.id -eq $case.id })
    Assert-Success ($found) "Found case by participant query"

    # 4. Remove Participant
    Write-Host "Removing participant $u2Id..."
    $case = Invoke-RestMethod "$baseUrl/cases/$($case.id)/participants/$($u2Id)" -Method Delete -Headers $headers
    $hasP2Removed = -not ($case.participants | Where-Object { $_.id -eq $u2Id })
    Assert-Success ($case.participants.Count -eq 1 -and $hasP2Removed) "Participant removed"

    # Cleanup
    Write-Host "Cleaning up..."
    Invoke-RestMethod "$baseUrl/cases/$($case.id)" -Method Delete -Headers $headers
    Write-Host "Test Complete."
}
catch {
    Write-Host "Error: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
        $body = $reader.ReadToEnd()
        Write-Host "Response Body: $body" -ForegroundColor Red
    }
}
