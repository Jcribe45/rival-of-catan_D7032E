package com.catan.rivals.net;

import java.io.*;
import java.net.Socket;

/**
 * Utility class for managing network connections.
 * 
 * SOLID: Single Responsibility - handles connection management only
 * Booch: High cohesion - all connection operations together
 */
public class Connection {
    
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    
    /**
     * Constructor.
     * 
     * @param socket The socket
     * @throws IOException If streams cannot be created
     */
    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        // IMPORTANT: Create output stream first, then input stream
        this.output = new ObjectOutputStream(socket.getOutputStream());
        this.output.flush();
        this.input = new ObjectInputStream(socket.getInputStream());
    }
    
    /**
     * Sends an object over the connection.
     * 
     * @param obj The object to send
     * @throws IOException If send fails
     */
    public void send(Object obj) throws IOException {
        output.writeObject(obj);
        output.flush();
        output.reset(); // Prevent caching issues
    }
    
    /**
     * Receives an object from the connection.
     * 
     * @return The received object
     * @throws IOException If receive fails
     * @throws ClassNotFoundException If class not found
     */
    public Object receive() throws IOException, ClassNotFoundException {
        return input.readObject();
    }
    
    /**
     * Sends a string message.
     * 
     * @param message The message
     * @throws IOException If send fails
     */
    public void sendMessage(String message) throws IOException {
        send(message);
    }
    
    /**
     * Receives a string message.
     * 
     * @return The message
     * @throws IOException If receive fails
     * @throws ClassNotFoundException If class not found
     */
    public String receiveMessage() throws IOException, ClassNotFoundException {
        Object obj = receive();
        return obj == null ? "" : obj.toString();
    }
    
    /**
     * Closes the connection.
     */
    public void close() {
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Checks if connection is open.
     * 
     * @return True if open
     */
    public boolean isOpen() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }
    
    /**
     * Gets the remote address.
     * 
     * @return The address string
     */
    public String getRemoteAddress() {
        if (socket != null) {
            return socket.getInetAddress().getHostAddress();
        }
        return "unknown";
    }
}
