package com.catan.rivals.player;

import com.catan.rivals.model.*;
import com.catan.rivals.player.action.*;
import com.catan.rivals.player.ui.PlayerUI;
import com.catan.rivals.util.*;
import java.util.*;

/**
 * Refactored Player class with reduced responsibilities.
 * 
 * Design Pattern: Template Method + Facade
 * SOLID: Single Responsibility - manages player state only
 * Delegates actions to ActionFactory
 * Delegates UI to PlayerUI
 */
public abstract class Player {
    
    // State
    protected Principality principality;
    protected ResourceBank resourceBank;
    protected List<Card> hand;
    protected EffectTracker effectTracker;
    
    // Points
    protected int victoryPoints;
    protected int commercePoints;
    protected int skillPoints;
    protected int strengthPoints;
    protected int progressPoints;
    
    // Flags
    protected Set<String> flags;
    
    // UI helper
    protected PlayerUI ui;
    
    /**
     * Constructor.
     */
    public Player() {
        this.principality = new Principality();
        this.resourceBank = new ResourceBank(principality);
        this.hand = new ArrayList<>();
        this.effectTracker = new EffectTracker();
        this.flags = new HashSet<>();
        
        this.victoryPoints = 0;
        this.commercePoints = 0;
        this.skillPoints = 0;
        this.strengthPoints = 0;
        this.progressPoints = 0;
        
        this.ui = new PlayerUI(this);
    }
    
    // ========== Abstract Methods ==========
    
    public abstract void sendMessage(String message);
    public abstract String receiveInput();
    
    // ========== Template Method for Actions ==========
    
    /**
     * Template method for taking actions.
     * Delegates to ActionFactory for command execution.
     */
    public void takeActions(Player opponent, Deck deck) {
        boolean done = false;
        
        while (!done) {
            ui.displayGameState(opponent);
            ui.displayActionMenu();
            
            sendMessage("Your choice:");
            String input = receiveInput();
            
            done = executeAction(input, opponent, deck);
        }
    }
    
    /**
     * Executes an action using ActionFactory.
     */
    protected boolean executeAction(String input, Player opponent, Deck deck) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        String command = input.trim().toUpperCase().split("\\s+")[0];
        
        if (command.equals("END")) {
            return true;
        }
        
        if (command.equals("VIEW")) {
            ui.displayGameState(opponent);
            return false;
        }
        
        PlayerAction action = ActionFactory.getAction(command);
        
