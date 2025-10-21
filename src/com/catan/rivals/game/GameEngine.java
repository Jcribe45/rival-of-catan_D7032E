package com.catan.rivals.game;

import com.catan.rivals.model.*;
import com.catan.rivals.player.*;
import com.catan.rivals.util.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Main game engine coordinating the game flow.
 * Uses Template Method pattern for turn execution.
 * 
 * Design Pattern: Template Method, Observer
 * SOLID: Single Responsibility - coordinates game flow only
 * SOLID: Open-Closed - extensible through observers
 */
public class GameEngine {
    
    private Deck deck;
    private List<Player> players;
    private VictoryCondition victoryCondition;
    private Dice dice;
    private List<GameObserver> observers;
    private GamePhase currentPhase;
    
    /**
     * Constructor.
     */
    public GameEngine() {
        this.players = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.dice = new Dice();
        this.victoryCondition = VictoryCondition.introductoryGame();
        this.currentPhase = GamePhase.SETUP;
    }
    
    /**
     * Initializes the game from cards.json file.
     * 
     * @param cardsPath Path to cards.json
     * @throws IOException If file cannot be read
     */
    public void initialize(String cardsPath) throws IOException {
        deck = CardFactory.loadCardsFromJson(cardsPath);
        GameSetup.setupGame(deck, players);
        notifyObservers("GAME_INITIALIZED", null);
    }
    
    /**
     * Adds a player to the game.
     * 
     * @param player The player to add
     */
    public void addPlayer(Player player) {
        if (players.size() < 2) {
            players.add(player);
        }
    }
    
