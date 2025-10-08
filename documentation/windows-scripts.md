# Windows Scripts & Shortcuts

## Scripts (repo root)
- `build-and-run.bat`: build JAR and start app
- `run-jar.bat`: run packaged JAR
- `domain-checker-launcher.bat`: used by desktop shortcut
- PowerShell helpers: `create-desktop-shortcut.ps1`, `create-jar-shortcut.ps1`, `make-shortcut.ps1`, `create-shortcut-launcher.ps1`

## Desktop shortcut flow
Shortcut → launcher BAT → `java -jar target\domain_checker.jar`.

## Tips
- Ensure Java is on PATH
- Rebuild JAR after code changes
- Run PowerShell scripts with execution policy permitting local scripts (e.g., `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`)


