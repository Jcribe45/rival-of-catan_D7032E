package com.catan.rivals.game;

/**
 * Enumeration of game phases using State pattern.
 * Each phase represents a distinct state in the game flow.
 * 
 * Design Pattern: State Pattern
 * SOLID: Single Responsibility - represents game states only
 */
public enum GamePhase {
    
    /**
     * Game initialization phase.
     */
    SETUP("Setup"),
    
    /**
     * Dice rolling phase.
     */
    ROLL_DICE("Roll Dice"),
    
    /**
     * Production phase (resources generated).
     */
    PRODUCTION("Production"),
    
    /**
     * Event handling phase.
     */
    EVENT("Event"),
    
    /**
     * Action phase (play cards, build, trade).
     */
    ACTION("Action"),
    
    /**
     * Replenish hand phase.
     */
    REPLENISH("Replenish"),
    
    /**
     * Exchange phase (optional card exchange).
     */
    EXCHANGE("Exchange"),
    
    /**
     * Victory check phase.
     */
    VICTORY_CHECK("Victory Check"),
    
    /**
     * Game ended.
     */
    GAME_OVER("Game Over");
    
    private final String displayName;
    
    GamePhase(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the display name.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the next phase in the turn sequence.
     * 
     * @return The next phase
     */
    public GamePhase next() {
        switch (this) {
            case SETUP:
                return ROLL_DICE;
            case ROLL_DICE:
                return PRODUCTION;
            case PRODUCTION:
                return EVENT;
            case EVENT:
                return ACTION;
            case ACTION:
                return REPLENISH;
            case REPLENISH:
                return EXCHANGE;
            case EXCHANGE:
                return VICTORY_CHECK;
            case VICTORY_CHECK:
                return ROLL_DICE; // Next turn
            case GAME_OVER:
                return GAME_OVER; // Stay in game over
            default:
                return this;
        }
    }
    
    /**
     * Checks if this is an active play phase.
     * 
     * @return True if active phase
     */
    public boolean isActivePhase() {
        return this != SETUP && this != GAME_OVER;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
