# Simple Test script for Client Creation with Users
$baseUrl = "http://localhost:5000/api"

# Login
$token = (Invoke-RestMethod "$baseUrl/auth/login" -Method Post -Body (@{username = "admin"; password = "password123" } | ConvertTo-Json) -ContentType "application/json").accessToken
$headers = @{Authorization = "Bearer $token"; "Content-Type" = "application/json" }

# Get User ID
$users = Invoke-RestMethod "$baseUrl/users" -Method Get -Headers $headers
$userId = $users[0].id
Write-Host "Assigning User ID: $userId"

# Create Client
$rnd = Get-Random
$body = @{
    firstname = "Test"; lastname = "Assign$rnd"; email = "assign$rnd@test.com"; 
    ni_number = "AB${rnd}C"; phone = "111"; assignedUserIds = @($userId)
} | ConvertTo-Json

try {
    $client = Invoke-RestMethod "$baseUrl/clients" -Method Post -Body $body -Headers $headers -ContentType "application/json"
    Write-Host "Created Client: $($client.id)"
    if ($client.assignedUsers.Count -gt 0) { 
        Write-Host "SUCCESS: Assigned Users: $($client.assignedUsers.Count)" -ForegroundColor Green 
    }
    else { 
        Write-Host "FAILURE: No users assigned" -ForegroundColor Red 
    }
}
catch {
    Write-Host "Error creating client" -ForegroundColor Red
    Write-Host $_
}
