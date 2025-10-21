package com.catan.rivals.net;

import com.catan.rivals.game.GameEngine;
import com.catan.rivals.player.*;
import java.io.*;
import java.net.*;

/**
 * Game server for network multiplayer.
 * Hosts a game and accepts one remote player connection.
 * 
 * SOLID: Single Responsibility - handles network hosting only
 * Design Pattern: Proxy - RemotePlayerProxy represents remote player
 */
public class GameServer {
    
    private static final int DEFAULT_PORT = 2048;
    private ServerSocket serverSocket;
    private GameEngine gameEngine;
    
    /**
     * Constructor.
     * 
     * @param port The port to listen on
     * @throws IOException If server cannot be started
     */
    public GameServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.gameEngine = new GameEngine();
        System.out.println("Server started on port " + port);
    }
    
    /**
     * Starts the server and waits for connections.
     * 
     * @throws IOException If connection fails
     */
    public void start() throws IOException {
        System.out.println("Waiting for player connection...");
        
        // Add local player (server host)
        HumanPlayer localPlayer = new HumanPlayer("Host");
        gameEngine.addPlayer(localPlayer);
        
        // Accept remote player connection
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket.getInetAddress());
        
        // Create remote player proxy
        RemotePlayerProxy remotePlayer = new RemotePlayerProxy("Remote Player");
        remotePlayer.setConnection(clientSocket);
        gameEngine.addPlayer(remotePlayer);
        
        // Initialize and start game
        gameEngine.initialize("cards.json");
        gameEngine.startGame();
        
        // Cleanup
        remotePlayer.closeConnection();
        serverSocket.close();
    }
    
    /**
     * Main entry point for server.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port, using default: " + DEFAULT_PORT);
            }
        }
        
        try {
            GameServer server = new GameServer(port);
            server.start();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
