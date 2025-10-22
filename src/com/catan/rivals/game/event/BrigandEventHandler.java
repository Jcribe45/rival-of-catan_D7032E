package com.catan.rivals.game.event;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;
import java.util.List;

/**
 * Handles the Brigand Attack event.
 * Players lose Gold and Wool if total > 7.
 * 
 * Design Pattern: Strategy Pattern (concrete strategy)
 * SOLID: Single Responsibility - only handles brigand attacks
 */
public class BrigandEventHandler implements GameEventHandler {
    
    private List<Player> allPlayers;
    
    /**
     * Constructor.
     * 
     * @param allPlayers All players in the game
     */
    public BrigandEventHandler(List<Player> allPlayers) {
        this.allPlayers = allPlayers;
    }
    
    @Override
    public void handleEvent(Player activePlayer, Player opponent) {
        for (Player player : allPlayers) {
            int totalResources = player.getResourceBank().getTotalResources();
            if (totalResources > 7) {
                removeGoldAndWool(player);
                player.sendMessage("Brigand Attack! You had more than 7 resources, so all your Gold and Wool have been removed.");
            }
        }
    }
    
    @Override
    public String getEventName() {
        return "Brigand Attack";
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
}
