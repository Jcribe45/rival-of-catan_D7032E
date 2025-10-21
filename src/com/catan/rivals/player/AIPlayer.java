package com.catan.rivals.player;

import com.catan.rivals.model.*;
import java.util.*;

/**
 * Simple AI player implementation.
 * 
 * Design Pattern: Strategy Pattern - AI decision making
 * SOLID: Liskov Substitution - can substitute Player
 * SOLID: Single Responsibility - AI decision logic only
 */
public class AIPlayer extends Player {
    
    private Random random;
    private String playerName;
    
    /**
     * Constructor.
     * 
     * @param playerName The AI's name
     */
    public AIPlayer(String playerName) {
        super();
        this.playerName = playerName;
        this.random = new Random();
    }
    
    /**
     * Default constructor.
     */
    public AIPlayer() {
        this("Bot");
    }
    
    @Override
    public void sendMessage(String message) {
        // AI logs messages but doesn't display them
        System.out.println("[" + playerName + " AI] " + message);
    }
    
    @Override
    public String receiveInput() {
        // AI doesn't receive interactive input
        return "";
    }
    
    @Override
    protected String promptForAction() {
        // AI makes automatic decisions
        try {
            Thread.sleep(500); // Simulate thinking
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return decideAction();
    }
    
    /**
     * AI decision making for actions.
     * Simple strategy: try to play cards or build, otherwise end turn.
     * 
     * @return The action command
     */
    private String decideAction() {
        // 70% chance to try playing a card if hand not empty
        if (!hand.isEmpty() && random.nextDouble() < 0.7) {
            return tryPlayCard();
        }
        
        // 20% chance to try building
        if (random.nextDouble() < 0.2) {
            return tryBuild();
        }
        
        // 10% chance to try trading
        if (random.nextDouble() < 0.1) {
            return tryTrade();
        }
        
        // Otherwise end turn
        return "END";
    }
    
    /**
     * Attempts to play a card from hand.
     * 
     * @return Play command or END
     */
    private String tryPlayCard() {
        if (hand.isEmpty()) {
            return "END";
        }
        
        // Find an affordable card
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            
            if (resourceBank.canAfford(card.getCost())) {
                // Find a valid position
                int[] pos = findValidPosition(card);
                if (pos != null) {
                    return String.format("PLAY %d %d %d", i, pos[0], pos[1]);
                }
            }
        }
        
        return "END";
    }
    
    /**
     * Attempts to build a center card.
     * 
     * @return Build command or END
     */
    private String tryBuild() {
        // Simple strategy: prefer Settlement > Road > City
        String[] priorities = {"SETTLEMENT", "ROAD", "CITY"};
        
        for (String type : priorities) {
            int[] pos = findValidBuildPosition(type);
            if (pos != null) {
                return String.format("BUILD %s %d %d", type, pos[0], pos[1]);
            }
        }
        
        return "END";
    }
    
    /**
     * Attempts to trade with bank.
     * 
     * @return Trade command or END
     */
    private String tryTrade() {
        // Find resource with count >= 3
        Map<ResourceType, Integer> resources = resourceBank.getResourceSummary();
        
        for (Map.Entry<ResourceType, Integer> entry : resources.entrySet()) {
            if (entry.getValue() >= 3) {
                // Trade for a resource we have less of
                ResourceType target = findScarcestResource(resources, entry.getKey());
                if (target != null) {
                    return String.format("TRADE3 %s %s", 
                        entry.getKey().getDisplayName(), 
                        target.getDisplayName());
                }
            }
        }
        
        return "END";
    }
    
    /**
     * Finds a valid position for placing a card.
     * 
     * @param card The card to place
     * @return [row, col] or null if no valid position
     */
    private int[] findValidPosition(Card card) {
        int rows = principality.getRowCount();
        int cols = principality.getColumnCount();
        
        // Try random positions
        for (int attempt = 0; attempt < 20; attempt++) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            
            if (principality.isEmptyAt(row, col)) {
                // Simple validation - actual validation happens in card.applyEffect
                return new int[]{row, col};
            }
        }
        
        // Try systematic search
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (principality.isEmptyAt(r, c)) {
                    return new int[]{r, c};
                }
            }
        }
        
        return null;
    }
    
    /**
     * Finds a valid position for building a center card.
     * 
     * @param type The build type (ROAD/SETTLEMENT/CITY)
     * @return [row, col] or null
     */
    private int[] findValidBuildPosition(String type) {
        int centerRow = 2; // Center row in principality
        int cols = principality.getColumnCount();
        
        // Try positions in center row
        for (int c = 0; c < cols; c++) {
            if (principality.isEmptyAt(centerRow, c)) {
                return new int[]{centerRow, c};
            }
        }
        
        return null;
    }
    
    /**
     * Finds the scarcest resource (excluding one type).
     * 
     * @param resources Resource counts
     * @param exclude Type to exclude
     * @return The scarcest type
     */
    private ResourceType findScarcestResource(Map<ResourceType, Integer> resources, ResourceType exclude) {
        ResourceType scarcest = null;
        int minCount = Integer.MAX_VALUE;
        
        for (Map.Entry<ResourceType, Integer> entry : resources.entrySet()) {
            if (entry.getKey() != exclude && entry.getValue() < minCount) {
                minCount = entry.getValue();
                scarcest = entry.getKey();
            }
        }
        
        return scarcest;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    @Override
    public String toString() {
        return playerName + " (AI)";
    }
}
