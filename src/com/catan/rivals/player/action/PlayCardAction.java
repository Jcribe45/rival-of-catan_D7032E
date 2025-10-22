package com.catan.rivals.player.action;

import com.catan.rivals.player.Player;
import com.catan.rivals.model.*;

/**
 * Handles playing a card from hand.
 * Extracted from Player class for better separation of concerns.
 */
public class PlayCardAction implements PlayerAction {
    
    @Override
    public boolean execute(Player player, Player opponent, Deck deck) {
        player.sendMessage("=== Play Card ===");
        
        // Choose card from hand
        int cardIndex = player.chooseCardFromHand("Choose card to play", true);
        if (cardIndex < 0) {
            return false;
        }
        
        Card card = player.getHand().get(cardIndex);
        
        // Check affordability
        if (!player.getResourceBank().canAfford(card.getCost())) {
            player.sendMessage("Cannot afford: " + card.getCostString());
            return false;
        }
        
        // Get placement
        player.sendMessage("Enter position (row col):");
        String input = player.receiveInput();
        String[] parts = input.trim().split("\\s+");
        
        if (parts.length != 2) {
            player.sendMessage("Invalid format! Use: row col");
            return false;
        }
        
        try {
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            
            // Validate and apply
            if (!card.canApplyEffect(player, opponent, row, col)) {
                player.sendMessage("Cannot play card at that position");
                return false;
            }
            
            // Pay cost and apply effect
            if (player.getResourceBank().payCost(card.getCost())) {
                if (card.applyEffect(player, opponent, row, col)) {
                    player.removeCardFromHand(cardIndex);
                    
                    // Register effects
                    Principality.CardPosition pos = 
                        new Principality.CardPosition(card, row, col);
                    player.getEffectTracker().registerCardEffects(card, pos);
                    
                    player.sendMessage("✓ Played: " + card.getName());
                    return false; // Continue action phase
                }
            }
            
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid numbers!");
        }
        
        return false;
    }
    
    @Override
    public String getCommandName() {
        return "PLAY";
    }
    
    @Override
    public String getUsageHelp() {
        return "PLAY - Play a card from your hand";
    }
}