package com.catan.rivals.util;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;
import java.util.Map;

/**
 * Helper class for resource transactions.
 * Handles payments, refunds, and validation.
 * 
 * Design Pattern: Utility/Helper Pattern
 * SOLID: Single Responsibility - handles resource transactions only
 */
public class ResourceTransactionHelper {
    
    /**
     * Pays a resource cost, prompting player for resource type.
     * 
     * @param player The player paying
     * @param cost The amount to pay
     * @param description Description of what's being paid for
     * @return The ResourceType paid, or null if failed
     */
    public static ResourceType payResourceCost(Player player, int cost, String description) {
        if (cost <= 0) {
            return ResourceType.GOLD; // Dummy return for free
        }
        
        if (player.getResourceBank().getTotalResources() < cost) {
            player.sendMessage("Not enough resources! (Need " + cost + ")");
            return null;
        }
        
        player.sendMessage("\n" + description + " cost: " + cost + " resource" + 
                          (cost > 1 ? "s" : ""));
        
        ResourceType type = player.chooseResourceType("Choose resource type to pay:");
        
        if (type == null) {
            return null;
        }
        
        if (player.getResourceBank().getResourceCount(type) < cost) {
            player.sendMessage("Not enough " + type.getDisplayName() + "! " +
                              "(Have: " + player.getResourceBank().getResourceCount(type) + 
                              ", Need: " + cost + ")");
            return null;
        }
        
        if (player.getResourceBank().removeResources(type, cost)) {
            return type;
        }
        
        return null;
    }
    
    /**
     * Refunds resources to a player.
     * 
     * @param player The player receiving refund
     * @param type The resource type
     * @param amount The amount to refund
     */
    public static void refundResources(Player player, ResourceType type, int amount) {
        if (type != null && amount > 0) {
            player.getResourceBank().addResources(type, amount);
            player.sendMessage("Refunded " + amount + " " + type.getDisplayName());
        }
    }
    
    /**
     * Validates if player can afford a cost.
     * 
     * @param player The player
     * @param cost The cost map
     * @return True if affordable
     */
    public static boolean canAfford(Player player, Map<ResourceType, Integer> cost) {
        return player.getResourceBank().canAfford(cost);
    }
    
    /**
     * Pays a specific resource cost from a map.
     * 
     * @param player The player
     * @param cost The cost map
     * @param description Description of payment
     * @return True if successfully paid
     */
    public static boolean payResourceMap(Player player, Map<ResourceType, Integer> cost, 
                                        String description) {
        if (!canAfford(player, cost)) {
            player.sendMessage("Cannot afford: " + description);
            return false;
        }
        
        return player.getResourceBank().payCost(cost);
    }
}