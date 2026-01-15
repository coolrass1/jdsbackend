# verify-access-control.ps1

$baseUrl = "http://localhost:5000/api"

function Login($username, $password) {
    $body = @{
        username = $username
        password = $password
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $body -ContentType "application/json"
        return $response.accessToken
    }
    catch {
        Write-Host "Login failed for $username : $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

function Create-User($token, $username, $email, $role) {
    # Since we don't have a direct Create User endpoint publicly exposed for custom roles easily without Admin,
    # we'll assume we are Admin.
    # Actually register endpoint defaults to CASE_WORKER.
    # To create VIEWER, we might need direct DB or Admin User Management endpoint if it exists.
    # If not, we skip Viewer test if we can't create one, or assume one exists.
    # For this script we will try to register and then maybe Admin updates role? 
    # Or we skip Viewer if not easily doable and focus on Case Worker/Participant.
    return
}

# 1. Login as Admin
Write-Host "Logging in as Admin..."
$adminToken = Login "admin" "password123"
if (!$adminToken) { exit }

# 2. Login as Caseworker (Owner)
Write-Host "Logging in as Caseworker..."
$cwToken = Login "caseworker" "password123"

# 3. Login as Caseworker2 (Participant)
Write-Host "Logging in as Caseworker2..."
$cw2Token = Login "caseworker2" "password123"

# 4. Cleanup/Setup: Caseworker creates a case
Write-Host "`n4. Caseworker creates a case..."
$caseBody = @{
    title       = "Access Control Test Case"
    description = "Testing participants"
    priority    = "MEDIUM"
    idChecked   = $false
    clientId    = 1
    # assignedUserId = $null # Let backend handle or default to creator
} | ConvertTo-Json

try {
    $case = Invoke-RestMethod -Uri "$baseUrl/cases" -Method Post -Body $caseBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $cwToken" }
    Write-Host "Case created: $($case.id)" -ForegroundColor Green
    $caseId = $case.id
}
catch {
    Write-Host "Failed to create case: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# 5. Caseworker2 tries to access (Should fail or return 403 or empty list if filtering)
# Access by ID
Write-Host "`n5. Caseworker2 tries to access Case $caseId (Should Fail)..."
try {
    Invoke-RestMethod -Uri "$baseUrl/cases/$caseId" -Method Get -Headers @{ Authorization = "Bearer $cw2Token" }
    Write-Host "FAILURE: Caseworker2 could read case!" -ForegroundColor Red
}
catch {
    if ($_.Exception.Response.StatusCode -eq [System.Net.HttpStatusCode]::Forbidden) {
        Write-Host "SUCCESS: Access Denied (403)" -ForegroundColor Green
    }
    else {
        Write-Host "Unexpected error: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
    }
}

# 6. Add Caseworker2 as Participant
Write-Host "`n6. Owner adds Caseworker2 as participant..."
# Need user ID of caseworker2.
# We can get it from /auth/login response usually id is in there
# Let's re-login/parse login response properly
$loginResp = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body (@{username = "caseworker2"; password = "password123" } | ConvertTo-Json) -ContentType "application/json"
$cw2Id = $loginResp.id

try {
    Invoke-RestMethod -Uri "$baseUrl/cases/$caseId/participants/$cw2Id" -Method Post -Headers @{ Authorization = "Bearer $cwToken" }
    Write-Host "Participant added successfully." -ForegroundColor Green
}
catch {
    Write-Host "Failed to add participant: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. Caseworker2 attempts access again (Should Success)
Write-Host "`n7. Caseworker2 accesses Case $caseId (Should Succeed)..."
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/cases/$caseId" -Method Get -Headers @{ Authorization = "Bearer $cw2Token" }
    Write-Host "SUCCESS: Caseworker2 read case." -ForegroundColor Green
}
catch {
    Write-Host "FAILURE: Access Denied or Error: $($_.Exception.Message)" -ForegroundColor Red
}

# 8. Caseworker2 tries to Update (Should Succeed as default is EDITOR)
Write-Host "`n8. Caseworker2 tries to update Case $caseId..."
$updateBody = @{
    title = "Updated by Participant"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$baseUrl/cases/$caseId" -Method Put -Body $updateBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $cw2Token" }
    Write-Host "SUCCESS: Caseworker2 updated case." -ForegroundColor Green
}
catch {
    Write-Host "FAILURE: Update failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nVerification Complete."
