package com.catan.rivals.game.phase;

import com.catan.rivals.model.VictoryCondition;
import com.catan.rivals.player.Player;

/**
 * Handles victory condition checking.
 * 
 * Design Pattern: Strategy Pattern (concrete strategy)
 * SOLID: Single Responsibility - only checks victory conditions
 */
public class VictoryCheckHandler implements PhaseHandler {
    
    private VictoryCondition victoryCondition;
    
    /**
     * Constructor.
     * 
     * @param victoryCondition The victory condition to check
     */
    public VictoryCheckHandler(VictoryCondition victoryCondition) {
        this.victoryCondition = victoryCondition;
    }
    
    @Override
    public boolean execute(Player activePlayer, Player opponent) {
        return !checkVictory(activePlayer, opponent);
    }
    
    @Override
    public String getPhaseName() {
        return "Victory Check";
    }
    
    /**
     * Checks if a player has won.
     * 
     * @param activePlayer The active player
     * @param opponent The opponent
     * @return True if someone has won
     */
    public boolean checkVictory(Player activePlayer, Player opponent) {
        if (victoryCondition.hasWon(activePlayer, opponent)) {
            return true;
        }
        if (victoryCondition.hasWon(opponent, activePlayer)) {
            return true;
        }
        return false;
    }
    
    /**
     * Gets a victory summary for a player.
     * 
     * @param player The player
     * @param opponent The opponent
     * @return Victory points summary
     */
    public String getVictorySummary(Player player, Player opponent) {
        return victoryCondition.getVictoryPointsSummary(player, opponent);
    }
}
