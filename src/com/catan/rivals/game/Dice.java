package com.catan.rivals.game;

import java.util.Random;

/**
 * Handles dice rolling for the game.
 * Two dice types: Production (1-6) and Event (1-6).
 * 
 * SOLID: Single Responsibility - handles dice rolling only
 * Booch: High cohesion - all dice logic in one place
 */
public class Dice {
    
    private Random random;
    
    // Event die face meanings
    public static final int EVENT_BRIGAND = 1;
    public static final int EVENT_TRADE = 2;
    public static final int EVENT_CELEBRATION = 3;
    public static final int EVENT_PLENTIFUL_HARVEST = 4;
    public static final int EVENT_CARD_A = 5;
    public static final int EVENT_CARD_B = 6;
    
    /**
     * Constructor.
     */
    public Dice() {
        this.random = new Random();
    }
    
    /**
     * Constructor with seed for testing.
     * 
     * @param seed Random seed
     */
    public Dice(long seed) {
        this.random = new Random(seed);
    }
    
    /**
     * Rolls the production die (determines which regions produce).
     * 
     * @return A value from 1 to 6
     */
    public int rollProduction() {
        return 1 + random.nextInt(6);
    }
    
    /**
     * Rolls the event die (determines which event occurs).
     * 
     * @return A value from 1 to 6
     */
    public int rollEvent() {
        return 1 + random.nextInt(6);
    }
    
    /**
     * Rolls both dice and returns results.
     * 
     * @return [production, event]
     */
    public int[] rollBoth() {
        return new int[]{rollProduction(), rollEvent()};
    }
    
    /**
     * Gets a description of an event die result.
     * 
     * @param eventFace The event die face (1-6)
     * @return Description string
     */
    public static String getEventDescription(int eventFace) {
        switch (eventFace) {
            case EVENT_BRIGAND:
                return "Brigand Attack";
            case EVENT_TRADE:
                return "Trade";
            case EVENT_CELEBRATION:
                return "Celebration";
            case EVENT_PLENTIFUL_HARVEST:
                return "Plentiful Harvest";
            case EVENT_CARD_A:
            case EVENT_CARD_B:
                return "Event Card";
            default:
                return "Unknown Event";
        }
    }
    
    /**
     * Checks if event face draws an event card.
     * 
     * @param eventFace The event die face
     * @return True if should draw event card
     */
    public static boolean isEventCard(int eventFace) {
        return eventFace == EVENT_CARD_A || eventFace == EVENT_CARD_B;
    }
}
