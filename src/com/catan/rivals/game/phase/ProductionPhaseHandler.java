package com.catan.rivals.game.phase;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;
import java.util.List;

/**
 * Handles the production phase of a turn.
 * FIXED: Both players produce simultaneously when their regions match the die roll.
 * Per official rules: All regions with the rolled number produce for BOTH players at the same time.
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
     * @param productionDieRoll The production die result (1-6)
     * @param allPlayers All players in the game
     */
    public ProductionPhaseHandler(int productionDieRoll, List<Player> allPlayers) {
        this.productionDieRoll = productionDieRoll;
        this.allPlayers = allPlayers;
    }
    
    @Override
    public boolean execute(Player activePlayer, Player opponent) {
        broadcast("[Production] Die rolled: " + productionDieRoll);
        broadcast("All regions with number " + productionDieRoll + " produce resources.");
        
        // Apply production to ALL players simultaneously (per official rules)
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
     * Official Rules: Each region with the rolled number produces 1 resource.
     * Booster buildings (mills, foundries, etc.) double this to 2 resources.
     * 
     * @param player The player
     */
    private void applyProductionForPlayer(Player player) {
        List<Principality.CardPosition> regions = 
            player.getPrincipality().findCardsByType(CardType.REGION);
        
        int totalProduced = 0;
        
        for (Principality.CardPosition pos : regions) {
            Card region = pos.card;
            
            // Check if this region's number was rolled
            if (region.getDiceRoll() == productionDieRoll) {
                // Base production: +1 resource
                int production = 1;
                
                // Check for booster buildings (double production)
                if (hasAdjacentBooster(player, pos.row, pos.col)) {
                    production = 2;
                }
                
                // Add resources to the region (max 3 per region)
                int added = 0;
                for (int i = 0; i < production; i++) {
                    if (region.addResource()) {
                        added++;
                    } else {
                        // Region is full (3/3), stop adding
                        break;
                    }
                }
                
                if (added > 0) {
                    totalProduced += added;
                    player.sendMessage(String.format("  %s at (%d,%d) produced %d %s (now has %d/3)", 
                        region.getName(), pos.row, pos.col, added,
                        getResourceDisplayName(region), region.getStoredResources()));
                }
            }
        }
        
        if (totalProduced == 0) {
            player.sendMessage("  No production this turn.");
        } else {
            player.sendMessage(String.format("  Total resources produced: %d", totalProduced));
        }
    }
    
    /**
     * Checks if a region has an adjacent booster building.
     * Booster buildings double production:
     * - Iron Foundry (ore from mountains)
     * - Grain Mill (grain from fields)
     * - Lumber Camp (lumber from forests)
     * - Brick Factory (brick from hills)
     * - Weaver's Shop (wool from pastures)
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
        
        // Check adjacent positions (left and right in same row)
        // Also check above and below for settlement-placed boosters
        Card left = player.getPrincipality().getCardAt(row, col - 1);
        Card right = player.getPrincipality().getCardAt(row, col + 1);
        Card above = player.getPrincipality().getCardAt(row - 1, col);
        Card below = player.getPrincipality().getCardAt(row + 1, col);
        
        return isBoosting(left, region) || isBoosting(right, region) ||
               isBoosting(above, region) || isBoosting(below, region);
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
        
        // Check building-region matching per official rules
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
     * Gets display name for a region's resource.
     * 
     * @param region The region
     * @return Resource display name
     */
    private String getResourceDisplayName(Card region) {
        ResourceType type = region.getProducedResource();
        return type != null ? type.getDisplayName() : "resource";
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
