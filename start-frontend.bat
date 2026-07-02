@echo off
REM start-frontend.bat - Usage: start-frontend.bat [dev|electron]

set MODE=%1
if "%MODE%"=="" set MODE=dev

cd /d "%~dp0frontend"

if not exist "node_modules" (
    echo [*] Installing dependencies...
    call npm install
    if %ERRORLEVEL% neq 0 (
        echo [!] npm install failed.
        pause
        exit /b 1
    )
)

if "%MODE%"=="electron" (
    echo Starting Electron app...
    call npm run electron:dev
) else (
    echo Starting Vite dev server...
    echo Open http://localhost:5173 in your browser.
    call npm run dev
)

pause