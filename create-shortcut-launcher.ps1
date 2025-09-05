# Create Desktop Shortcut for Domain Checker (Batch Launcher Version)
Write-Host "Creating desktop shortcut for Domain Checker..." -ForegroundColor Green

$WScriptShell = New-Object -ComObject WScript.Shell
$DesktopPath = [System.Environment]::GetFolderPath([System.Environment+SpecialFolder]::Desktop)
$ShortcutPath = "$DesktopPath\Domain Checker.lnk"

$Shortcut = $WScriptShell.CreateShortcut($ShortcutPath)
$Shortcut.TargetPath = "$PWD\domain-checker-launcher.bat"
$Shortcut.WorkingDirectory = "$PWD"
$Shortcut.IconLocation = "shell32.dll,21"  # Globe icon
$Shortcut.Description = "Domain Checker Application - Check domains for reachability, HTTP status, and security"
$Shortcut.WindowStyle = 1  # Normal window
$Shortcut.Save()

Write-Host ""
Write-Host "✅ Desktop shortcut created successfully!" -ForegroundColor Green
Write-Host "📍 Shortcut: 'Domain Checker' on your desktop" -ForegroundColor Yellow
Write-Host "🚀 Double-click to start the Domain Checker application" -ForegroundColor Cyan
Write-Host "🌐 App will open at: http://localhost:8080" -ForegroundColor Magenta
Write-Host ""
Write-Host "Features of this shortcut:" -ForegroundColor White
Write-Host "- Pretty startup screen with ASCII art" -ForegroundColor Gray
Write-Host "- Clear instructions and URL display" -ForegroundColor Gray
Write-Host "- Easy to stop (close window or Ctrl+C)" -ForegroundColor Gray