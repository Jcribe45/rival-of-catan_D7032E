#!/usr/bin/env pwsh
# compile.ps1 - PowerShell script to compile Rivals for Catan on Windows
Write-Host "=== Compiling Rivals for Catan (Windows) ==="

# Ensure bin exists
if (-not (Test-Path bin)) {
    New-Item -ItemType Directory -Path bin | Out-Null
}

# Ensure gson.jar exists (download from Maven central if missing)
$gson = Join-Path -Path (Get-Location) -ChildPath "gson.jar"
if (-not (Test-Path $gson)) {
    Write-Host "gson.jar not found. Downloading gson-2.10.1 from Maven Central..."
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar" -OutFile $gson
}

# Compile command using Windows classpath separator ';'
$cp = ".;gson.jar"

Write-Host "Compiling source files..."
javac -cp $cp -d bin `
    src\com\catan\rivals\util\GameObserver.java `
    src\com\catan\rivals\util\JsonUtils.java `
    src\com\catan\rivals\util\CardFactory.java `
    src\com\catan\rivals\util\PrincipalityRenderer.java `
    src\com\catan\rivals\model\*.java `
    src\com\catan\rivals\player\Player.java `
    src\com\catan\rivals\player\HumanPlayer.java `
    src\com\catan\rivals\player\RemotePlayerProxy.java `
    src\com\catan\rivals\game\*.java `
    src\com\catan\rivals\game\phase\*.java `
    src\com\catan\rivals\game\event\*.java `
    src\com\catan\rivals\net\*.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!"
    Write-Host ""
    Write-Host "To run the game:"
    Write-Host '  Local:  java -cp ".;gson.jar;bin" com.catan.rivals.game.GameEngine'
    Write-Host '  Server: java -cp ".;gson.jar;bin" com.catan.rivals.net.GameServer'
    Write-Host '  Client: java -cp ".;gson.jar;bin" com.catan.rivals.net.GameClient'
    Write-Host ""
    Write-Host "Note: AI player removed. To add AI in future, implement the Player abstract class."
} else {
    Write-Host "Compilation failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}
