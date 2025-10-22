package com.catan.rivals.game.event;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;
import java.util.List;

/**
 * Handles the Fraternal Feuds event.
 * Player with strength advantage chooses 2 cards from opponent's hand to discard.
 */
public class FraternalFeudsEventHandler implements GameEventHandler {
    
    @Override
    public void handleEvent(Player activePlayer, Player opponent) {
        // Check strength advantage
        if (activePlayer.getStrengthPoints() <= opponent.getStrengthPoints()) {
            activePlayer.sendMessage("No strength advantage - Fraternal Feuds has no effect.");
            return;
        }
        
        activePlayer.sendMessage("Fraternal Feuds! You have strength advantage.");
        activePlayer.sendMessage("Choose 2 cards from opponent's hand to discard.");
        
        // Show opponent's hand to active player
        activePlayer.sendMessage("=== Opponent's Hand ===");
        List<Card> oppHand = opponent.getHand();
        
        if (oppHand.isEmpty()) {
            activePlayer.sendMessage("Opponent has no cards!");
            return;
        }
        
        for (int i = 0; i < oppHand.size(); i++) {
            activePlayer.sendMessage(String.format("[%d] %s", i, oppHand.get(i).getName()));
        }
        
        // Choose up to 2 cards (or all if less than 2)
        int toDiscard = Math.min(2, oppHand.size());
        
        for (int i = 0; i < toDiscard; i++) {
            activePlayer.sendMessage("Choose card to discard (" + (i + 1) + "/" + toDiscard + "):");
            
            // Use existing index choosing logic
            int index = getCardIndex(activePlayer, oppHand.size());
            
            if (index >= 0 && index < oppHand.size()) {
                Card discarded = oppHand.remove(index);
                activePlayer.sendMessage("Discarded: " + discarded.getName());
                opponent.sendMessage("Your " + discarded.getName() + " was discarded!");
            }
        }
    }
    
    @Override
    public String getEventName() {
        return "Fraternal Feuds";
    }
    
    /**
     * Gets a valid card index from player input.
     */
    private int getCardIndex(Player player, int maxIndex) {
        String input = player.receiveInput();
        try {
            int index = Integer.parseInt(input.trim());
            if (index >= 0 && index < maxIndex) {
                return index;
            }
        } catch (NumberFormatException e) {
            // Invalid input
        }
        player.sendMessage("Invalid index!");
        return -1;
    }
}