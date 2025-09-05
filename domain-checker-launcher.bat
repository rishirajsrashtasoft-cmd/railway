@echo off
title Domain Checker Application - Rishiraj Vishwakarma
color 0A
echo.
echo  ████████▄   ████████▄   ████████▄
echo  ██     ██   ██     ██   ██     ██
echo  ██     ██   ██     ██   ██     ██
echo  ████████▀   ████████▀   ████████▀
echo.
echo  ╔══════════════════════════════════════╗
echo  ║        Domain Checker Application    ║
echo  ║     Developed by Rishiraj Vishwakarma ║
echo  ╚══════════════════════════════════════╝
echo.
echo  🚀 Starting application...
echo  🌐 The app will be available at: http://localhost:8080
echo.
echo  📋 Features:
echo     • Domain reachability check
echo     • HTTP status verification  
echo     • VirusTotal security scan
echo     • Export to CSV, PDF, Word
echo.
echo  ⚠️  Press Ctrl+C to stop the application
echo  ❌ Close this window to stop the application
echo.

cd /d "%~dp0"
java -jar target\domain_checker.jar

echo.
echo Application stopped.
pause