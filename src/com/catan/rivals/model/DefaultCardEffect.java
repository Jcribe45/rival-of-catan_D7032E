package com.catan.rivals.model;

import com.catan.rivals.player.Player;

/**
 * Default implementation of CardEffect for cards without special behavior.
 * 
 * Design Pattern: Strategy Pattern (concrete strategy)
 * SOLID: Liskov Substitution - can be used anywhere CardEffect is expected
 */
public class DefaultCardEffect implements CardEffect {
    
    @Override
    public boolean apply(Card card, Player activePlayer, Player opponent, int row, int col) {
        // Default behavior: just place the card if placement is required
        if (card.getCardType().requiresPlacement()) {
            if (row >= 0 && col >= 0) {
                activePlayer.getPrincipality().placeCard(row, col, card);
                
                // Add points from card
                activePlayer.addVictoryPoints(card.getVictoryPoints());
                activePlayer.addCommercePoints(card.getCommercePoints());
                activePlayer.addSkillPoints(card.getSkillPoints());
                activePlayer.addStrengthPoints(card.getStrengthPoints());
                activePlayer.addProgressPoints(card.getProgressPoints());
                
                return true;
            }
            return false;
        }
        
        // Action cards: default is +1 VP
        activePlayer.addVictoryPoints(1);
        return true;
    }
    
    @Override
    public boolean canApply(Card card, Player activePlayer, Player opponent, int row, int col) {
        if (card.getCardType().requiresPlacement()) {
            // Check if space is empty
            return activePlayer.getPrincipality().isEmptyAt(row, col);
        }
        return true;
    }
    
    @Override
    public String getDescription() {
        return "Default card effect";
    }
}
