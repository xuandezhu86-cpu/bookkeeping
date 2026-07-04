param(
    [string]$Service = "all"
)

$backendDir = Join-Path $PSScriptRoot "backend"

# Service port mapping
$services = @{
    "gateway-service"    = 8080
    "auth-service"       = 8081
    "category-service"   = 8082
    "expense-service"    = 8083
    "budget-service"     = 8084
    "statistics-service" = 8085
}

$mainClassMap = @{
    "gateway-service"    = "com.youran.bookkeeping.gateway.GatewayApplication"
    "auth-service"       = "com.youran.bookkeeping.auth.AuthApplication"
    "category-service"   = "com.youran.bookkeeping.category.CategoryApplication"
    "expense-service"    = "com.youran.bookkeeping.expense.ExpenseApplication"
    "budget-service"     = "com.youran.bookkeeping.budget.BudgetApplication"
    "statistics-service" = "com.youran.bookkeeping.statistics.StatisticsApplication"
}

function Ensure-Classpath {
    param($ServiceDir)
    $cpFile = Join-Path $ServiceDir "cp.txt"
    if (-not (Test-Path $cpFile)) {
        Write-Host "[*] Generating classpath for $(Split-Path $ServiceDir -Leaf)..."
        Push-Location $ServiceDir
        & mvn dependency:build-classpath -Dmdep.outputFile=cp.txt -q -o
        Pop-Location
    }
}

function Kill-Port {
    param($Port)
    try {
        $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction Stop
        if ($conn.OwningProcess -ne $PID) {
            Write-Host "[*] Killing old process PID $($conn.OwningProcess) on port $Port..."
            Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
            Start-Sleep 1
        }
    } catch {
        # No existing connection, continue
    }
}

function Start-Service {
    param($Name, $Port, $MainClass)
    # Kill old process on port
    Kill-Port -Port $Port

    $serviceDir = Join-Path $backendDir $Name
    Ensure-Classpath -ServiceDir $serviceDir

    $cpFile = Join-Path $serviceDir "cp.txt"
    $cp = [System.IO.File]::ReadAllText($cpFile).Trim()
    $classpath = (Join-Path $serviceDir "target/classes") + ";" + $cp

    Write-Host "[*] Starting $Name on port $Port..."

    $logFile = Join-Path $serviceDir "server.log"
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = "java"
    $psi.Arguments = "-cp `"$classpath`" $MainClass --server.port=$Port"
    $psi.RedirectStandardOutput = $false
    $psi.RedirectStandardError = $false
    $psi.UseShellExecute = $true
    $psi.WorkingDirectory = $serviceDir

    try {
        $p = [System.Diagnostics.Process]::Start($psi)
        Write-Host "  PID: $($p.Id) - log: $logFile"
    } catch {
        Write-Host "[!] Failed to start $Name`: $_"
    }
}

# Step 1: Generate classpath files for all needed services
$targets = if ($Service -eq "all") { $services.Keys } else { @($Service) }

foreach ($svc in $targets) {
    $serviceDir = Join-Path $backendDir $svc
    Ensure-Classpath -ServiceDir $serviceDir
}

# Step 2: Start services
if ($Service -eq "all") {
    Write-Host "[*] Starting all 6 microservices..."
    foreach ($svc in $services.Keys) {
        Start-Service -Name $svc -Port $services[$svc] -MainClass $mainClassMap[$svc]
        Start-Sleep 2
    }
    Write-Host ""
    Write-Host "[*] All services started!"
    Write-Host "    Gateway:     http://localhost:8080"
    Write-Host "    Auth:        http://localhost:8081"
    Write-Host "    Category:    http://localhost:8082"
    Write-Host "    Expense:     http://localhost:8083"
    Write-Host "    Budget:      http://localhost:8084"
    Write-Host "    Statistics:  http://localhost:8085"
} elseif ($services.ContainsKey($Service)) {
    Write-Host "[*] Starting $Service..."
    Start-Service -Name $Service -Port $services[$Service] -MainClass $mainClassMap[$Service]
} else {
    Write-Host "[!] Unknown service: $Service"
    Write-Host "    Available: all, $($services.Keys -join ', ')"
}
