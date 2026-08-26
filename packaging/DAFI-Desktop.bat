@echo off
title DAFI Desktop - Starting...
echo.
echo ========================================
echo   DAFI Desktop - Despacho de Asesoria
echo          Funeraria Integral
echo ========================================
echo.
echo Checking dependencies...
echo.

REM Run dependency check/installer
powershell -ExecutionPolicy Bypass -File "%~dp0check-deps.ps1" -Silent
if %ERRORLEVEL% neq 0 (
    echo.
    echo WARNING: Some dependencies may not be installed correctly.
    echo The application may not start without Java 17+.
    echo.
    echo Press any key to continue anyway, or close this window to abort.
    pause >nul
)

echo.
echo Starting DAFI Desktop...
echo.

REM Launch the application
set JAVA_MODULE_PATH=%~dp0app
set JAVA_OPTIONS=-Dfile.encoding=UTF-8 --add-modules javafx.controls,javafx.fxml -Xmx512m

REM Find java.exe in known locations
where java >nul 2>&1
if %ERRORLEVEL% equ 0 (
    java %JAVA_OPTIONS% -cp "%~dp0app\*" com.dafi.desktop.infrastructure.DafiLauncher %*
    goto :end
)

REM Check LOCALAPPDATA
if exist "%LOCALAPPDATA%\DAFI-Desktop\jdk\bin\java.exe" (
    "%LOCALAPPDATA%\DAFI-Desktop\jdk\bin\java.exe" %JAVA_OPTIONS% -cp "%~dp0app\*" com.dafi.desktop.infrastructure.DafiLauncher %*
    goto :end
)

REM Check Program Files
if exist "C:\Program Files\Eclipse Adoptium\jdk-17*\bin\java.exe" (
    for /d %%i in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do (
        "%%i\bin\java.exe" %JAVA_OPTIONS% -cp "%~dp0app\*" com.dafi.desktop.infrastructure.DafiLauncher %*
        goto :end
    )
)

REM Check JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        "%JAVA_HOME%\bin\java.exe" %JAVA_OPTIONS% -cp "%~dp0app\*" com.dafi.desktop.infrastructure.DafiLauncher %*
        goto :end
    )
)

REM Last resort: try java directly
java %JAVA_OPTIONS% -cp "%~dp0app\*" com.dafi.desktop.infrastructure.DafiLauncher %*

:end
if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: Application failed to start.
    echo Please ensure Java 17+ is installed.
    echo.
    echo You can download Java from: https://adoptium.net/
    echo.
    pause
)
