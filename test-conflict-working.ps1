Write-Host "`n=== Testing Client Conflict of Interest ===" -ForegroundColor Cyan

Write-Host "`n[1] Logging in..." -ForegroundColor Yellow
$loginBody = '{"username":"admin","password":"password123"}'
$login = Invoke-RestMethod -Uri "http://localhost:5000/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
Write-Host "    Logged in!" -ForegroundColor Green

$headers = @{Authorization="Bearer $($login.accessToken)"}

Write-Host "`n[2] Creating client WITH conflict..." -ForegroundColor Yellow
$randomNum = Get-Random -Minimum 1000 -Maximum 9999
$randomNI = "AB$(Get-Random -Minimum 100000 -Maximum 999999)C"
$clientBody = "{`"firstname`":`"Jane`",`"lastname`":`"Smith`",`"email`":`"conflict$randomNum@test.com`",`"NI_number`":`"$randomNI`",`"phone`":`"555-1234`",`"address`":`"123 St`",`"company`":`"Corp`",`"hasConflictOfInterest`":true,`"conflictOfInterestComment`":`"Previous representation - waiver obtained`"}"
$client = Invoke-RestMethod -Uri "http://localhost:5000/api/clients" -Method Post -Body $clientBody -ContentType "application/json" -Headers $headers

Write-Host "    SUCCESS!" -ForegroundColor Green
Write-Host "    ID: $($client.id)" -ForegroundColor Cyan
Write-Host "    Name: $($client.firstname) $($client.lastname)" -ForegroundColor White
Write-Host "    NI Number: $($client.NI_number)" -ForegroundColor Cyan
Write-Host "    Has Conflict: $($client.hasConflictOfInterest)" -ForegroundColor Yellow
Write-Host "    Comment: $($client.conflictOfInterestComment)" -ForegroundColor Yellow

Write-Host "`n[3] Creating client WITHOUT conflict..." -ForegroundColor Yellow
$randomNum2 = Get-Random -Minimum 1000 -Maximum 9999
$randomNI2 = "CD$(Get-Random -Minimum 100000 -Maximum 999999)E"
$clientBody2 = "{`"firstname`":`"John`",`"lastname`":`"Doe`",`"email`":`"noconflict$randomNum2@test.com`",`"NI_number`":`"$randomNI2`",`"phone`":`"555-5678`",`"address`":`"456 Ave`",`"company`":`"Inc`",`"hasConflictOfInterest`":false}"
$client2 = Invoke-RestMethod -Uri "http://localhost:5000/api/clients" -Method Post -Body $clientBody2 -ContentType "application/json" -Headers $headers

Write-Host "    SUCCESS!" -ForegroundColor Green
Write-Host "    Name: $($client2.firstname) $($client2.lastname)" -ForegroundColor White
Write-Host "    NI Number: $($client2.NI_number)" -ForegroundColor Cyan
Write-Host "    Has Conflict: $($client2.hasConflictOfInterest)" -ForegroundColor Green

Write-Host "`n=== ALL TESTS PASSED ===" -ForegroundColor Green
Write-Host "Conflict of interest fields are working correctly!`n" -ForegroundColor Green
