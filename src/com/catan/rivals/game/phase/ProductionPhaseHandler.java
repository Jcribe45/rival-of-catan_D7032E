package com.catan.rivals.game.phase;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;
import java.util.List;

/**
 * Handles the production phase of a turn.
 * Applies resource production based on dice roll.
 * 
 * Design Pattern: Strategy Pattern (concrete strategy)
 * SOLID: Single Responsibility - only handles production
 */
public class ProductionPhaseHandler implements PhaseHandler {
    
    private int productionDieRoll;
    private List<Player> allPlayers;
    
    /**
     * Constructor.
     * 
     * @param productionDieRoll The production die result
     * @param allPlayers All players in the game
     */
    public ProductionPhaseHandler(int productionDieRoll, List<Player> allPlayers) {
        this.productionDieRoll = productionDieRoll;
        this.allPlayers = allPlayers;
    }
    
    @Override
    public boolean execute(Player activePlayer, Player opponent) {
        broadcast("[Production] Die face: " + productionDieRoll);
        
        // Apply production to all players
        for (Player player : allPlayers) {
            applyProductionForPlayer(player);
        }
        
        return true;
    }
    
    @Override
    public String getPhaseName() {
        return "Production";
    }
    
    /**
     * Applies production for a single player.
     * 
     * @param player The player
     */
    private void applyProductionForPlayer(Player player) {
        List<Principality.CardPosition> regions = 
            player.getPrincipality().findCardsByType(CardType.REGION);
        
        for (Principality.CardPosition pos : regions) {
            Card region = pos.card;
            
            if (region.getDiceRoll() == productionDieRoll) {
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
