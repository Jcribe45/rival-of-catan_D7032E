package com.catan.rivals.game;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;
import java.util.List;

/**
 * Handles initial game setup for players.
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
    }
    
    /**
     * Sets up a single player's starting principality.
     * Layout (5x5 grid, center row = 2):
     * Row 1: Region | Empty | Region | Empty | Region
     * Row 2: Empty  | Settlement | Road | Settlement | Empty
     * Row 3: Region | Empty | Region | Empty | Region
     * 
     * @param player The player
     * @param deck The deck
     * @param playerIndex Player index (0 or 1) for different dice assignments
     */
    private static void setupPlayerPrincipality(Player player, Deck deck, int playerIndex) {
        Principality prin = player.getPrincipality();
        int centerRow = 2;
        
        // Place center cards (settlements and road)
        Card settlement1 = deck.removeCardByName(deck.getSettlements(), "Settlement");
        Card road = deck.removeCardByName(deck.getRoads(), "Road");
        Card settlement2 = deck.removeCardByName(deck.getSettlements(), "Settlement");
        
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
        
        // Place regions with asymmetric dice assignments
        setupRegions(player, deck, playerIndex);
    }
    
    /**
     * Sets up regions for a player with specific dice assignments.
     * 
     * @param player The player
     * @param deck The deck
     * @param playerIndex 0 or 1
     */
    private static void setupRegions(Player player, Deck deck, int playerIndex) {
        Principality prin = player.getPrincipality();
        
        // Dice assignments for two players (from original game rules)
        // Player 0: Forest=2, Gold=1, Field=6, Hill=3, Pasture=4, Mountain=5
        // Player 1: Forest=3, Gold=4, Field=5, Hill=2, Pasture=1, Mountain=6
        int[][] diceAssignments = {
            {2, 1, 6, 3, 4, 5},  // Player 0
            {3, 4, 5, 2, 1, 6}   // Player 1
        };
        
        int[] dice = diceAssignments[playerIndex % 2];
        
        // Place regions in specific positions
        placeRegion(player, deck, "Forest", 1, 0, dice[0], 1);
        placeRegion(player, deck, "Gold Field", 1, 2, dice[1], 0);
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
     * @param diceValue The production die value
     * @param initialResources Initial stored resources
     */
    private static void placeRegion(Player player, Deck deck, String regionName, 
                                    int row, int col, int diceValue, int initialResources) {
        Card region = deck.removeCardByName(deck.getRegions(), regionName);
        
        if (region != null) {
            region.setDiceRoll(diceValue);
            region.setStoredResources(initialResources);
            player.getPrincipality().placeCard(row, col, region);
        }
    }
    
    /**
     * Draws initial hand for a player (3 cards).
     * 
     * @param player The player
     * @param deck The deck
     */
    private static void drawInitialHand(Player player, Deck deck) {
        for (int i = 1; i <= 3; i++) {
            Card card = deck.drawFromStack(i);
            if (card != null) {
                player.addCardToHand(card);
            }
        }
    }
    
    /**
     * Assigns dice values to remaining regions in the deck.
     * Each region type has specific dice distributions.
     */
    private static void assignRemainingRegionDice(Deck deck) {
        // Remaining regions get these dice values:
        // 2 Fields: 3, 1
        // 2 Mountains: 4, 2
        // 2 Hills: 5, 1
        // 2 Forests: 6, 4
        // 2 Pastures: 6, 5
        // 2 Gold Fields: 3, 2
        
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
}
