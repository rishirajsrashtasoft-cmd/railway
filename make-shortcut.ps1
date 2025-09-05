# Create Desktop Shortcut for Domain Checker
Write-Host "Creating desktop shortcut..." -ForegroundColor Green

$WScriptShell = New-Object -ComObject WScript.Shell
$DesktopPath = [System.Environment]::GetFolderPath([System.Environment+SpecialFolder]::Desktop)
$ShortcutPath = "$DesktopPath\Domain Checker.lnk"

$Shortcut = $WScriptShell.CreateShortcut($ShortcutPath)
$Shortcut.TargetPath = "$PWD\domain-checker-launcher.bat"
$Shortcut.WorkingDirectory = "$PWD"
$Shortcut.IconLocation = "shell32.dll,21"
$Shortcut.Description = "Domain Checker Application"
$Shortcut.Save()

Write-Host "Desktop shortcut created successfully!" -ForegroundColor Green
Write-Host "Look for 'Domain Checker' icon on your desktop" -ForegroundColor Yellow