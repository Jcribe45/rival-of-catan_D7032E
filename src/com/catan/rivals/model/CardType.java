package com.catan.rivals.model;

/**
 * Enumeration of all card types in the game.
 * 
 * SOLID: Single Responsibility - represents card classifications
 * Booch: Completeness - all card types are represented
 */
public enum CardType {
    REGION("Region"),
    CENTER_CARD("Center Card"),
    BUILDING("Building"),
    UNIT("Unit"),
    HERO("Hero"),
    SHIP("Ship"),
    ACTION("Action"),
    ACTION_ATTACK("Action - Attack"),
    ACTION_NEUTRAL("Action - Neutral"),
    EVENT("Event"),
    UNKNOWN("Unknown");
    
    private final String displayName;
    
    CardType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Checks if this is an action card type.
     * 
     * @return True if action card
     */
    public boolean isAction() {
        return this == ACTION || this == ACTION_ATTACK || this == ACTION_NEUTRAL;
    }
    
    /**
     * Checks if this is a unit type (includes heroes and ships).
     * 
     * @return True if unit type
     */
    public boolean isUnit() {
        return this == UNIT || this == HERO || this == SHIP;
    }
    
    /**
     * Checks if this requires placement in principality.
     * 
     * @return True if requires placement
     */
    public boolean requiresPlacement() {
        return this == REGION || this == CENTER_CARD || this == BUILDING || 
               this == UNIT || this == HERO || this == SHIP;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
