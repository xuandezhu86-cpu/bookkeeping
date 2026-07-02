@echo off
REM start-backend.bat - Usage: start-backend.bat [port]

set PORT=%1
if "%PORT%"=="" set PORT=8080

set JAVA_HOME=c:\Users\33907\.jdks\ms-17.0.18
set MAVEN_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2025.1\plugins\maven\lib\maven3
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

cd /d "%~dp0backend"

echo [1/2] Compiling...
call mvn compile -q -o
if %ERRORLEVEL% neq 0 (
    echo [!] Compile failed!
    pause
    exit /b 1
)

echo [2/2] Starting server on port %PORT%...
echo Press Ctrl+C to stop.
echo.

REM Delegate to PowerShell to handle long classpath
powershell -ExecutionPolicy Bypass -File "%~dp0start-backend.ps1" -Port %PORT%

pause