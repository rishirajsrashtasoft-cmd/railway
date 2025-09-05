# Create Desktop Shortcut for Domain Checker JAR
Write-Host "Creating desktop shortcut for Domain Checker..." -ForegroundColor Green

$WScriptShell = New-Object -ComObject WScript.Shell
$DesktopPath = [System.Environment]::GetFolderPath([System.Environment+SpecialFolder]::Desktop)
$ShortcutPath = "$DesktopPath\Domain Checker.lnk"

$Shortcut = $WScriptShell.CreateShortcut($ShortcutPath)
$Shortcut.TargetPath = "java.exe"
$Shortcut.Arguments = "-jar `"$PWD\target\domain_checker.jar`""
$Shortcut.WorkingDirectory = "$PWD"
$Shortcut.IconLocation = "java.exe,0"  # Use Java icon
$Shortcut.Description = "Domain Checker Application - Check domains for reachability, HTTP status, and security"
$Shortcut.WindowStyle = 1  # Normal window
$Shortcut.Save()

Write-Host ""
Write-Host "✅ Desktop shortcut created successfully!" -ForegroundColor Green
Write-Host "📍 Location: $ShortcutPath" -ForegroundColor Yellow
Write-Host "🚀 Double-click 'Domain Checker' on your desktop to start the app" -ForegroundColor Cyan
Write-Host "🌐 App will be available at: http://localhost:8080" -ForegroundColor Magenta
Write-Host ""
Write-Host "Note: Make sure you have built the JAR file first with:" -ForegroundColor White
Write-Host "mvn clean package -DskipTests" -ForegroundColor Gray