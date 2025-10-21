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
    
    private Socket socket;
    private Connection connection;
    private Scanner consoleInput;
    
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
                
                // Display to console
                System.out.println(message);
                
                // Check if this is a prompt that needs a response
                if (message.startsWith("PROMPT:") || message.contains("Your choice:") || 
                    message.contains("Choose") || message.endsWith(">")) {
                    
                    // Read response from console
                    System.out.print("> ");
                    String response = consoleInput.nextLine();
                    
                    // Send response to server
                    connection.sendMessage(response);
                }
                
                // Check for game end
                if (message.toLowerCase().contains("game over") || 
                    message.toLowerCase().contains("winner")) {
                    System.out.println("Game ended. Press Enter to exit.");
                    consoleInput.nextLine();
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Connection error: " + e.getMessage());
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
     * Main entry point for client.
     * 
     * @param args Command line arguments: [host] [port]
     */
    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        
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
        
        try {
            GameClient client = new GameClient(host, port);
            client.start();
        } catch (IOException e) {
            System.err.println("Failed to connect: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
