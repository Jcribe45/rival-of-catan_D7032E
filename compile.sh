#!/bin/bash

# Compilation script for Rivals for Catan (Unix / Git Bash / WSL)
# Usage: ./Compile.sh

set -euo pipefail

echo "=== Compiling Rivals for Catan ==="

# Create bin directory if it doesn't exist
mkdir -p bin

# Ensure gson.jar exists (download from Maven central if missing)
GSON_URL="https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
if [ ! -f gson.jar ]; then
    echo "gson.jar not found. Downloading gson-2.10.1 from Maven Central..."
    if command -v curl >/dev/null 2>&1; then
        curl -L -o gson.jar "$GSON_URL"
    elif command -v wget >/dev/null 2>&1; then
        wget -O gson.jar "$GSON_URL"
    else
        echo "Error: curl or wget is required to download gson.jar. Please download it and place it in the project root as gson.jar"
        exit 1
    fi
fi

# Compile command using Unix classpath separator ':'
CP=".:gson.jar"

echo "Compiling source files..."
javac -cp "$CP" -d bin \
        src/com/catan/rivals/util/*.java \
        src/com/catan/rivals/model/*.java \
        src/com/catan/rivals/player/Player.java \
        src/com/catan/rivals/player/HumanPlayer.java \
        src/com/catan/rivals/player/RemotePlayerProxy.java \
        src/com/catan/rivals/game/*.java \
        src/com/catan/rivals/game/phase*.java \
        src/com/catan/rivals/game/event*.java \
        src/com/catan/rivals/net/*.java

if [ $? -eq 0 ]; then
        echo "Compilation successful!"
        echo ""
        echo "To run the game:"
        echo "  Local (2 players):  java -cp \".:gson.jar:bin\" com.catan.rivals.game.GameEngine"
        echo "  Server:             java -cp \".:gson.jar:bin\" com.catan.rivals.net.GameServer"
        echo "  Client:             java -cp \".:gson.jar:bin\" com.catan.rivals.net.GameClient"
        echo ""
else
        echo "Compilation failed!"
        exit 1
fi
