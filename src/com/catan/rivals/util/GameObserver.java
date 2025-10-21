package com.catan.rivals.util;

/**
 * Observer interface for event notifications.
 * Part of Observer pattern for game event handling.
 * 
 * Design Pattern: Observer
 * SOLID: Dependency Inversion Principle - depend on abstraction
 */
public interface GameObserver {
    /**
     * Called when an observed event occurs.
     * 
     * @param event The event that occurred
     * @param data Additional event data
     */
    void onEvent(String event, Object data);
}
