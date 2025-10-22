package com.catan.rivals.player.action;

import com.catan.rivals.player.Player;
import com.catan.rivals.model.*;
import com.catan.rivals.game.GameSetup;
import java.util.Map;

/**
 * Handles building center cards (Road/Settlement/City).
 * Extracted from Player class.
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
            
            // Validate placement
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
            
            // Pay and build
            if (player.getResourceBank().payCost(cost)) {
                player.getPrincipality().placeCard(row, col, cardToBuild);
                
                // Add VP
                if (type.equals("SETTLEMENT")) {
                    player.addVictoryPoints(1);
                } else if (type.equals("CITY")) {
                    player.addVictoryPoints(1);
                }
                
                player.sendMessage("✓ Built " + type + " at (" + row + "," + col + ")");
            }
            
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid numbers!");
        }
        
        return false;
    }
    
    private boolean validatePlacement(Player player, String type, int row, int col) {
        if (!player.getPrincipality().isEmptyAt(row, col)) {
            player.sendMessage("Position not empty!");
            return false;
        }
        
        if (type.equals("CITY")) {
            Card existing = player.getPrincipality().getCardAt(row, col);
            if (existing == null || !existing.getName().equalsIgnoreCase("Settlement")) {
                player.sendMessage("City must be built on Settlement!");
                return false;
            }
        }
        
        return true;
    }
    
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