    /**
     * Adds an observer for game events.
     * 
     * @param observer The observer
     */
    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }
    
    /**
     * Starts the main game loop.
     */
    public void startGame() {
        if (players.size() != 2) {
            System.err.println("Need exactly 2 players to start");
            return;
        }
        
        broadcast("=== RIVALS FOR CATAN - GAME START ===");
        int currentPlayerIndex = 0;
        
        while (currentPhase != GamePhase.GAME_OVER) {
            Player active = players.get(currentPlayerIndex);
            Player opponent = players.get((currentPlayerIndex + 1) % 2);
            
            broadcast("\n--- " + active + "'s Turn ---");
            executeTurn(active, opponent);
            
            if (currentPhase == GamePhase.GAME_OVER) {
                break;
            }
            
            currentPlayerIndex = (currentPlayerIndex + 1) % 2;
        }
    }
    
    /**
     * Executes a complete turn (Template Method).
     * 
     * @param active The active player
     * @param opponent The opponent
     */
    private void executeTurn(Player active, Player opponent) {
        // Phase 1: Roll Dice
        currentPhase = GamePhase.ROLL_DICE;
        int[] rolls = rollDice(active);
        int production = rolls[0];
        int event = rolls[1];
        
        // Phase 2 & 3: Apply Production and Event
        // Order depends on event type (Brigand first, then production)
        if (event == Dice.EVENT_BRIGAND) {
            currentPhase = GamePhase.EVENT;
            handleEvent(event, active, opponent);
            
            currentPhase = GamePhase.PRODUCTION;
            applyProduction(production);
        } else {
            currentPhase = GamePhase.PRODUCTION;
            applyProduction(production);
            
            currentPhase = GamePhase.EVENT;
            handleEvent(event, active, opponent);
        }
        
        // Phase 4: Action Phase
        currentPhase = GamePhase.ACTION;
        active.takeActions(opponent, deck);
        
        // Phase 5: Replenish Hand
        currentPhase = GamePhase.REPLENISH;
        replenishHand(active);
        
        // Phase 6: Exchange (optional)
        currentPhase = GamePhase.EXCHANGE;
        offerExchange(active);
        
        // Phase 7: Victory Check
        currentPhase = GamePhase.VICTORY_CHECK;
        if (victoryCondition.hasWon(active, opponent)) {
            handleVictory(active, opponent);
        }
    }
    
    /**
     * Rolls both dice.
     * 
     * @param active The active player
     * @return [production, event]
     */
    private int[] rollDice(Player active) {
        int[] rolls = dice.rollBoth();
        broadcast("Production Die: " + rolls[0]);
        broadcast("Event Die: " + rolls[1] + " (" + Dice.getEventDescription(rolls[1]) + ")");
        notifyObservers("DICE_ROLLED", rolls);
        return rolls;
    }
    
    /**
     * Applies production for all players.
     * 
     * @param productionFace The production die face (1-6)
     */
    private void applyProduction(int productionFace) {
        broadcast("[Production] Die face: " + productionFace);
        
        for (Player player : players) {
            applyProductionForPlayer(player, productionFace);
        }
    }
    
    /**
     * Applies production for a single player.
     * 
     * @param player The player
     * @param productionFace The die face
     */
    private void applyProductionForPlayer(Player player, int productionFace) {
        List<Principality.CardPosition> regions = 
            player.getPrincipality().findCardsByType(CardType.REGION);
        
        for (Principality.CardPosition pos : regions) {
            Card region = pos.card;
            
            if (region.getDiceRoll() == productionFace) {
                // Base production: +1 resource
                int production = 1;
                
                // Check for booster buildings (double production)
                if (hasAdjacentBooster(player, pos.row, pos.col)) {
                    production = 2;
                }
                
                // Add resources (max 3 per region)
                for (int i = 0; i < production; i++) {
                    region.addResource();
                }
            }
        }
    }
    
    /**
     * Checks if a region has an adjacent booster building.
     * 
     * @param player The player
     * @param row Region row
     * @param col Region column
     * @return True if has booster
     */
    private boolean hasAdjacentBooster(Player player, int row, int col) {
        Card region = player.getPrincipality().getCardAt(row, col);
        if (region == null) {
            return false;
        }
        
        // Check left and right for booster buildings
        Card left = player.getPrincipality().getCardAt(row, col - 1);
        Card right = player.getPrincipality().getCardAt(row, col + 1);
        
        return isBoosting(left, region) || isBoosting(right, region);
    }
    
    /**
     * Checks if a building boosts a region.
     * 
     * @param building The potential booster
     * @param region The region
     * @return True if boosts
     */
    private boolean isBoosting(Card building, Card region) {
        if (building == null || building.getCardType() != CardType.BUILDING) {
            return false;
        }
        
        String bName = building.getName();
        String rName = region.getName();
        
        if ("Iron Foundry".equalsIgnoreCase(bName) && "Mountain".equalsIgnoreCase(rName)) {
            return true;
        }
        if ("Grain Mill".equalsIgnoreCase(bName) && "Field".equalsIgnoreCase(rName)) {
            return true;
        }
        if ("Lumber Camp".equalsIgnoreCase(bName) && "Forest".equalsIgnoreCase(rName)) {
            return true;
        }
        if ("Brick Factory".equalsIgnoreCase(bName) && "Hill".equalsIgnoreCase(rName)) {
            return true;
        }
        if ("Weaver's Shop".equalsIgnoreCase(bName) && "Pasture".equalsIgnoreCase(rName)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Handles an event.
     * 
     * @param eventFace The event die face
     * @param active The active player
     * @param opponent The opponent
     */
    private void handleEvent(int eventFace, Player active, Player opponent) {
        switch (eventFace) {
            case Dice.EVENT_BRIGAND:
                handleBrigand();
                break;
            case Dice.EVENT_TRADE:
                handleTrade();
                break;
            case Dice.EVENT_CELEBRATION:
                handleCelebration();
                break;
            case Dice.EVENT_PLENTIFUL_HARVEST:
                handlePlentifulHarvest();
                break;
            case Dice.EVENT_CARD_A:
            case Dice.EVENT_CARD_B:
                handleEventCard(active, opponent);
                break;
        }
    }
    
    /**
     * Handles Brigand Attack event.
     * Players lose Gold and Wool if total > 7.
     */
    private void handleBrigand() {
        broadcast("[Event] Brigand Attack!");
        
        for (Player player : players) {
            int goldWool = countGoldAndWool(player);
            
            if (goldWool > 7) {
                // Remove all Gold and Wool
                removeGoldAndWool(player);
                player.sendMessage("Brigands stole your Gold and Wool!");
            }
        }
    }
    
    /**
     * Counts Gold and Wool resources for a player.
     * 
     * @param player The player
     * @return Total count
     */
    private int countGoldAndWool(Player player) {
        int count = 0;
        count += player.getResourceBank().getResourceCount(ResourceType.GOLD);
        count += player.getResourceBank().getResourceCount(ResourceType.WOOL);
        return count;
    }
    
    /**
     * Removes all Gold and Wool from a player.
     * 
     * @param player The player
     */
    private void removeGoldAndWool(Player player) {
        List<Principality.CardPosition> regions = 
            player.getPrincipality().findCardsByType(CardType.REGION);
        
        for (Principality.CardPosition pos : regions) {
            Card region = pos.card;
            ResourceType type = region.getProducedResource();
            
            if (type == ResourceType.GOLD || type == ResourceType.WOOL) {
                region.setStoredResources(0);
            }
        }
    }
    
    /**
     * Handles Trade event.
     * Player with trade advantage gains 1 resource.
     */
    private void handleTrade() {
        broadcast("[Event] Trade!");
        
        for (Player player : players) {
            Player opponent = getOpponent(player);
            
            if (victoryCondition.hasTradeAdvantage(player, opponent)) {
                player.sendMessage("Trade advantage! Choose 1 resource:");
                String input = player.receiveInput();
                ResourceType type = ResourceType.fromString(input);
                
                if (type != null) {
                    player.getResourceBank().addResources(type, 1);
                }
            }
        }
    }
    
    /**
     * Handles Celebration event.
     * Player with most skill points gains 1 resource.
     */
    private void handleCelebration() {
        broadcast("[Event] Celebration!");
        
        int skill0 = players.get(0).getSkillPoints();
        int skill1 = players.get(1).getSkillPoints();
        
        if (skill0 == skill1) {
            // Tie: both players gain 1 resource
            for (Player player : players) {
                player.sendMessage("Celebration (tie)! Choose 1 resource:");
                String input = player.receiveInput();
                ResourceType type = ResourceType.fromString(input);
                
                if (type != null) {
                    player.getResourceBank().addResources(type, 1);
                }
            }
        } else {
            // Winner gets 1 resource
            Player winner = skill0 > skill1 ? players.get(0) : players.get(1);
            winner.sendMessage("Celebration (you have most skill)! Choose 1 resource:");
            String input = winner.receiveInput();
            ResourceType type = ResourceType.fromString(input);
            
            if (type != null) {
                winner.getResourceBank().addResources(type, 1);
            }
        }
    }
    
    /**
     * Handles Plentiful Harvest event.
     * Each player gains 1 resource of choice.
     */
    private void handlePlentifulHarvest() {
        broadcast("[Event] Plentiful Harvest!");
        
        for (Player player : players) {
            player.sendMessage("Choose 1 resource:");
            String input = player.receiveInput();
            ResourceType type = ResourceType.fromString(input);
            
            if (type != null) {
                player.getResourceBank().addResources(type, 1);
            }
        }
    }
    
    /**
     * Handles drawing an event card.
     * 
     * @param active The active player
     * @param opponent The opponent
     */
    private void handleEventCard(Player active, Player opponent) {
        if (deck.getEvents().isEmpty()) {
            broadcast("[Event] Event deck is empty!");
            return;
        }
        
        Card eventCard = deck.getEvents().remove(0);
        broadcast("[Event] Drew: " + eventCard.getName());
        broadcast("  " + eventCard.getCardText());
        
        // Simple event handling - can be expanded
        String name = eventCard.getName().toLowerCase();
        
        if (name.contains("yule")) {
            // Reshuffle event deck
            deck.shuffleEvents();
            handleEventCard(active, opponent); // Draw again
        }
    }
    
    /**
     * Replenishes a player's hand to hand limit.
     * 
     * @param player The player
     */
    private void replenishHand(Player player) {
        int handLimit = 3 + player.getProgressPoints();
        
        while (player.getHand().size() < handLimit) {
            player.sendMessage("Draw from stack (1-4):");
            String input = player.receiveInput();
            
            try {
                int stackNum = Integer.parseInt(input.trim());
                Card card = deck.drawFromStack(stackNum);
                
                if (card != null) {
                    player.addCardToHand(card);
                } else {
                    player.sendMessage("Stack empty, trying next...");
                    // Try other stacks
                    for (int i = 1; i <= 4; i++) {
                        card = deck.drawFromStack(i);
                        if (card != null) {
                            player.addCardToHand(card);
                            break;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid input");
            }
        }
    }
    
    /**
     * Offers optional card exchange.
     * 
     * @param player The player
     */
    private void offerExchange(Player player) {
        player.sendMessage("Exchange a card? (Y/N)");
        String input = player.receiveInput();
        
        if (input != null && input.trim().toUpperCase().startsWith("Y")) {
            // Simple exchange implementation
            player.sendMessage("Feature not yet implemented");
        }
    }
    
    /**
     * Handles victory.
     * 
     * @param winner The winner
     * @param loser The loser
     */
    private void handleVictory(Player winner, Player loser) {
        int finalScore = victoryCondition.calculateTotalVictoryPoints(winner, loser);
        
        broadcast("\n=================================");
        broadcast("         GAME OVER!");
        broadcast("=================================");
        broadcast("Winner: " + winner);
        broadcast("Final Score: " + finalScore + " Victory Points");
        broadcast(victoryCondition.getVictoryPointsSummary(winner, loser));
        broadcast("=================================");
        
        currentPhase = GamePhase.GAME_OVER;
        notifyObservers("GAME_WON", winner);
    }
    
    /**
     * Gets the opponent of a player.
     * 
     * @param player The player
     * @return The opponent
     */
    private Player getOpponent(Player player) {
        return player == players.get(0) ? players.get(1) : players.get(0);
    }
    
    /**
     * Broadcasts a message to all players.
     * 
     * @param message The message
     */
    private void broadcast(String message) {
        for (Player player : players) {
            player.sendMessage(message);
        }
    }
    
    /**
     * Notifies all observers of an event.
     * 
     * @param event The event name
     * @param data The event data
     */
    private void notifyObservers(String event, Object data) {
        for (GameObserver observer : observers) {
            observer.onEvent(event, data);
        }
    }
    
    /**
     * Main entry point for standalone execution.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            GameEngine engine = new GameEngine();
            
            // Setup players
            if (args.length > 0 && args[0].equalsIgnoreCase("bot")) {
                engine.addPlayer(new HumanPlayer("Player 1"));
                engine.addPlayer(new AIPlayer("Bot"));
            } else {
                engine.addPlayer(new HumanPlayer("Player 1"));
                engine.addPlayer(new HumanPlayer("Player 2"));
            }
            
            // Initialize and start
            engine.initialize("cards.json");
            engine.startGame();
            
        } catch (IOException e) {
            System.err.println("Failed to load cards: " + e.getMessage());
        }
    }
}
