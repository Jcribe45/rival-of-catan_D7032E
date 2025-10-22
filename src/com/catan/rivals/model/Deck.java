package com.catan.rivals.model;

import java.util.*;

/**
 * Represents a deck of cards with draw stacks.
 */
public class Deck {
    
    private List<Card> roads;
    private List<Card> settlements;
    private List<Card> cities;
    private List<Card> regions;
    private List<Card> events;
    private List<Card> drawStack1;
    private List<Card> drawStack2;
    private List<Card> drawStack3;
    private List<Card> drawStack4;
    
    /**
     * Constructor initializes all lists.
     */
    public Deck() {
        this.roads = new ArrayList<>();
        this.settlements = new ArrayList<>();
        this.cities = new ArrayList<>();
        this.regions = new ArrayList<>();
        this.events = new ArrayList<>();
        this.drawStack1 = new ArrayList<>();
        this.drawStack2 = new ArrayList<>();
        this.drawStack3 = new ArrayList<>();
        this.drawStack4 = new ArrayList<>();
    }
    
    /**
     * Adds a card to the appropriate pile based on its type and name.
     * 
     * @param card The card to add
     */
    public void addCard(Card card) {
        String name = card.getName();
        CardType type = card.getCardType();
        String placement = card.getPlacement();
        
        // Center cards
        if ("Road".equalsIgnoreCase(name)) {
            roads.add(card);
        } else if ("Settlement".equalsIgnoreCase(name)) {
            settlements.add(card);
        } else if ("City".equalsIgnoreCase(name)) {
            cities.add(card);
        }
        // Regions
        else if (type == CardType.REGION) {
            regions.add(card);
        }
        // Events
        else if (type == CardType.EVENT || "Event".equalsIgnoreCase(placement)) {
            events.add(card);
        }
        // Draw stacks (all other cards)
        else {
            // Distribute to draw stacks (will be shuffled later)
            int total = drawStack1.size() + drawStack2.size() +
                       drawStack3.size() + drawStack4.size();
            int stackNum = total % 4;
            
            switch (stackNum) {
                case 0: drawStack1.add(card); break;
                case 1: drawStack2.add(card); break;
                case 2: drawStack3.add(card); break;
                case 3: drawStack4.add(card); break;
            }
        }
    }
    
    /**
     * Shuffles all draw stacks.
     */
    public void shuffleDrawStacks() {
        Collections.shuffle(drawStack1);
        Collections.shuffle(drawStack2);
        Collections.shuffle(drawStack3);
        Collections.shuffle(drawStack4);
    }
    
    /**
     * Shuffles the event deck and places Yule card 4th from bottom.
     */
    public void shuffleEvents() {
        // Find Yule card
        Card yule = null;
        for (int i = 0; i < events.size(); i++) {
            if ("Yule".equalsIgnoreCase(events.get(i).getName())) {
                yule = events.remove(i);
                break;
            }
        }
        
        Collections.shuffle(events);
        
        // Place Yule 4th from bottom (per rules)
        if (yule != null && events.size() >= 3) {
            events.add(Math.max(0, events.size() - 3), yule);
        } else if (yule != null) {
            events.add(yule);
        }
    }
    
    /**
     * Shuffles the region deck.
     */
    public void shuffleRegions() {
        Collections.shuffle(regions);
    }
    
    /**
     * Draws a card from a specified draw stack.
     * 
     * @param stackNumber The stack number (1-4)
     * @return The drawn card, or null if stack is empty
     */
    public Card drawFromStack(int stackNumber) {
        List<Card> stack = getDrawStack(stackNumber);
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return stack.remove(0);
    }
    
    /**
     * Gets a draw stack by number.
     * 
     * @param stackNumber The stack number (1-4)
     * @return The stack, or null if invalid number
     */
    public List<Card> getDrawStack(int stackNumber) {
        switch (stackNumber) {
            case 1: return drawStack1;
            case 2: return drawStack2;
            case 3: return drawStack3;
            case 4: return drawStack4;
            default: return null;
        }
    }
    
    /**
     * Removes a card by name from a specific list.
     * 
     * @param list The list to search
     * @param cardName The card name
     * @return The removed card, or null if not found
     */
    public Card removeCardByName(List<Card> list, String cardName) {
        if (list == null || cardName == null) {
            return null;
        }
        
        for (int i = 0; i < list.size(); i++) {
            Card card = list.get(i);
            if (card.getName().equalsIgnoreCase(cardName)) {
                return list.remove(i);
            }
        }
        
        return null;
    }
    
    /**
     * Returns a card to the bottom of a draw stack.
     * Used for exchanges and card cycling.
     * 
     * @param card The card to return
     * @param stackNum The stack number (1-4)
     * @return True if successfully returned
     */
    public boolean returnCardToStackBottom(Card card, int stackNum) {
        List<Card> stack = getDrawStack(stackNum);
        if (stack != null && card != null) {
            stack.add(card); // Add to bottom (end of list)
            return true;
        }
        return false;
    }

    /**
     * Returns a card to the top of a draw stack.
     * Used for refunds or immediate redraw.
     * 
     * @param card The card to return
     * @param stackNum The stack number (1-4)
     * @return True if successfully returned
     */
    public boolean returnCardToStackTop(Card card, int stackNum) {
        List<Card> stack = getDrawStack(stackNum);
        if (stack != null && card != null) {
            stack.add(0, card); // Add to top (start of list)
            return true;
        }
        return false;
    }

    /**
     * Peeks at the top card of a stack without removing it.
     * 
     * @param stackNum The stack number (1-4)
     * @return The top card, or null if empty
     */
    public Card peekStack(int stackNum) {
        List<Card> stack = getDrawStack(stackNum);
        if (stack != null && !stack.isEmpty()) {
            return stack.get(0);
        }
        return null;
    }

    /**
     * Checks if a stack has cards.
     * 
     * @param stackNum The stack number (1-4)
     * @return True if stack has cards
     */
    public boolean isStackEmpty(int stackNum) {
        List<Card> stack = getDrawStack(stackNum);
        return stack == null || stack.isEmpty();
    }

    /**
     * Gets the size of a draw stack.
     * 
     * @param stackNum The stack number (1-4)
     * @return The stack size
     */
    public int getStackSize(int stackNum) {
        List<Card> stack = getDrawStack(stackNum);
        return stack != null ? stack.size() : 0;
    }

    // Getters
    
    public List<Card> getRoads() { return roads; }
    public List<Card> getSettlements() { return settlements; }
    public List<Card> getCities() { return cities; }
    public List<Card> getRegions() { return regions; }
    public List<Card> getEvents() { return events; }
    public List<Card> getDrawStack1() { return drawStack1; }
    public List<Card> getDrawStack2() { return drawStack2; }
    public List<Card> getDrawStack3() { return drawStack3; }
    public List<Card> getDrawStack4() { return drawStack4; }
}