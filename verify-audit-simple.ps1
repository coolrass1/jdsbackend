# Simple Audit Test
$url = "http://localhost:5000/api"
$token = (Invoke-RestMethod "$url/auth/login" -Method Post -Body (@{username = "admin"; password = "password123" } | ConvertTo-Json) -ContentType "application/json").accessToken
$head = @{Authorization = "Bearer $token"; "Content-Type" = "application/json" }

$rnd = Get-Random
$ref = "REF$rnd"
$body = @{firstname = "Aud"; lastname = "T$rnd"; email = "a$rnd@t.com"; ni_number = "A$rnd"; referenceNumber = $ref } | ConvertTo-Json

try {
    Write-Host "Creating Client..."
    $c = Invoke-RestMethod "$url/clients" -Method Post -Body $body -Headers $head -ContentType "application/json"
    Write-Host "Created Client ID: $($c.id)"
    
    if ($c.referenceNumber -eq $ref) { Write-Host "Ref Num: OK" } else { Write-Host "Ref Num: FAIL" }
    
    if ($c.createdByUser) { 
        Write-Host "CreatedBy: OK ($($c.createdByUser.username))" 
    }
    else { 
        Write-Host "CreatedBy: FAIL" 
    }
    
    if ($c.lastModifiedByUser) { 
        Write-Host "ModifiedBy: OK ($($c.lastModifiedByUser.username))" 
    }
    else { 
        Write-Host "ModifiedBy: FAIL" 
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
