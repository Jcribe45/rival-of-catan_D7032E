package com.catan.rivals.player;

import com.catan.rivals.model.*;
import com.catan.rivals.game.GameSetup;
import com.catan.rivals.util.PrincipalityRenderer;
import java.util.*;

/**
 * Abstract base class for all player types.
 * FIXED: Proper building costs and validation per official rules.
 * 
 * Design Pattern: Template Method - defines player behavior skeleton
 * SOLID: Open-Closed - extensible for new player types (including AI)
 * SOLID: Liskov Substitution - all subtypes can be used interchangeably
 */
public abstract class Player {
    
    protected Principality principality;
    protected ResourceBank resourceBank;
    protected List<Card> hand;
    
    // Points
    protected int victoryPoints;
    protected int commercePoints;
    protected int skillPoints;
    protected int strengthPoints;
    protected int progressPoints;
    
    // Flags for special abilities (Marketplace, Parish Hall, etc.)
    protected Set<String> flags;
    
    /**
     * Constructor initializes player state.
     */
    public Player() {
        this.principality = new Principality();
        this.resourceBank = new ResourceBank(principality);
        this.hand = new ArrayList<>();
        this.flags = new HashSet<>();
        this.victoryPoints = 0;
        this.commercePoints = 0;
        this.skillPoints = 0;
        this.strengthPoints = 0;
        this.progressPoints = 0;
    }
    
    // ========== Abstract Methods (must be implemented by subclasses) ==========
    
    /**
     * Sends a message to this player.
     * 
     * @param message The message to send
     */
    public abstract void sendMessage(String message);
    
    /**
     * Receives input from this player.
     * 
     * @return The input string
     */
    public abstract String receiveInput();
    
    // ========== Template Methods ==========
    
    /**
     * Template method for taking actions during action phase.
     * Subclasses can override specific steps if needed.
     * 
     * @param opponent The opponent player
     * @param deck The game deck
     */
    public void takeActions(Player opponent, Deck deck) {
        boolean done = false;
        
        while (!done) {
            displayBothBoards(opponent);
            displayHand();
            displayResources();
            String action = promptForAction();
            done = executeAction(action, opponent, deck);
        }
    }
    
    /**
     * Displays both the player's and opponent's principalities.
     * This provides full game state visibility.
     * Uses centralized PrincipalityRenderer for consistent formatting.
     * 
     * @param opponent The opponent player
     */
    protected void displayBothBoards(Player opponent) {
        sendMessage(PrincipalityRenderer.renderBothPrincipalities(this, opponent));
    }
    
    /**
     * Displays the player's principality.
     * Uses centralized PrincipalityRenderer.
     */
    protected void displayBoard() {
        sendMessage("\n=== Your Principality ===");
        sendMessage(PrincipalityRenderer.renderPrincipality(this));
    }
    
