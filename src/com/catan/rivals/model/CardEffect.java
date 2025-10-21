package com.catan.rivals.model;

import com.catan.rivals.player.Player;

/**
 * Strategy interface for card effects.
 * Different cards have different effects when played.
 * 
 * Design Pattern: Strategy Pattern
 * SOLID: Open-Closed Principle - new effects can be added without modifying existing code
 * SOLID: Interface Segregation - focused interface for card effects
 */
public interface CardEffect {
    
    /**
     * Applies the card's effect.
     * 
     * @param card The card being played
     * @param activePlayer The player playing the card
     * @param opponent The opponent player
     * @param row The row for placement (if applicable, -1 for actions)
     * @param col The column for placement (if applicable, -1 for actions)
     * @return True if effect was successfully applied, false otherwise
     */
    boolean apply(Card card, Player activePlayer, Player opponent, int row, int col);
    
    /**
     * Validates whether the effect can be applied.
     * 
     * @param card The card to validate
     * @param activePlayer The player attempting to play
     * @param opponent The opponent
     * @param row The target row
     * @param col The target column
     * @return True if the effect can be applied
     */
    boolean canApply(Card card, Player activePlayer, Player opponent, int row, int col);
    
    /**
     * Gets a description of what this effect does.
     * 
     * @return A human-readable description
     */
    String getDescription();
}