        if (action != null) {
            return action.execute(this, opponent, deck);
        } else {
            sendMessage("Unknown command: " + command);
            sendMessage(ActionFactory.getActionHelp());
            return false;
        }
    }
    
    // ========== Card Selection Helpers ==========
    
    /**
     * Prompts player to choose a card from hand.
     */
    public int chooseCardFromHand(String prompt, boolean allowCancel) {
        if (hand.isEmpty()) {
            sendMessage("Your hand is empty!");
            return -1;
        }
        
        sendMessage("\n=== Your Hand ===");
        for (int i = 0; i < hand.size(); i++) {
            sendMessage(String.format("[%d] %s", i, hand.get(i).getName()));
        }
        
        String cancelText = allowCancel ? " or 'C' to cancel" : "";
        sendMessage(prompt + " (0-" + (hand.size() - 1) + ")" + cancelText + ":");
        
        String input = receiveInput();
        
        if (allowCancel && input.trim().toUpperCase().equals("C")) {
            return -1;
        }
        
        try {
            int index = Integer.parseInt(input.trim());
            if (index >= 0 && index < hand.size()) {
                return index;
            }
        } catch (NumberFormatException e) {
            // Fall through
        }
        
        sendMessage("Invalid choice!");
        return -1;
    }
    
    /**
     * Prompts player to choose a draw stack.
     */
    public int chooseDrawStack(Deck deck, String prompt, boolean allowCancel) {
        String cancelText = allowCancel ? " or 'C' to cancel" : "";
        sendMessage(prompt + " (1-4)" + cancelText + ":");
        
        String input = receiveInput();
        
        if (allowCancel && input.trim().toUpperCase().equals("C")) {
            return -1;
        }
        
        try {
            int stackNum = Integer.parseInt(input.trim());
            if (stackNum >= 1 && stackNum <= 4) {
                if (deck.isStackEmpty(stackNum)) {
                    sendMessage("Stack " + stackNum + " is empty!");
                    return -1;
                }
                return stackNum;
            }
        } catch (NumberFormatException e) {
            // Fall through
        }
        
        sendMessage("Invalid stack number!");
        return -1;
    }
    
    /**
     * Chooses a card from a stack (for paid exchanges).
     */
    public Card chooseCardFromStack(List<Card> stack, String prompt, boolean allowCancel) {
        if (stack == null || stack.isEmpty()) {
            sendMessage("Stack is empty!");
            return null;
        }
        
        sendMessage("\n=== Cards in Stack ===");
        for (int i = 0; i < stack.size(); i++) {
            sendMessage(String.format("[%d] %s - %s",
                i, stack.get(i).getName(), stack.get(i).getCardType().getDisplayName()));
        }
        
        String cancelText = allowCancel ? " or 'C' to cancel" : "";
        sendMessage(prompt + " (0-" + (stack.size() - 1) + ")" + cancelText + ":");
        
        String input = receiveInput();
        
        if (allowCancel && input.trim().toUpperCase().equals("C")) {
            return null;
        }
        
        try {
            int index = Integer.parseInt(input.trim());
            if (index >= 0 && index < stack.size()) {
                return stack.remove(index);
            }
        } catch (NumberFormatException e) {
            // Fall through
        }
        
        sendMessage("Invalid index!");
        return null;
    }
    
    /**
     * Prompts for resource type selection.
     */
    public ResourceType chooseResourceType(String prompt) {
        sendMessage(prompt);
        sendMessage("Options: Brick, Grain, Lumber, Wool, Ore, Gold");
        sendMessage("Your choice:");
        
        String input = receiveInput();
        ResourceType type = ResourceType.fromString(input);
        
        if (type == null) {
            sendMessage("Invalid resource type!");
        }
        
        return type;
    }
    
    // ========== Card Management ==========
    
    public void addCardToHand(Card card) {
        if (card != null) {
            hand.add(card);
        }
    }
    
    public Card removeCardFromHand(int index) {
        if (index >= 0 && index < hand.size()) {
            return hand.remove(index);
        }
        return null;
    }
    
    // ========== Getters/Setters ==========
    
    public Principality getPrincipality() { return principality; }
    public ResourceBank getResourceBank() { return resourceBank; }
    public List<Card> getHand() { return hand; }
    public EffectTracker getEffectTracker() { return effectTracker; }
    public Set<String> getFlags() { return flags; }
    public PlayerUI getUI() { return ui; }
    
    public int getVictoryPoints() { return victoryPoints; }
    public void setVictoryPoints(int points) { this.victoryPoints = points; }
    public void addVictoryPoints(int points) { this.victoryPoints += points; }
    
    public int getCommercePoints() { return commercePoints; }
    public void setCommercePoints(int points) { this.commercePoints = points; }
    public void addCommercePoints(int points) { this.commercePoints += points; }
    
    public int getSkillPoints() { return skillPoints; }
    public void setSkillPoints(int points) { this.skillPoints = points; }
    public void addSkillPoints(int points) { this.skillPoints += points; }
    
    public int getStrengthPoints() { return strengthPoints; }
    public void setStrengthPoints(int points) { this.strengthPoints = points; }
    public void addStrengthPoints(int points) { this.strengthPoints += points; }
    
    public int getProgressPoints() { return progressPoints; }
    public void setProgressPoints(int points) { this.progressPoints = points; }
    public void addProgressPoints(int points) { this.progressPoints += points; }
}