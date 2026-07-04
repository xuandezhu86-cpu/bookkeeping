@echo off
REM start-backend.bat - Usage: start-backend.bat [all|service-name] [port]
REM   all           - Start all 6 microservices (default)
REM   auth-service  - Start a specific service
REM   port          - Port for single service (default 8081 for auth-service)

set SERVICE=%1
if "%SERVICE%"=="" set SERVICE=all

set JAVA_HOME=c:\Users\33907\.jdks\ms-17.0.18
set MAVEN_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2025.1\plugins\maven\lib\maven3
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

cd /d "%~dp0backend"

echo [1/2] Compiling multi-module project...
call mvn compile -q -o
if %ERRORLEVEL% neq 0 (
    echo [!] Compile failed!
    pause
    exit /b 1
)

echo [2/2] Starting service(s)...
powershell -ExecutionPolicy Bypass -File "%~dp0start-backend.ps1" -Service "%SERVICE%"

pause