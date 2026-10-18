@echo off
echo ========================================================
echo   Compiling and Starting Smart Timetable Maker Server...
echo ========================================================

javac *.java
if %errorlevel% neq 0 (
    echo [ERROR] Java compilation failed! Please check your JDK installation.
    pause
    exit /b %errorlevel%
)

echo.
echo Starting Smart Timetable Maker Server...
echo Open your browser and navigate to: http://localhost:8080
echo Press Ctrl+C in this terminal to stop the server.
echo.

java AppServer
pause
