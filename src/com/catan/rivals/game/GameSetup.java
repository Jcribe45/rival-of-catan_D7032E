package com.catan.rivals.game;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;

/**
 * Handles initial game setup for players.
 * FIXED: Corrected building costs per official Rivals for Catan rules.
 * 
 * SOLID: Single Responsibility - handles setup only
 * Booch: Completeness - all setup logic in one place
 */
public class GameSetup {
    
    /**
     * Sets up the game for all players.
     * 
     * @param deck The game deck
     * @param players The players
     */
    public static void setupGame(Deck deck, List<Player> players) {
        // Setup each player's principality
        for (int i = 0; i < players.size(); i++) {
            setupPlayerPrincipality(players.get(i), deck, i);
        }
        
        // Initial card draw for each player
        for (Player player : players) {
            drawInitialHand(player, deck);
        }
        
        // Shuffle all decks
        deck.shuffleDrawStacks();
        deck.shuffleEvents();
        deck.shuffleRegions();
        
        // Setup remaining regions with dice values
        assignRemainingRegionDice(deck);
        
        // Initialize effect tracking from starting boards
        for (Player player : players) {
            player.getEffectTracker().rebuildFromBoard(player.getPrincipality());
        }
    }
    
    /**
     * Sets up a single player's starting principality.
     * Official Rules Layout:
     * Row 1: Region(2) | Empty     | Region(1) | Empty     | Region(6)
     * Row 2: Empty     | Settlement| Road      | Settlement| Empty
     * Row 3: Region(3) | Empty     | Region(4) | Empty     | Region(5)
     * 
     * Player 0 dice: Forest=2, Gold=1, Field=6, Hill=3, Pasture=4, Mountain=5
     * Player 1 dice: Forest=3, Gold=4, Field=5, Hill=2, Pasture=1, Mountain=6
     * 
     * @param player The player
     * @param deck The deck
     * @param playerIndex Player index (0 or 1) for different dice assignments
     */
    private static void setupPlayerPrincipality(Player player, Deck deck, int playerIndex) {
        Principality prin = player.getPrincipality();
        int centerRow = 2;
        
        // Place center cards (settlements and road) - FIXED COSTS
        Card settlement1 = createSettlement();
        Card road = createRoad();
        Card settlement2 = createSettlement();
        
        if (settlement1 != null) {
            prin.placeCard(centerRow, 1, settlement1);
            player.addVictoryPoints(1); // Settlement = 1 VP
        }
        
        if (road != null) {
            prin.placeCard(centerRow, 2, road);
        }
        
        if (settlement2 != null) {
            prin.placeCard(centerRow, 3, settlement2);
            player.addVictoryPoints(1); // Settlement = 1 VP
        }
        
        // Place regions with asymmetric dice assignments per official rules
        setupRegions(player, deck, playerIndex);
    }
    
    /**
     * Creates a Settlement card with correct cost.
     * Official Cost: 1 Brick, 1 Grain, 1 Lumber, 1 Wool
     */
    private static Card createSettlement() {
        Card settlement = new Card("Settlement");
        settlement.setCardType(CardType.CENTER_CARD);
        settlement.setVictoryPoints(1);
        settlement.setPlacement("Settlement/city");
        
        Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        cost.put(ResourceType.BRICK, 1);
        cost.put(ResourceType.GRAIN, 1);
        cost.put(ResourceType.LUMBER, 1);
        cost.put(ResourceType.WOOL, 1);
        settlement.setCost(cost);
        
        return settlement;
    }
    
    /**
     * Creates a Road card with correct cost.
     * Official Cost: 2 Brick, 1 Lumber
     */
    private static Card createRoad() {
        Card road = new Card("Road");
        road.setCardType(CardType.CENTER_CARD);
        road.setPlacement("Road");
        
        Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        cost.put(ResourceType.BRICK, 2);
        cost.put(ResourceType.LUMBER, 1);
        road.setCost(cost);
        
        return road;
    }
    
