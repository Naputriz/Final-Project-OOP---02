# Maestra Trials - Build and Package Script

$ErrorActionPreference = "Stop"

Write-Host ">>> Starting Build Process..." -ForegroundColor Cyan

# Ask if Backend should be retained (Skip Build)
$BuildBackend = $true
$response = Read-Host "Do you want to rebuild the backend? (Y/n) [Default: Y]"
if ($response -eq "n" -or $response -eq "N") {
    $BuildBackend = $false
}

# 1. Build Backend
if ($BuildBackend) {
    Write-Host "`n>>> Building Backend (Spring Boot)..." -ForegroundColor Cyan
    Push-Location backend
    ./gradlew bootJar
    if ($LASTEXITCODE -ne 0) { Write-Error "Backend build failed!"; exit 1 }
    Pop-Location
} else {
    Write-Host "`n>>> Skipping Backend Build (Using existing JAR if present)..." -ForegroundColor Yellow
    if (-not (Test-Path "backend/build/libs/backend-0.0.1-SNAPSHOT.jar")) {
        Write-Error "Cannot skip Backend Build: 'backend/build/libs/backend-0.0.1-SNAPSHOT.jar' is missing! Please Run again and select 'Y'."
        exit 1
    }
}

# 2. Build Frontend
Write-Host "`n>>> Building Frontend (LibGDX)..." -ForegroundColor Cyan
Push-Location Frontend
./gradlew lwjgl3:dist
if ($LASTEXITCODE -ne 0) { Write-Error "Frontend build failed!"; exit 1 }
Pop-Location

# 3. Setup Release Directory
$ReleaseDir = "MaestraTrialsRelease"
Write-Host "`n>>> Setting up Release Directory: $ReleaseDir..." -ForegroundColor Cyan

if (Test-Path $ReleaseDir) {
    Remove-Item $ReleaseDir -Recurse -Force
}
New-Item -ItemType Directory -Path $ReleaseDir | Out-Null

# Copy Jars
Copy-Item "backend/build/libs/backend-0.0.1-SNAPSHOT.jar" -Destination "$ReleaseDir/backend.jar"
Copy-Item "Frontend/lwjgl3/build/libs/Maestra Trials-1.0.0.jar" -Destination "$ReleaseDir/game.jar"

# Copy Assets
Write-Host ">>> Copying Assets..." -ForegroundColor Cyan
Copy-Item "Frontend/assets" -Destination "$ReleaseDir/assets" -Recurse

Write-Host "`n>>> JAR Distribution Created in $ReleaseDir" -ForegroundColor Green
Write-Host "    You can run it with: java -jar $ReleaseDir/game.jar"

# 4. Create EXE with jpackage
Write-Host "`n>>> Creating EXE Installer with jpackage..." -ForegroundColor Cyan
$InstallerDir = "InstallerOutput"
if (Test-Path $InstallerDir) {
    Remove-Item $InstallerDir -Recurse -Force
}

# Note: Using --icon would require an .ico file on Windows. Skipping for default java icon.
jpackage `
  --name "MaestraTrials" `
  --input "$ReleaseDir" `
  --main-jar "game.jar" `
  --type app-image `
  --dest "$InstallerDir" `
  --win-console `
  --java-options "-Xmx1024m"

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n>>> SUCCESS!" -ForegroundColor Green
    Write-Host "    Standalone Application created at: $InstallerDir/MaestraTrials/MaestraTrials.exe"
    Write-Host "    (You can zip the '$InstallerDir/MaestraTrials' folder and share it!)"
} else {
    Write-Error "jpackage failed!"
}
