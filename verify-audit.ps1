# Test script for Client Audit Fields
$baseUrl = "http://localhost:5000/api"

# Login
$token = (Invoke-RestMethod "$baseUrl/auth/login" -Method Post -Body (@{username = "admin"; password = "password123" } | ConvertTo-Json) -ContentType "application/json").accessToken
$headers = @{Authorization = "Bearer $token"; "Content-Type" = "application/json" }

# Get Me
$users = Invoke-RestMethod "$baseUrl/users" -Method Get -Headers $headers
$me = $users[0] # Admin
Write-Host "Logged in as: $($me.username) (ID: $($me.id))"

# Create Client with Reference Number
$rnd = Get-Random
$ref = "REF-$rnd"
$body = @{
    firstname = "Audit"; lastname = "Test$rnd"; email = "audit$rnd@test.com"; 
    ni_number = "AU${rnd}D"; phone = "222"; 
    referenceNumber = $ref
} | ConvertTo-Json

try {
    Write-Host "Creating client with Ref: $ref..."
    $client = Invoke-RestMethod "$baseUrl/clients" -Method Post -Body $body -Headers $headers -ContentType "application/json"
    
    Write-Host "Client Created: $($client.id)"
    
    # Verify Fields
    if ($client.referenceNumber -eq $ref) { Write-Host "✓ Reference Number matches" -ForegroundColor Green } 
    else { Write-Host "✗ Reference Number mismatch: $($client.referenceNumber)" -ForegroundColor Red }

    if ($client.createdByUser -ne $null -and $client.createdByUser.id -eq $me.id) { 
        Write-Host "✓ CreatedByUser matches logged in user" -ForegroundColor Green 
    }
    else { 
        Write-Host "✗ CreatedByUser mismatch or null" -ForegroundColor Red 
    }

    if ($client.lastModifiedByUser -ne $null -and $client.lastModifiedByUser.id -eq $me.id) { 
        Write-Host "✓ LastModifiedByUser matches logged in user" -ForegroundColor Green 
    }
    else { 
        Write-Host "✗ LastModifiedByUser mismatch or null" -ForegroundColor Red 
    }

    # Update Client
    Write-Host "Updating client..."
    $updateBody = @{ referenceNumber = "UPD-$ref" } | ConvertTo-Json
    $updatedClient = Invoke-RestMethod "$baseUrl/clients/$($client.id)" -Method Put -Body $updateBody -Headers $headers -ContentType "application/json"
    
    if ($updatedClient.referenceNumber -eq "UPD-$ref") { Write-Host "✓ Reference Number updated" -ForegroundColor Green }
    
    # (Since we update as same user, modifier doesn't change ID, but we verify it's present)
    if ($updatedClient.lastModifiedByUser.id -eq $me.id) { Write-Host "✓ LastModifiedByUser present after update" -ForegroundColor Green }

}
catch {
    Write-Host "Error: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "Details: $($reader.ReadToEnd())" -ForegroundColor Red
    }
}
