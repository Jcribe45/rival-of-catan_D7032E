package com.catan.rivals.player;

import java.io.*;
import java.net.Socket;

/**
 * Remote player proxy for network play.
 * 
 * Design Pattern: Proxy Pattern - represents remote player
 * SOLID: Liskov Substitution - can substitute Player
 * SOLID: Single Responsibility - handles network I/O only
 */
public class RemotePlayerProxy extends Player {
    
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String playerName;
    
    /**
     * Constructor.
     * 
     * @param playerName The player's name
     */
    public RemotePlayerProxy(String playerName) {
        super();
        this.playerName = playerName;
    }
    
    /**
     * Sets up the network connection.
     * 
     * @param socket The socket connection
     * @throws IOException If connection setup fails
     */
    public void setConnection(Socket socket) throws IOException {
        this.socket = socket;
        // IMPORTANT: Create output stream first, then input stream
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.outputStream.flush();
        this.inputStream = new ObjectInputStream(socket.getInputStream());
    }
    
    /**
     * Sets up connection with pre-created streams.
     * 
     * @param socket The socket
     * @param input Input stream
     * @param output Output stream
     */
    public void setConnection(Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        this.socket = socket;
        this.inputStream = input;
        this.outputStream = output;
    }
    
    @Override
    public void sendMessage(String message) {
        if (outputStream == null) {
            System.err.println("Cannot send message: not connected");
            return;
        }
        
        try {
            outputStream.writeObject(message);
            outputStream.flush();
            outputStream.reset(); // Prevent memory leak from object caching
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
    
    @Override
    public String receiveInput() {
        if (inputStream == null) {
            System.err.println("Cannot receive input: not connected");
            return "";
        }
        
        try {
            Object obj = inputStream.readObject();
            return obj == null ? "" : obj.toString();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error receiving input: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Closes the network connection.
     */
    public void closeConnection() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Checks if connected.
     * 
     * @return True if connected
     */
    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    @Override
    public String toString() {
        return playerName + " (Remote)";
    }
}
