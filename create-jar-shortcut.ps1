# Create Desktop Shortcut for Domain Checker (JAR Version)
$WScriptShell = New-Object -ComObject WScript.Shell
$Shortcut = $WScriptShell.CreateShortcut("$Home\Desktop\Domain Checker (Fast).lnk")
$Shortcut.TargetPath = "$PWD\run-jar.bat"
$Shortcut.WorkingDirectory = "$PWD"
$Shortcut.IconLocation = "shell32.dll,21"  # Different icon for JAR version
$Shortcut.Description = "Start Domain Checker Application (JAR - Fast Startup)"
$Shortcut.Save()

Write-Host "Desktop shortcut for JAR version created successfully!" -ForegroundColor Green
Write-Host "Icon: 'Domain Checker (Fast)' on your desktop" -ForegroundColor Yellow
Write-Host "This version starts in 3-5 seconds!" -ForegroundColor Cyan