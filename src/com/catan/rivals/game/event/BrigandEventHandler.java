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
            int goldWool = countGoldAndWool(player);
            
            if (goldWool > 7) {
                // Remove all Gold and Wool
                removeGoldAndWool(player);
                player.sendMessage("Brigands stole your Gold and Wool!");
            }
        }
    }
    
    @Override
    public String getEventName() {
        return "Brigand Attack";
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
}
