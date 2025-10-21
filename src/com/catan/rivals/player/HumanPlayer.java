package com.catan.rivals.player;

import java.util.Scanner;

/**
 * Human player implementation using console input/output.
 * 
 * Design Pattern: Template Method - uses Player's template methods
 * SOLID: Liskov Substitution - can substitute Player
 * SOLID: Single Responsibility - handles console I/O only
 */
public class HumanPlayer extends Player {
    
    private Scanner scanner;
    private String playerName;
    
    /**
     * Constructor.
     * 
     * @param playerName The player's name
     */
    public HumanPlayer(String playerName) {
        super();
        this.playerName = playerName;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Default constructor.
     */
    public HumanPlayer() {
        this("Player");
    }
    
    @Override
    public void sendMessage(String message) {
        System.out.println("[" + playerName + "] " + message);
    }
    
    @Override
    public String receiveInput() {
        System.out.print("[" + playerName + "] > ");
        return scanner.nextLine();
    }
    
    /**
     * Gets the player's name.
     * 
     * @return The name
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Sets the player's name.
     * 
     * @param playerName The new name
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    @Override
    public String toString() {
        return playerName + " (Human)";
    }
}
