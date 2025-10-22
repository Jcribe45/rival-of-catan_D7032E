package com.catan.rivals.util;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;

/**
 * Helper class for trading operations.
 * Checks for active effects and applies appropriate trade rates.
 * 
 * Design Pattern: Strategy Pattern + Template Method
 * SOLID: Single Responsibility - handles trading only
 * SOLID: Open-Closed - extensible for new trade types
 */
public class TradingHelper {
    
    /**
     * Executes a trade for a player.
     * Automatically checks for 2:1 trade abilities.
     * 
     * @param player The player trading
     * @param giveType Resource to give
     * @param getType Resource to receive
     * @return True if trade successful
     */
    public static boolean executeTrade(Player player, ResourceType giveType, ResourceType getType) {
        if (giveType == null || getType == null || giveType == getType) {
            player.sendMessage("Invalid trade!");
            return false;
        }
        
        // Check for 2:1 trading
        if (player.getEffectTracker().hasTwoForOneTrading(giveType)) {
            return executeTwoForOneTrade(player, giveType, getType);
        }
        
        // Default: 3:1 bank trade
        return executeThreeForOneTrade(player, giveType, getType);
    }
    
    /**
     * Executes a 2:1 trade.
     */
    private static boolean executeTwoForOneTrade(Player player, ResourceType giveType, 
                                                ResourceType getType) {
        if (player.getResourceBank().getResourceCount(giveType) < 2) {
            player.sendMessage("Not enough " + giveType.getDisplayName() + "! (Need 2)");
            return false;
        }
        
        if (player.getResourceBank().removeResources(giveType, 2)) {
            player.getResourceBank().addResources(getType, 1);
            player.sendMessage("Traded 2 " + giveType.getDisplayName() + 
                             " for 1 " + getType.getDisplayName() + " (2:1 rate)");
            return true;
        }
        
        return false;
    }
    
    /**
     * Executes a 3:1 trade.
     */
    private static boolean executeThreeForOneTrade(Player player, ResourceType giveType, 
                                                  ResourceType getType) {
        if (player.getResourceBank().getResourceCount(giveType) < 3) {
            player.sendMessage("Not enough " + giveType.getDisplayName() + "! (Need 3)");
            return false;
        }
        
        if (player.getResourceBank().removeResources(giveType, 3)) {
            player.getResourceBank().addResources(getType, 1);
            player.sendMessage("Traded 3 " + giveType.getDisplayName() + 
                             " for 1 " + getType.getDisplayName() + " (3:1 rate)");
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets available trade options for a player.
     * 
     * @param player The player
     * @return Formatted string of available trades
     */
    public static String getAvailableTradesInfo(Player player) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n=== Available Trade Options ===\n");
        sb.append("Bank: 3:1 (any resource)\n");
        
        var traders = player.getEffectTracker().getAllTwoForOneTraders();
        if (!traders.isEmpty()) {
            for (ResourceType type : traders.keySet()) {
                sb.append("2:1: ").append(type.getDisplayName()).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Prompts player for trade and executes it.
     * 
     * @param player The player
     * @return True if trade completed
     */
    public static boolean promptAndExecuteTrade(Player player) {
        player.sendMessage(getAvailableTradesInfo(player));
        player.sendMessage("Enter trade: <give> <get> (or 'C' to cancel)");
        player.sendMessage("Example: Brick Lumber");
        
        String input = player.receiveInput();
        
        if (input.trim().equalsIgnoreCase("C")) {
            return false;
        }
        
        String[] parts = input.trim().split("\\s+");
        if (parts.length != 2) {
            player.sendMessage("Invalid format! Use: <give> <get>");
            return false;
        }
        
        ResourceType giveType = ResourceType.fromString(parts[0]);
        ResourceType getType = ResourceType.fromString(parts[1]);
        
        return executeTrade(player, giveType, getType);
    }
}