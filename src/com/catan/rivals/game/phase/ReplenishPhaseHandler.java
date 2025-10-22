package com.catan.rivals.game.phase;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;

/**
 * Handles the replenish phase where players draw cards to reach hand limit.
 * 
 * REFACTORED: Uses Player.chooseDrawStack() for consistency.
 */
public class ReplenishPhaseHandler implements PhaseHandler {
    
    private Deck deck;
    
    public ReplenishPhaseHandler(Deck deck) {
        this.deck = deck;
    }
    
    @Override
    public boolean execute(Player activePlayer, Player opponent) {
        int handLimit = 3 + activePlayer.getProgressPoints();
        
        while (activePlayer.getHand().size() < handLimit) {
            int stackNum = activePlayer.chooseDrawStack(deck, "Draw from stack", false);
            
            if (stackNum < 0) {
                // Try to find a non-empty stack automatically
                stackNum = findNonEmptyStack();
                if (stackNum < 0) {
                    activePlayer.sendMessage("All stacks empty!");
                    break;
                }
            }
            
            Card card = deck.drawFromStack(stackNum);
            if (card != null) {
                activePlayer.addCardToHand(card);
                activePlayer.sendMessage("Drew: " + card.getName());
            }
        }
        
        activePlayer.sendMessage("Hand replenished to " + 
                                activePlayer.getHand().size() + " cards.");
        return true;
    }
    
    @Override
    public String getPhaseName() {
        return "Replenish";
    }
    
    /**
     * Finds the first non-empty draw stack.
     * 
     * @return Stack number (1-4), or -1 if all empty
     */
    private int findNonEmptyStack() {
        for (int i = 1; i <= 4; i++) {
            if (!deck.isStackEmpty(i)) {
                return i;
            }
        }
        return -1;
    }
}