package com.catan.rivals.game.phase;

import com.catan.rivals.game.Dice;
import com.catan.rivals.game.event.*;
import com.catan.rivals.model.Deck;
import com.catan.rivals.player.Player;
import java.util.*;

/**
 * Handles the event phase of a turn.
 * Delegates to specific event handlers using Strategy pattern.
 * 
 * Design Pattern: Strategy Pattern + Factory Pattern
 * SOLID: Single Responsibility - coordinates event handling
 * SOLID: Open-Closed - new events can be added without modification
 */
public class EventPhaseHandler implements PhaseHandler {
    
    private int eventDieRoll;
    private List<Player> allPlayers;
    private Deck deck;
    private Map<Integer, GameEventHandler> eventHandlers;
    
    /**
     * Constructor.
     * 
     * @param eventDieRoll The event die result
     * @param allPlayers All players in the game
     * @param deck The game deck
     */
    public EventPhaseHandler(int eventDieRoll, List<Player> allPlayers, Deck deck) {
        this.eventDieRoll = eventDieRoll;
        this.allPlayers = allPlayers;
        this.deck = deck;
        this.eventHandlers = new HashMap<>();
        initializeEventHandlers();
    }
    
    /**
     * Initializes all event handlers (Factory Pattern).
     */
    private void initializeEventHandlers() {
        eventHandlers.put(Dice.EVENT_BRIGAND, new BrigandEventHandler(allPlayers));
        eventHandlers.put(Dice.EVENT_TRADE, new TradeEventHandler(allPlayers));
        eventHandlers.put(Dice.EVENT_CELEBRATION, new CelebrationEventHandler(allPlayers));
        eventHandlers.put(Dice.EVENT_PLENTIFUL_HARVEST, new PlentifulHarvestEventHandler(allPlayers));
        eventHandlers.put(Dice.EVENT_CARD_A, new EventCardHandler(allPlayers, deck));
        eventHandlers.put(Dice.EVENT_CARD_B, new EventCardHandler(allPlayers, deck));
    }
    
    @Override
    public boolean execute(Player activePlayer, Player opponent) {
        String eventDesc = Dice.getEventDescription(eventDieRoll);
        broadcast("[Event] " + eventDesc);
        
        GameEventHandler handler = eventHandlers.get(eventDieRoll);
        
        if (handler != null) {
            handler.handleEvent(activePlayer, opponent);
        } else {
            broadcast("Unknown event: " + eventDieRoll);
        }
        
        return true;
    }
    
    @Override
    public String getPhaseName() {
        return "Event";
    }
    
    /**
     * Broadcasts message to all players.
     * 
     * @param message The message
     */
    private void broadcast(String message) {
        for (Player player : allPlayers) {
            player.sendMessage(message);
        }
    }
}
