package com.catan.rivals.player.action;

import com.catan.rivals.player.Player;
import com.catan.rivals.model.*;
import com.catan.rivals.game.GameSetup;
import java.util.Map;

/**
 * Handles building center cards (Road/Settlement/City).
 */
public class BuildAction implements PlayerAction {
    
    @Override
    public boolean execute(Player player, Player opponent, Deck deck) {
        player.sendMessage("=== Build ===");
        player.sendMessage("Options: Road, Settlement, City");
        player.sendMessage("Choose type (or 'C' to cancel):");
        
        String typeInput = player.receiveInput();
        if (typeInput.trim().equalsIgnoreCase("C")) {
            return false;
        }
        
        String type = typeInput.trim().toUpperCase();
        
        if (!type.equals("ROAD") && !type.equals("SETTLEMENT") && !type.equals("CITY")) {
            player.sendMessage("Invalid type!");
            return false;
        }
        
        // Get cost
        Map<ResourceType, Integer> cost = GameSetup.getCenterCardCost(type);
        
        if (!player.getResourceBank().canAfford(cost)) {
            player.sendMessage("Cannot afford " + type);
            player.sendMessage("Cost: " + formatCost(cost));
            return false;
        }
        
        // Get placement
        player.sendMessage("Enter position (row col):");
        String input = player.receiveInput();
        String[] parts = input.trim().split("\\s+");
        
        if (parts.length != 2) {
            player.sendMessage("Invalid format!");
            return false;
        }
        
        try {
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            
            // Validate basic placement rules
            if (!validatePlacement(player, type, row, col)) {
                return false;
            }
            
            // Get card from deck
            Card cardToBuild = getCardFromDeck(deck, type);
            if (cardToBuild == null) {
                player.sendMessage("No " + type + " available!");
                return false;
            }
            
            cardToBuild.setCost(cost);
            
            // Pay cost
            if (!player.getResourceBank().payCost(cost)) {
                player.sendMessage("Failed to pay cost!");
                return false;
            }
            
            // CRITICAL FIX: Handle grid expansion before placing
            int finalCol = col;
            
            if (type.equals("SETTLEMENT")) {
                // Draw two regions for the settlement
                Card region1 = drawRegionFromDeck(deck);
                Card region2 = drawRegionFromDeck(deck);
                
                if (region1 == null || region2 == null) {
                    player.sendMessage("Warning: Not enough regions in deck!");
                    // Refund resources
                    for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
                        player.getResourceBank().addResources(entry.getKey(), entry.getValue());
                    }
                    return false;
                }
                
                // Expand grid and place regions
                finalCol = player.getPrincipality().expandForSettlement(row, col, region1, region2);
                player.sendMessage("Grid expanded! New regions placed.");
                
            } else if (type.equals("ROAD")) {
                // Expand for road if on edge
                finalCol = player.getPrincipality().expandForRoad(row, col);
            }
            
            // Place the card at adjusted position
            player.getPrincipality().placeCard(row, finalCol, cardToBuild);
            
            // Add VP
            if (type.equals("SETTLEMENT")) {
                player.addVictoryPoints(1);
            } else if (type.equals("CITY")) {
                player.addVictoryPoints(1);
            }
            
            player.sendMessage("✓ Built " + type + " at (" + row + "," + finalCol + ")");
            
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid numbers!");
        } catch (IllegalArgumentException e) {
            player.sendMessage("Error: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Validates basic placement rules.
     */
    private boolean validatePlacement(Player player, String type, int row, int col) {
        // Settlements and roads must be in center row (2)
        if ((type.equals("SETTLEMENT") || type.equals("ROAD")) && row != 2) {
            player.sendMessage(type + " must be built in center row!");
            return false;
        }
        
        if (type.equals("CITY")) {
            Card existing = player.getPrincipality().getCardAt(row, col);
            if (existing == null || !existing.getName().equalsIgnoreCase("Settlement")) {
                player.sendMessage("City must be built on Settlement!");
                return false;
            }
        } else {
            // For non-city builds, position should be empty
            if (!player.getPrincipality().isEmptyAt(row, col)) {
                player.sendMessage("Position not empty!");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Gets a center card from deck.
     */
    private Card getCardFromDeck(Deck deck, String type) {
        java.util.List<Card> sourceList = null;
        
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
        
        if (sourceList != null && !sourceList.isEmpty()) {
            return sourceList.remove(0);
        }
        
        return null;
    }
    
    /**
     * Draws a region card from deck and assigns dice value.
     */
    private Card drawRegionFromDeck(Deck deck) {
        if (deck.getRegions().isEmpty()) {
            return null;
        }
        
        Card region = deck.getRegions().remove(0);
        region.setCardType(CardType.REGION);
        
        // Region should already have dice value assigned from setup
        // If not, assign a default value
        if (region.getDiceRoll() == 0) {
            region.setDiceRoll(3); // Default to 3
        }
        
        // Regions start with 1 resource
        region.setStoredResources(1);
        
        return region;
    }
    
    /**
     * Formats cost for display.
     */
    private String formatCost(Map<ResourceType, Integer> cost) {
        if (cost.isEmpty()) return "Free";
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(entry.getValue()).append(" ").append(entry.getKey().getDisplayName());
        }
        return sb.toString();
    }
    
    @Override
    public String getCommandName() {
        return "BUILD";
    }
    
    @Override
    public String getUsageHelp() {
        return "BUILD - Build a Road, Settlement, or City";
    }
}