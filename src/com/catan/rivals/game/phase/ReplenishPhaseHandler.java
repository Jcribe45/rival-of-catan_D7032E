package com.catan.rivals.game.phase;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;

/**
 * Handles the replenish phase where players draw cards to reach hand limit.
 * 
 * Design Pattern: Strategy Pattern (concrete strategy)
 * SOLID: Single Responsibility - only handles hand replenishment
 */
public class ReplenishPhaseHandler implements PhaseHandler {
    
    private Deck deck;
    
    /**
     * Constructor.
     * 
     * @param deck The game deck
     */
    public ReplenishPhaseHandler(Deck deck) {
        this.deck = deck;
    }
    
    @Override
    public boolean execute(Player activePlayer, Player opponent) {
        int handLimit = 3 + activePlayer.getProgressPoints();
        
        while (activePlayer.getHand().size() < handLimit) {
            activePlayer.sendMessage("Draw from stack (1-4):");
            String input = activePlayer.receiveInput();
            
            try {
                int stackNum = Integer.parseInt(input.trim());
                Card card = deck.drawFromStack(stackNum);
                
                if (card != null) {
                    activePlayer.addCardToHand(card);
                    activePlayer.sendMessage("Drew: " + card.getName());
                } else {
                    activePlayer.sendMessage("Stack empty, trying next...");
                    // Try other stacks
                    for (int i = 1; i <= 4; i++) {
                        card = deck.drawFromStack(i);
                        if (card != null) {
                            activePlayer.addCardToHand(card);
                            activePlayer.sendMessage("Drew: " + card.getName());
                            break;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                activePlayer.sendMessage("Invalid input");
            }
        }
        
        activePlayer.sendMessage("Hand replenished to " + activePlayer.getHand().size() + " cards.");
        return true;
    }
    
    @Override
    public String getPhaseName() {
        return "Replenish";
    }
}
