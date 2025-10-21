package com.catan.rivals.game;

import com.catan.rivals.model.*;
import com.catan.rivals.player.*;
import com.catan.rivals.util.*;
import com.catan.rivals.game.phase.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Main game engine coordinating the game flow.
 * Uses Phase Handlers (Strategy Pattern) to delegate phase execution.
 * 
 * Design Pattern: Template Method + Strategy Pattern + Facade Pattern
 * SOLID: Single Responsibility - coordinates game flow, delegates phases
 * SOLID: Open-Closed - new phases can be added without modifying this class
 * 
 * This refactored version is much cleaner with phases extracted to handlers.
 */
public class GameEngine {
    
    private Deck deck;
    private List<Player> players;
    private VictoryCondition victoryCondition;
    private Dice dice;
    private List<GameObserver> observers;
    private GamePhase currentPhase;
    
    // Phase handlers (Strategy Pattern)
    private ExchangePhaseHandler exchangeHandler;
    private ReplenishPhaseHandler replenishHandler;
    private VictoryCheckHandler victoryHandler;
    
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
        
        // Initialize phase handlers
        exchangeHandler = new ExchangePhaseHandler(deck);
        replenishHandler = new ReplenishPhaseHandler(deck);
        victoryHandler = new VictoryCheckHandler(victoryCondition);
        
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
        displayGameStateToAll();
        
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
     * Executes a complete turn using Template Method pattern.
     * Delegates each phase to appropriate handler.
     * 
     * @param active The active player
     * @param opponent The opponent
     */
    private void executeTurn(Player active, Player opponent) {
        // Phase 1: Roll Dice
        currentPhase = GamePhase.ROLL_DICE;
        int[] rolls = dice.rollBoth();
        int production = rolls[0];
        int event = rolls[1];
        
        broadcast("Production Die: " + production);
        broadcast("Event Die: " + event + " (" + Dice.getEventDescription(event) + ")");
        notifyObservers("DICE_ROLLED", rolls);
        
        // Phase 2 & 3: Apply Production and Event
        // (Order depends on event type - Brigand first, then production)
        if (event == Dice.EVENT_BRIGAND) {
            executePhase(new EventPhaseHandler(event, players, deck));
            executePhase(new ProductionPhaseHandler(production, players));
        } else {
            executePhase(new ProductionPhaseHandler(production, players));
            executePhase(new EventPhaseHandler(event, players, deck));
        }
        
        // Show updated board state
        displayGameStateToAll();
        
        // Phase 4: Action Phase
        currentPhase = GamePhase.ACTION;
        active.takeActions(opponent, deck);
        
        // Phase 5: Replenish Hand
        currentPhase = GamePhase.REPLENISH;
        replenishHandler.execute(active, opponent);
        
        // Phase 6: Exchange (optional)
        currentPhase = GamePhase.EXCHANGE;
        exchangeHandler.executeExchange(active);
        
        // Phase 7: Victory Check
        currentPhase = GamePhase.VICTORY_CHECK;
        if (victoryHandler.checkVictory(active, opponent)) {
            handleVictory(active, opponent);
        }
    }
    
    /**
     * Executes a phase using its handler.
     * Template Method pattern coordination.
     * 
     * @param handler The phase handler
     */
    private void executePhase(PhaseHandler handler) {
        if (handler.shouldExecute(players.get(0), players.get(1))) {
            handler.execute(players.get(0), players.get(1));
        }
    }
    
    /**
     * Displays the complete game state to all players.
     */
    private void displayGameStateToAll() {
        for (Player player : players) {
            Player opponent = getOpponent(player);
            player.sendMessage(
                PrincipalityRenderer.renderBothPrincipalities(player, opponent)
            );
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
            
            // Setup two human players
            engine.addPlayer(new HumanPlayer("Player 1"));
            engine.addPlayer(new HumanPlayer("Player 2"));
            
            // Initialize and start
            engine.initialize("cards.json");
            engine.startGame();
            
        } catch (IOException e) {
            System.err.println("Failed to load cards: " + e.getMessage());
        }
    }
}
