package com.catan.rivals.game.event;

import com.catan.rivals.player.Player;

/**
 * Strategy interface for handling game events.
 * Each event type has its own concrete handler.
 * 
 * Design Pattern: Strategy Pattern
 * SOLID: Single Responsibility - each handler does one event
 * SOLID: Open-Closed - new events can be added without modifying existing
 */
public interface GameEventHandler {
    
    /**
     * Handles the event for the given players.
     * 
     * @param activePlayer The active player
     * @param opponent The opponent
     */
    void handleEvent(Player activePlayer, Player opponent);
    
    /**
     * Gets the name of this event.
     * 
     * @return Event name
     */
    String getEventName();
}