    /**
     * Displays the player's hand.
     * Default implementation, can be overridden.
     */
    protected void displayHand() {
        sendMessage("\n=== Your Hand (" + hand.size() + " cards) ===");
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            sendMessage(String.format("[%d] %s (Cost: %s, Type: %s)", 
                i, card.getName(), card.getCostString(), card.getCardType()));
        }
        sendMessage("");
    }
    
    /**
     * Displays player's available resources.
     */
    protected void displayResources() {
        sendMessage("\n=== Your Resources ===");
        Map<ResourceType, Integer> resources = resourceBank.getResourceSummary();
        for (Map.Entry<ResourceType, Integer> entry : resources.entrySet()) {
            if (entry.getValue() > 0) {
                sendMessage(String.format("  %s: %d", entry.getKey().getDisplayName(), entry.getValue()));
            }
        }
        sendMessage(String.format("Total: %d resources\n", resourceBank.getTotalResources()));
    }
    
    /**
     * Prompts for and returns the player's action choice.
     * Can be overridden for different input methods.
     * 
     * @return The action string
     */
    protected String promptForAction() {
        sendMessage("Choose action:");
        sendMessage("  PLAY <index> <row> <col> - Play card from hand");
        sendMessage("  BUILD <Road|Settlement|City> <row> <col> - Build center card");
        sendMessage("  TRADE3 <give> <get> - Trade 3:1 with bank");
        sendMessage("  VIEW - View boards again");
        sendMessage("  END - End action phase");
        sendMessage("\nYour choice: ");
        return receiveInput();
    }
    
    /**
     * Executes an action command.
     * 
     * @param action The action command
     * @param opponent The opponent
     * @param deck The game deck
     * @return True if action phase should end
     */
    protected boolean executeAction(String action, Player opponent, Deck deck) {
        if (action == null || action.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = action.trim().split("\\s+");
        String command = parts[0].toUpperCase();
        
        switch (command) {
            case "END":
                return true;
                
            case "VIEW":
                displayBothBoards(opponent);
                return false;
                
            case "PLAY":
                if (parts.length >= 4) {
                    return handlePlayCard(parts, opponent);
                }
                sendMessage("Usage: PLAY <index> <row> <col>");
                break;
                
            case "BUILD":
                if (parts.length >= 4) {
                    return handleBuild(parts, deck);
                }
                sendMessage("Usage: BUILD <Road|Settlement|City> <row> <col>");
                break;
                
            case "TRADE3":
                if (parts.length >= 3) {
                    return handleTrade3(parts);
                }
                sendMessage("Usage: TRADE3 <give> <get>");
                break;
                
            default:
                sendMessage("Unknown command: " + command);
        }
        
        return false;
    }
    
    // ========== Action Handlers ==========
    
    /**
     * Handles playing a card from hand.
     * FIXED: Improved validation and error messages.
     */
    protected boolean handlePlayCard(String[] parts, Player opponent) {
        try {
            int index = Integer.parseInt(parts[1]);
            int row = Integer.parseInt(parts[2]);
            int col = Integer.parseInt(parts[3]);
            
            if (index < 0 || index >= hand.size()) {
                sendMessage("Invalid card index (0-" + (hand.size()-1) + ")");
                return false;
            }
            
            Card card = hand.get(index);
            
            // Check if can afford
            if (!resourceBank.canAfford(card.getCost())) {
                sendMessage("Cannot afford card cost: " + card.getCostString());
                sendMessage("You need: " + card.getCostString());
                return false;
            }
            
            // Check if can place at that position
            if (!card.canApplyEffect(this, opponent, row, col)) {
                sendMessage("Cannot play card at position (" + row + "," + col + ")");
                sendMessage("Reasons might include:");
                sendMessage("  - Position is not empty");
                sendMessage("  - Card type cannot be placed there");
                sendMessage("  - Prerequisites not met");
                return false;
            }
            
            // Pay cost
            if (resourceBank.payCost(card.getCost())) {
                // Apply effect
                if (card.applyEffect(this, opponent, row, col)) {
                    hand.remove(index);
                    sendMessage("✓ Successfully played " + card.getName());
                    return false;
                }
                else {
                    sendMessage("✗ Failed to apply card effect");
                    // Refund resources (add them back)
                    for (Map.Entry<ResourceType, Integer> entry : card.getCost().entrySet()) {
                        resourceBank.addResources(entry.getKey(), entry.getValue());
                    }
                }
            }
            
            sendMessage("Failed to play card");
            
        } catch (NumberFormatException e) {
            sendMessage("Invalid number format");
        }
        
        return false;
    }
    
    /**
     * Handles building a center card (Road/Settlement/City).
     * FIXED: Uses correct costs from GameSetup.getCenterCardCost().
     */
    protected boolean handleBuild(String[] parts, Deck deck) {
        String type = parts[1].toUpperCase();
        
        try {
            int row = Integer.parseInt(parts[2]);
            int col = Integer.parseInt(parts[3]);
            
            // Validate type
            if (!type.equals("ROAD") && !type.equals("SETTLEMENT") && !type.equals("CITY")) {
                sendMessage("Invalid build type. Use: Road, Settlement, or City");
                return false;
            }
            
            // Get correct cost from GameSetup (FIXED)
            Map<ResourceType, Integer> cost = GameSetup.getCenterCardCost(type);
            
            if (cost.isEmpty()) {
                sendMessage("Invalid build type");
                return false;
            }
            
            // Check if can afford
            if (!resourceBank.canAfford(cost)) {
                sendMessage("Cannot afford " + type);
                sendMessage("Cost: " + formatCost(cost));
                sendMessage("You have: " + formatResources(resourceBank.getResourceSummary()));
                return false;
            }
            
            // Get card from deck
            Card cardToBuild = null;
            List<Card> sourceList = null;
            
            switch (type) {
                case "ROAD":
                    sourceList = deck.getRoads();
                    break;
                case "SETTLEMENT":
                    sourceList = deck.getSettlements();
                    break;
                case "CITY":
                    sourceList = deck.getCities();
                    break;
            }
            
            if (sourceList == null || sourceList.isEmpty()) {
                sendMessage("No " + type + " cards available in deck");
                return false;
            }
            
            cardToBuild = sourceList.get(0);
            cardToBuild.setCost(cost); // Ensure cost is set
            
            // Check placement rules
            if (!principality.isEmptyAt(row, col)) {
                sendMessage("Position (" + row + "," + col + ") is not empty");
                return false;
            }
            
            // Additional validation for specific types
            if (type.equals("CITY")) {
                // City must upgrade a settlement
                Card existing = principality.getCardAt(row, col);
                if (existing == null || !existing.getName().equalsIgnoreCase("Settlement")) {
                    sendMessage("City must be built on a Settlement");
                    return false;
                }
            }
            
            // Pay cost
            if (resourceBank.payCost(cost)) {
                sourceList.remove(0); // Remove from deck
                
                // Place card
                principality.placeCard(row, col, cardToBuild);
                
                // Add victory points
                if (type.equals("SETTLEMENT")) {
                    addVictoryPoints(1);
                } else if (type.equals("CITY")) {
                    addVictoryPoints(1); // +1 more (settlement already gave 1)
                }
                
                sendMessage("✓ Successfully built " + type + " at (" + row + "," + col + ")");
                return false;
            }
            
        } catch (NumberFormatException e) {
            sendMessage("Invalid number format");
        }
        
        return false;
    }
    
    /**
     * Handles 3:1 trade with bank.
     * Per official rules: Trade 3 of one resource for 1 of any other.
     */
    protected boolean handleTrade3(String[] parts) {
        String give = parts[1];
        String get = parts[2];
        
        ResourceType giveType = ResourceType.fromString(give);
        ResourceType getType = ResourceType.fromString(get);
        
        if (giveType == null || getType == null) {
            sendMessage("Invalid resource types");
            sendMessage("Valid types: Brick, Grain, Lumber, Wool, Ore, Gold");
            return false;
        }
        
        if (giveType == getType) {
            sendMessage("Cannot trade for the same resource type");
            return false;
        }
        
        int available = resourceBank.getResourceCount(giveType);
        if (available < 3) {
            sendMessage("Not enough " + giveType.getDisplayName() + " to trade (need 3, have " + available + ")");
            return false;
        }
        
        // Perform trade
        if (resourceBank.removeResources(giveType, 3)) {
            resourceBank.addResources(getType, 1);
            sendMessage("✓ Traded 3 " + giveType.getDisplayName() + " for 1 " + getType.getDisplayName());
        } else {
            sendMessage("✗ Trade failed");
        }
        
        return false;
    }
    
    /**
     * Formats a cost map as a string.
     */
    private String formatCost(Map<ResourceType, Integer> cost) {
        if (cost.isEmpty()) {
            return "Free";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(entry.getValue()).append(" ").append(entry.getKey().getDisplayName());
        }
        return sb.toString();
    }
    
    /**
     * Formats resources as a string.
     */
    private String formatResources(Map<ResourceType, Integer> resources) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ResourceType, Integer> entry : resources.entrySet()) {
            if (entry.getValue() > 0) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(entry.getValue()).append(" ").append(entry.getKey().getDisplayName());
            }
        }
        return sb.length() > 0 ? sb.toString() : "None";
    }
    
    // ========== Card Management ==========
    
    /**
     * Adds a card to hand.
     * 
     * @param card The card to add
     */
    public void addCardToHand(Card card) {
        if (card != null) {
            hand.add(card);
        }
    }
    
    /**
     * Removes a card from hand by index.
     * 
     * @param index The index
     * @return The removed card, or null
     */
    public Card removeCardFromHand(int index) {
        if (index >= 0 && index < hand.size()) {
            return hand.remove(index);
        }
        return null;
    }
    
    // ========== Getters and Setters ==========
    
    public Principality getPrincipality() { return principality; }
    public ResourceBank getResourceBank() { return resourceBank; }
    public List<Card> getHand() { return hand; }
    public Set<String> getFlags() { return flags; }
    
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