    /**
     * Sets up regions for a player with specific dice assignments.
     * Per official rules:
     * - Player 0: Forest=2, Gold=1, Field=6, Hill=3, Pasture=4, Mountain=5
     * - Player 1: Forest=3, Gold=4, Field=5, Hill=2, Pasture=1, Mountain=6
     * 
     * @param player The player
     * @param deck The deck
     * @param playerIndex 0 or 1
     */
    private static void setupRegions(Player player, Deck deck, int playerIndex) {
        
        // Dice assignments for two players (from official game rules)
        int[][] diceAssignments = {
            {2, 1, 6, 3, 4, 5},  // Player 0
            {3, 4, 5, 2, 1, 6}   // Player 1
        };
        
        int[] dice = diceAssignments[playerIndex % 2];
        
        // Place regions in specific positions per official layout
        // Row 1: Forest, Gold Field, Field
        // Row 3: Hill, Pasture, Mountain
        placeRegion(player, deck, "Forest", 1, 0, dice[0], 1);
        placeRegion(player, deck, "Gold Field", 1, 2, dice[1], 0); // Gold starts with 0
        placeRegion(player, deck, "Field", 1, 4, dice[2], 1);
        placeRegion(player, deck, "Hill", 3, 0, dice[3], 1);
        placeRegion(player, deck, "Pasture", 3, 2, dice[4], 1);
        placeRegion(player, deck, "Mountain", 3, 4, dice[5], 1);
    }
    
    /**
     * Places a specific region on the board.
     * 
     * @param player The player
     * @param deck The deck
     * @param regionName The region name
     * @param row The row
     * @param col The column
     * @param diceValue The production die value (1-6)
     * @param initialResources Initial stored resources
     */
    private static void placeRegion(Player player, Deck deck, String regionName, 
                                    int row, int col, int diceValue, int initialResources) {
        Card region = deck.removeCardByName(deck.getRegions(), regionName);
        
        if (region != null) {
            region.setCardType(CardType.REGION);
            region.setDiceRoll(diceValue);
            region.setStoredResources(initialResources);
            player.getPrincipality().placeCard(row, col, region);
        }
    }
    
    /**
     * Draws initial hand for a player (3 cards from stacks 1-3).
     * Official Rules: Each player draws 1 card from stack 1, 1 from stack 2, 1 from stack 3.
     * 
     * @param player The player
     * @param deck The deck
     */
    private static void drawInitialHand(Player player, Deck deck) {
        for (int stackNum = 1; stackNum <= 3; stackNum++) {
            Card card = deck.drawFromStack(stackNum);
            if (card != null) {
                player.addCardToHand(card);
            }
        }
    }
    
    /**
     * Assigns dice values to remaining regions in the deck.
     * Official Rules specify which dice values go on which regions.
     * 
     * Remaining regions get these dice values:
     * - 2 Fields: 3, 1
     * - 2 Mountains: 4, 2
     * - 2 Hills: 5, 1
     * - 2 Forests: 6, 4
     * - 2 Pastures: 6, 5
     * - 2 Gold Fields: 3, 2
     */
    private static void assignRemainingRegionDice(Deck deck) {
        assignDiceToRegionType(deck.getRegions(), "Field", 3, 1);
        assignDiceToRegionType(deck.getRegions(), "Mountain", 4, 2);
        assignDiceToRegionType(deck.getRegions(), "Hill", 5, 1);
        assignDiceToRegionType(deck.getRegions(), "Forest", 6, 4);
        assignDiceToRegionType(deck.getRegions(), "Pasture", 6, 5);
        assignDiceToRegionType(deck.getRegions(), "Gold Field", 3, 2);
    }
    
    /**
     * Assigns specific dice values to regions of a type.
     * 
     * @param regions The region list
     * @param regionName The region name
     * @param dice1 First die value
     * @param dice2 Second die value
     */
    private static void assignDiceToRegionType(List<Card> regions, String regionName, 
                                               int dice1, int dice2) {
        int assigned = 0;
        
        for (Card region : regions) {
            if (region.getName().equalsIgnoreCase(regionName) && region.getDiceRoll() == 0) {
                if (assigned == 0) {
                    region.setDiceRoll(dice1);
                    assigned++;
                } else if (assigned == 1) {
                    region.setDiceRoll(dice2);
                    break;
                }
            }
        }
    }
    
    /**
     * Gets the correct cost for a center card type.
     * 
     * @param type The card type ("Road", "Settlement", "City")
     * @return Cost map
     */
    public static Map<ResourceType, Integer> getCenterCardCost(String type) {
        Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        
        switch (type.toUpperCase()) {
            case "ROAD":
                // Official: 2 Brick, 1 Lumber
                cost.put(ResourceType.BRICK, 2);
                cost.put(ResourceType.LUMBER, 1);
                break;
                
            case "SETTLEMENT":
                // Official: 1 Brick, 1 Grain, 1 Lumber, 1 Wool
                cost.put(ResourceType.BRICK, 1);
                cost.put(ResourceType.GRAIN, 1);
                cost.put(ResourceType.LUMBER, 1);
                cost.put(ResourceType.WOOL, 1);
                break;
                
            case "CITY":
                // Official: 3 Grain, 2 Ore
                cost.put(ResourceType.GRAIN, 3);
                cost.put(ResourceType.ORE, 2);
                break;
        }
        
        return cost;
    }
}
