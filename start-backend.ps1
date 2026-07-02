param(
    [int]$Port = 8080
)

$backendDir = Join-Path $PSScriptRoot "backend"
$cpFile = Join-Path $backendDir "cp.txt"

# Kill any existing process on the target port
try {
    $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction Stop
    if ($conn.OwningProcess -ne $PID) {
        Write-Host "[*] Killing old process PID $($conn.OwningProcess) on port $Port..."
        Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
        Start-Sleep 2
    }
} catch {
    # No existing connection, continue
}

if (-not (Test-Path $cpFile)) {
    Write-Host "Generating classpath..."
    & mvn dependency:build-classpath -Dmdep.outputFile=cp.txt -q -o
}

$cp = [System.IO.File]::ReadAllText($cpFile).Trim()
$classpath = (Join-Path $backendDir "target/classes") + ";" + $cp

Write-Host "Starting server on port $Port..."
Write-Host ""

Push-Location $backendDir
java -cp $classpath com.youran.bookkeeping.YouranApplication --server.port=$Port
Pop-Location

if (-not $?) {
    Write-Host "[!] Server exited with error."
    pause
}
