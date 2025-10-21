package com.catan.rivals.game.phase;

import com.catan.rivals.player.Player;

/**
 * Strategy interface for game phase execution.
 * Each phase implements this interface with its specific logic.
 * 
 * Design Pattern: Strategy Pattern
 * SOLID: Single Responsibility - each implementation handles one phase
 * SOLID: Open-Closed - can add new phases without modifying existing code
 * SOLID: Dependency Inversion - GameEngine depends on abstraction
 */
public interface PhaseHandler {
    
    /**
     * Executes the phase for the active player.
     * 
     * @param activePlayer The player whose turn it is
     * @param opponent The opponent player
     * @return True if phase completed successfully, false if game should end
     */
    boolean execute(Player activePlayer, Player opponent);
    
    /**
     * Gets the name of this phase for logging/display.
     * 
     * @return The phase name
     */
    String getPhaseName();
    
    /**
     * Checks if this phase should be executed.
     * Some phases may be conditional.
     * 
     * @param activePlayer The active player
     * @param opponent The opponent
     * @return True if phase should execute
     */
    default boolean shouldExecute(Player activePlayer, Player opponent) {
        return true;
    }
}
