package com.catan.rivals.player;

import com.catan.rivals.model.*;
import com.catan.rivals.game.GameSetup;
import com.catan.rivals.util.EffectTracker;
import com.catan.rivals.util.PrincipalityRenderer;
import com.catan.rivals.util.TradingHelper;

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
    protected EffectTracker effectTracker;
    
    /**
     * Constructor initializes player state.
     */
    public Player() {
        this.principality = new Principality();
        this.resourceBank = new ResourceBank(principality);
        this.hand = new ArrayList<>();
        this.flags = new HashSet<>();
        this.effectTracker = new EffectTracker();
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
        sendMessage("=== Your Principality ===");
        sendMessage(PrincipalityRenderer.renderPrincipality(this));
    }
    
    /**
     * Displays the player's hand with detailed information.
     * Default implementation, can be overridden.
     */
    protected void displayHand() {
        sendMessage("=== Your Hand (" + hand.size() + " cards) ===");
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            sendMessage(String.format("[%d] %s (Cost: %s, Type: %s)", 
                i, 
                card.getName(), 
                card.getCostString(),
                card.getCardType().getDisplayName()));
        }
        sendMessage("");
    }

    /**
     * Displays the player's resources with active effects.
     */
    protected void displayResources() {
        sendMessage("\n=== Your Resources ===");
        Map<ResourceType, Integer> resources = resourceBank.getResourceSummary();
        
        for (ResourceType type : ResourceType.values()) {
            int count = resources.getOrDefault(type, 0);
            sendMessage("  " + type.getDisplayName() + ": " + count);
        }
        
        sendMessage("Total: " + resourceBank.getTotalResources() + " resources");
        
        // Show active effects
        String effects = effectTracker.getEffectsSummary();
        if (!effects.isEmpty()) {
            sendMessage(effects);
        }
    }

    /**
     * Prompts for and returns the player's action choice.
     */
    protected String promptForAction() {
        sendMessage("Choose action:");
        sendMessage("  PLAY <index> <row> <col> - Play card from hand");
        sendMessage("  BUILD <Road|Settlement|City> <row> <col> - Build center card");
        sendMessage("  TRADE <give> <get> - Trade with bank (checks for 2:1 abilities)");
        sendMessage("  VIEW - View boards again");
        sendMessage("  END - End action phase");
        sendMessage("");
        sendMessage("Your choice:");
        
        return receiveInput();
    }

    /**
     * Executes an action command.
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
                
            case "TRADE":
                if (parts.length >= 3) {
                    return handleTrade(parts);
                }
                sendMessage("Usage: TRADE <give> <get>");
                break;
                
            case "VIEW":
                displayBothBoards(opponent);
                displayHand();
                break;
                
            default:
                sendMessage("Unknown command: " + command);
        }
        
        return false;
    }

    
    // ========== Action Handlers ==========
    
    /**
     * Handles playing a card from hand.
     */
    protected boolean handlePlayCard(String[] parts, Player opponent) {
        try {
            int index = Integer.parseInt(parts[1]);
            int row = Integer.parseInt(parts[2]);
            int col = Integer.parseInt(parts[3]);
            
            if (index < 0 || index >= hand.size()) {
                sendMessage("Invalid card index");
                return false;
            }
            
            Card card = hand.get(index);
            
            if (!resourceBank.canAfford(card.getCost())) {
                sendMessage("Cannot afford card cost: " + card.getCostString());
                return false;
            }
            
            if (!card.canApplyEffect(this, opponent, row, col)) {
                sendMessage("Cannot play card at that position");
                return false;
            }
            
            if (resourceBank.payCost(card.getCost())) {
                if (card.applyEffect(this, opponent, row, col)) {
                    hand.remove(index);
                    
                    // Register card effects
                    Principality.CardPosition pos = new Principality.CardPosition(card, row, col);
                    effectTracker.registerCardEffects(card, pos);
                    
                    sendMessage("Successfully played " + card.getName());
                    
                    // Show if card has special abilities
                    if (effectTracker.hasTwoForOneTrading(ResourceType.GOLD) && 
                        card.getName().contains("Gold")) {
                        sendMessage("  Effect: You can now trade 2 Gold for 1 other resource!");
                    }
                    
                    return false;
                }
            }
            
            sendMessage("Failed to play card");
            
        } catch (NumberFormatException e) {
            sendMessage("Invalid number format");
        }
        
        return false;
    }

    /**
     * Handles trading with bank.
     * Now uses TradingHelper to check for 2:1 abilities.
     */
    protected boolean handleTrade(String[] parts) {
        String give = parts[1];
        String get = parts[2];
        
        ResourceType giveType = ResourceType.fromString(give);
        ResourceType getType = ResourceType.fromString(get);
        
        if (giveType == null || getType == null) {
            sendMessage("Invalid resource types");
            sendMessage("Available: Brick, Grain, Lumber, Wool, Ore, Gold");
            return false;
        }
        
        TradingHelper.executeTrade(this, giveType, getType);
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

    /**
     * Prompts player to choose a card from their hand.
     * Reusable across phases and events.
     * 
     * @param prompt The prompt message
     * @param allowCancel Whether to allow cancellation
     * @return The card index, or -1 if cancelled
     */
    public int chooseCardFromHand(String prompt, boolean allowCancel) {
        if (hand.isEmpty()) {
            sendMessage("Your hand is empty!");
            return -1;
        }
        
        sendMessage("=== Your Hand ===");
        for (int i = 0; i < hand.size(); i++) {
            sendMessage(String.format("[%d] %s", i, hand.get(i).getName()));
        }
        
        String cancelText = allowCancel ? " or 'C' to cancel" : "";
        sendMessage("" + prompt + " (0-" + (hand.size() - 1) + ")" + cancelText + ":");
        
        String input = receiveInput();
        
        if (allowCancel && input.trim().toUpperCase().equals("C")) {
            return -1;
        }
        
        try {
            int index = Integer.parseInt(input.trim());
            if (index >= 0 && index < hand.size()) {
                return index;
            } else {
                sendMessage("Invalid card index!");
                return -1;
            }
        } catch (NumberFormatException e) {
            sendMessage("Invalid input!");
            return -1;
        }
    }

    /**
     * Prompts player to choose multiple cards from hand.
     * Used for events like Religious Dispute.
     * 
     * @param count Number of cards to choose
     * @param prompt The prompt message
     * @return List of card indices, or empty list if cancelled
     */
    public List<Integer> chooseMultipleCardsFromHand(int count, String prompt) {
        List<Integer> chosen = new ArrayList<>();
        
        if (hand.size() < count) {
            sendMessage("Not enough cards in hand!");
            return chosen;
        }
        
        for (int i = 0; i < count; i++) {
            sendMessage("" + prompt + " (" + (i + 1) + "/" + count + ")");
            int index = chooseCardFromHand("Choose card", false);
            
            if (index < 0 || chosen.contains(index)) {
                sendMessage("Invalid choice!");
                return new ArrayList<>(); // Return empty on failure
            }
            
            chosen.add(index);
        }
        
        return chosen;
    }

    /**
     * Prompts player to choose a draw stack.
     * 
     * @param deck The game deck
     * @param prompt The prompt message
     * @param allowCancel Whether to allow cancellation
     * @return Stack number (1-4), or -1 if cancelled/invalid
     */
    public int chooseDrawStack(Deck deck, String prompt, boolean allowCancel) {
        String cancelText = allowCancel ? " or 'C' to cancel" : "";
        sendMessage("" + prompt + " (1-4)" + cancelText + ":");
        
        String input = receiveInput();
        
        if (allowCancel && input.trim().toUpperCase().equals("C")) {
            return -1;
        }
        
        try {
            int stackNum = Integer.parseInt(input.trim());
            if (stackNum >= 1 && stackNum <= 4) {
                // Check if stack is empty
                List<Card> stack = deck.getDrawStack(stackNum);
                if (stack == null || stack.isEmpty()) {
                    sendMessage("Stack " + stackNum + " is empty!");
                    return -1;
                }
                return stackNum;
            } else {
                sendMessage("Invalid stack number!");
                return -1;
            }
        } catch (NumberFormatException e) {
            sendMessage("Invalid input!");
            return -1;
        }
    }

    /**
     * Shows all cards in a stack and lets player choose one.
     * Used for paid exchanges and special card effects.
     * 
     * @param stack The draw stack to choose from
     * @param prompt The prompt message
     * @param allowCancel Whether to allow cancellation
     * @return The chosen card (removed from stack), or null if cancelled
     */
    public Card chooseCardFromStack(List<Card> stack, String prompt, boolean allowCancel) {
        if (stack == null || stack.isEmpty()) {
            sendMessage("Stack is empty!");
            return null;
        }
        
        sendMessage("=== Cards in Stack ===");
        for (int i = 0; i < stack.size(); i++) {
            sendMessage(String.format("[%d] %s - %s", 
                i, 
                stack.get(i).getName(),
                stack.get(i).getCardType().getDisplayName()));
        }
        
        String cancelText = allowCancel ? " or 'C' to cancel" : "";
        sendMessage("" + prompt + " (0-" + (stack.size() - 1) + ")" + cancelText + ":");
        
        String input = receiveInput();
        
        if (allowCancel && input.trim().toUpperCase().equals("C")) {
            return null;
        }
        
        try {
            int index = Integer.parseInt(input.trim());
            if (index >= 0 && index < stack.size()) {
                return stack.remove(index);
            } else {
                sendMessage("Invalid index!");
                return null;
            }
        } catch (NumberFormatException e) {
            sendMessage("Invalid input!");
            return null;
        }
    }

    /**
     * Prompts player to choose a resource type.
     * 
     * @param prompt The prompt message
     * @return The chosen ResourceType, or null if invalid
     */
    public ResourceType chooseResourceType(String prompt) {
        sendMessage("" + prompt);
        sendMessage("Options: Brick, Grain, Lumber, Wool, Ore, Gold");
        sendMessage("Your choice:");
        
        String input = receiveInput();
        ResourceType type = ResourceType.fromString(input);
        
        if (type == null) {
            sendMessage("Invalid resource type!");
        }
        
        return type;
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

    public EffectTracker getEffectTracker() { return effectTracker; }
}
