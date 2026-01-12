# Simple Sequence Test
$url = "http://localhost:5000/api"
$token = (Invoke-RestMethod "$url/auth/login" -Method Post -Body (@{username = "admin"; password = "password123" } | ConvertTo-Json) -ContentType "application/json").accessToken
$head = @{Authorization = "Bearer $token"; "Content-Type" = "application/json" }

$rnd = Get-Random
$body = @{firstname = "Seq"; lastname = "T$rnd"; email = "s$rnd@t.com"; ni_number = "S$rnd" } | ConvertTo-Json

try {
    Write-Host "Creating Client (Auto-Ref)..."
    $c = Invoke-RestMethod "$url/clients" -Method Post -Body $body -Headers $head -ContentType "application/json"
    Write-Host "Created Client ID: $($c.id)"
    Write-Host "Ref Num: $($c.referenceNumber)"
    
    $today = Get-Date -Format "yyyy-MM-dd"
    if ($c.referenceNumber -match "^$today-\d{4}$") { 
        Write-Host "Format OK ($today-XXXX)" -ForegroundColor Green 
    }
    else { 
        Write-Host "Format FAIL: $($c.referenceNumber)" -ForegroundColor Red 
    }
}
catch {
    Write-Host "Error: $_"
}
