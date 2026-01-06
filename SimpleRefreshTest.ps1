# Config
$baseUrl = "http://localhost:8080/api/auth"
$randomId = Get-Random -Minimum 1000 -Maximum 9999
$username = "testuser_$randomId"
$password = "password123"
$email = "test_$randomId@example.com"
$logFile = "C:\Users\cheikh\Documents\java\jdsbackend\verification_log.txt"

# Helper to log
function Log-Msg {
    param($msg)
    Write-Host $msg
    Add-Content -Path $logFile -Value $msg
}

# Clear log
if (Test-Path $logFile) { Remove-Item $logFile }
Log-Msg "Starting verification with user: $username"

# 1. Register User
Log-Msg "1. Registering User..."
$registerBody = @{
    username = $username
    email    = $email
    password = $password
    role     = "USER"
} | ConvertTo-Json

try {
    $regResponse = Invoke-RestMethod -Uri "$baseUrl/register" -Method Post -Body $registerBody -ContentType "application/json"
    Log-Msg "Registration Successful"
}
catch {
    Log-Msg "Registration Failed: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        if ($stream) {
            $reader = New-Object System.IO.StreamReader $stream
            $responseBody = $reader.ReadToEnd()
            Log-Msg "Response Body: $responseBody"
        }
    }
}

# 2. Login to get tokens
Log-Msg "`n2. Logging in..."
$loginBody = @{
    username = $username
    password = $password
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/login" -Method Post -Body $loginBody -ContentType "application/json"
    $accessToken = $authResponse.accessToken
    $refreshToken = $authResponse.refreshToken
    
    Log-Msg "Login Successful!"
    Log-Msg "Access Token (first 20 chars): $($accessToken.Substring(0, 20))..."
    Log-Msg "Refresh Token: $refreshToken"
}
catch {
    Log-Msg "Login failed. Details:"
    Log-Msg "Status Code: $($_.Exception.Response.StatusCode)"
    
    # Try to read response stream
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        if ($stream) {
            $reader = New-Object System.IO.StreamReader $stream
            $responseBody = $reader.ReadToEnd()
            Log-Msg "Response Body: $responseBody"
        }
    }
    exit
}

# 3. Use Refresh Token to get new Access Token
Log-Msg "`n3. Refreshing Token..."
$refreshBody = @{
    refreshToken = $refreshToken
} | ConvertTo-Json

try {
    $refreshResponse = Invoke-RestMethod -Uri "$baseUrl/refresh" -Method Post -Body $refreshBody -ContentType "application/json"
    $newAccessToken = $refreshResponse.accessToken
    $newRefreshToken = $refreshResponse.refreshToken
    
    Log-Msg "Refresh Successful!"
    Log-Msg "New Access Token: $($newAccessToken.Substring(0, 20))..."
    
    if ($newAccessToken -ne $accessToken) {
        Log-Msg "SUCCESS: New access token is different from the old one."
    }
    else {
        Log-Msg "WARNING: New access token appears identical to the old one."
    }
    
    if ($newRefreshToken) {
        Log-Msg "New Refresh Token: $newRefreshToken"
    }

}
catch {
    Log-Msg "Refresh failed: $_"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        if ($stream) {
            $reader = New-Object System.IO.StreamReader $stream
            $responseBody = $reader.ReadToEnd()
            Log-Msg "Response Body: $responseBody"
        }
    }
}
