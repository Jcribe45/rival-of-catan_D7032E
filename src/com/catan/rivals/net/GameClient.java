package com.catan.rivals.net;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Game client for connecting to a server.
 * Handles client-side network communication.
 * 
 * SOLID: Single Responsibility - handles client connection only
 */
public class GameClient {
    
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 2048;
    private static final String INPUT_PROMPT = "INPUT_PROMPT";
    
    private Socket socket;
    private Connection connection;
    private Scanner consoleInput;
    @SuppressWarnings("unused")
    private String playerName;
    
    /**
     * Constructor.
     * 
     * @param host The server hostname
     * @param port The server port
     * @throws IOException If connection fails
     */
    public GameClient(String host, int port) throws IOException {
        System.out.println("Connecting to " + host + ":" + port + "...");
        this.socket = new Socket(host, port);
        this.connection = new Connection(socket);
        this.consoleInput = new Scanner(System.in);
        this.playerName = "Remote Player"; // Default name
        System.out.println("Connected to server!");
    }
    
    /**
     * Starts the client message loop.
     */
    public void start() {
        try {
            while (connection.isOpen()) {
                // Receive message from server
                String message = connection.receiveMessage();
                
                // Check if this is an input prompt
                if (INPUT_PROMPT.equals(message)) {
                    // Server is requesting input
                    System.out.print("> ");
                    String response = consoleInput.nextLine();
                    
                    // Send response to server
                    connection.sendMessage(response);
                    continue;
                }
                
                // Regular message - display it
                System.out.println(message);
                
                // Check for game end
                if (message.toLowerCase().contains("game over") || 
                    message.toLowerCase().contains("winner")) {
                    System.out.println("\nPress Enter to exit.");
                    consoleInput.nextLine();
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    /**
     * Cleans up resources.
     */
    private void cleanup() {
        if (connection != null) {
            connection.close();
        }
        if (consoleInput != null) {
            consoleInput.close();
        }
    }
    
    /**
     * Sets the player name (optional).
     * 
     * @param playerName The player name
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    /**
     * Main entry point for client.
     * 
     * @param args Command line arguments: [host] [port] [player_name]
     */
    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        String playerName = "Remote Player";
        
        if (args.length > 0) {
            host = args[0];
        }
        
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port, using default: " + DEFAULT_PORT);
            }
        }
        
        if (args.length > 2) {
            playerName = args[2];
        }
        
        try {
            GameClient client = new GameClient(host, port);
            client.setPlayerName(playerName);
            
            System.out.println("Connected as: " + playerName);
            System.out.println("Waiting for game to start...\n");
            
            client.start();
        } catch (IOException e) {
            System.err.println("Failed to connect: " + e.getMessage());
            e.printStackTrace();
        }
    }
}