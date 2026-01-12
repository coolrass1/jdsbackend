# Verify Case Audit & Sequence
$url = "http://localhost:5000/api"
$token = (Invoke-RestMethod "$url/auth/login" -Post -Body (@{username = "admin"; password = "password123" } | ConvertTo-Json) -ContentType "application/json").accessToken
$head = @{Authorization = "Bearer $token"; "Content-Type" = "application/json" }

# 1. Get a Client ID (seeded)
try {
    $clients = Invoke-RestMethod "$url/clients" -Headers $head
    $cid = $clients[0].id
    Write-Host "Using Client ID: $cid"
}
catch {
    Write-Host "Make sure app is running and seeded."
    exit
}

# 2. Create Case (No Ref Num)
$rnd = Get-Random
$body = @{
    title       = "Audit Case $rnd"; 
    description = "Test audit logic"; 
    clientId    = $cid; 
    priority    = "MEDIUM"; 
    status      = "OPEN" 
    # idChecked is false by default
} | ConvertTo-Json

try {
    Write-Host "Creating Case..."
    $case = Invoke-RestMethod "$url/cases" -Post -Body $body -Headers $head -ContentType "application/json"
    
    Write-Host "Created Case ID: $($case.id)"
    
    # Check Ref Num
    $ref = $case.referenceNumber
    Write-Host "Ref Num: $ref"
    $today = Get-Date -Format "yyyy-MM-dd"
    if ($ref -match "^$today-\d{4}$") { 
        Write-Host "Ref Format OK" -ForegroundColor Green 
    }
    else { 
        Write-Host "Ref Format FAIL ($ref)" -ForegroundColor Red 
    }
    
    # Check Creator
    if ($case.createdByUser.username -eq "admin") { 
        Write-Host "Creator OK (admin)" -ForegroundColor Green 
    }
    else { 
        Write-Host "Creator FAIL" -ForegroundColor Red 
    }

}
catch {
    Write-Host "Error: $_"
    if ($_.Exception.Response) {
        $s = $_.Exception.Response.GetResponseStream()
        $r = New-Object System.IO.StreamReader($s)
        Write-Host $r.ReadToEnd()
    }
}
