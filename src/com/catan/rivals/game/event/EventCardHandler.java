package com.catan.rivals.game.event;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;
import java.util.List;

public class EventCardHandler implements GameEventHandler {
    
    private List<Player> allPlayers;
    private Deck deck;
    
    public EventCardHandler(List<Player> allPlayers, Deck deck) {
        this.allPlayers = allPlayers;
        this.deck = deck;
    }
    
    @Override
    public void handleEvent(Player activePlayer, Player opponent) {
        if (deck.getEvents().isEmpty()) {
            broadcast("Event deck is empty!");
            return;
        }
        
        Card eventCard = deck.getEvents().remove(0);
        broadcast("Drew event: " + eventCard.getName());
        broadcast("  " + eventCard.getCardText());
        
        // Handle special events
        String name = eventCard.getName().toLowerCase();
        
        if (name.contains("yule")) {
            // Reshuffle event deck
            deck.shuffleEvents();
            broadcast("Yule: Event deck reshuffled!");
            // Draw another event
            handleEvent(activePlayer, opponent);
        }
        // Other event card logic can be added here
    }
    
    @Override
    public String getEventName() {
        return "Event Card";
    }
    
    private void broadcast(String message) {
        for (Player player : allPlayers) {
            player.sendMessage(message);
        }
    }
}