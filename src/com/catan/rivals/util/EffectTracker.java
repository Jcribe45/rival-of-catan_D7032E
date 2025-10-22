package com.catan.rivals.util;

import com.catan.rivals.model.*;
import java.util.*;

/**
 * Tracks active card effects for a player.
 * Uses Observer pattern to monitor board state.
 * 
 * Design Pattern: Observer Pattern + Facade Pattern
 * SOLID: Single Responsibility - manages effects only
 */
public class EffectTracker {
    
    // Trade effects (2:1 trading for specific resources)
    private Map<ResourceType, List<String>> twoForOneTraders;
    
    // Special flags for unique abilities
    private Set<String> activeFlags;
    
    // Card position tracking for effect removal
    private Map<String, Principality.CardPosition> activeEffectCards;
    
    /**
     * Constructor.
     */
    public EffectTracker() {
        this.twoForOneTraders = new EnumMap<>(ResourceType.class);
        this.activeFlags = new HashSet<>();
        this.activeEffectCards = new HashMap<>();
    }
    
    /**
     * Registers a card's effects when played.
     * 
     * @param card The card being played
     * @param position The card's position
     */
    public void registerCardEffects(Card card, Principality.CardPosition position) {
        String cardName = card.getName();
        activeEffectCards.put(cardName, position);
        
        // Register trade ship effects
        if (cardName.contains("Ship") && card.getCardType() == CardType.SHIP) {
            registerTradeShipEffect(cardName);
        }
        
        // Register special building effects
        registerSpecialBuildingEffects(cardName);
    }
    
    /**
     * Registers trade ship effects (2:1 trading).
     */
    private void registerTradeShipEffect(String cardName) {
        if (cardName.equalsIgnoreCase("Brick Ship")) {
            addTwoForOneTrader(ResourceType.BRICK, cardName);
        } else if (cardName.equalsIgnoreCase("Grain Ship")) {
            addTwoForOneTrader(ResourceType.GRAIN, cardName);
        } else if (cardName.equalsIgnoreCase("Lumber Ship")) {
            addTwoForOneTrader(ResourceType.LUMBER, cardName);
        } else if (cardName.equalsIgnoreCase("Wool Ship")) {
            addTwoForOneTrader(ResourceType.WOOL, cardName);
        } else if (cardName.equalsIgnoreCase("Ore Ship")) {
            addTwoForOneTrader(ResourceType.ORE, cardName);
        } else if (cardName.equalsIgnoreCase("Gold Ship")) {
            addTwoForOneTrader(ResourceType.GOLD, cardName);
        }
    }
    
    /**
     * Registers special building effects.
     */
    private void registerSpecialBuildingEffects(String cardName) {
        // Marketplace: +1 CP and special trading
        if (cardName.equalsIgnoreCase("Marketplace")) {
            activeFlags.add("MARKETPLACE");
        }
        
        // Parish Hall: reduced exchange cost
        if (cardName.equalsIgnoreCase("Parish Hall")) {
            activeFlags.add("PARISH_HALL");
        }
        
        // Town Hall: free exchanges
        if (cardName.equalsIgnoreCase("Town Hall")) {
            activeFlags.add("TOWN_HALL");
        }
        
        // Odin's Fountain: 2 exchanges per turn
        if (cardName.equalsIgnoreCase("Odin's Fountain")) {
            activeFlags.add("ODIN_FOUNTAIN");
        }
        
        // Sacrificial Site: 2:1 wool trading
        if (cardName.equalsIgnoreCase("Sacrificial Site")) {
            addTwoForOneTrader(ResourceType.WOOL, cardName);
        }
    }
    
    /**
     * Adds a 2:1 trader for a resource type.
     */
    private void addTwoForOneTrader(ResourceType type, String cardName) {
        twoForOneTraders.computeIfAbsent(type, k -> new ArrayList<>()).add(cardName);
    }
    
    /**
     * Removes a card's effects when it leaves play.
     * 
     * @param cardName The card name
     */
    public void unregisterCardEffects(String cardName) {
        activeEffectCards.remove(cardName);
        
        // Remove from trade effects
        for (List<String> traders : twoForOneTraders.values()) {
            traders.remove(cardName);
        }
        
        // Remove flags (simple approach - re-scan board for precise tracking)
        activeFlags.remove(cardName.toUpperCase().replace(" ", "_"));
    }
    
    /**
     * Checks if player has 2:1 trading for a resource.
     * 
     * @param type The resource type
     * @return True if has 2:1 trading
     */
    public boolean hasTwoForOneTrading(ResourceType type) {
        List<String> traders = twoForOneTraders.get(type);
        return traders != null && !traders.isEmpty();
    }
    
    /**
     * Gets all 2:1 trading options.
     * 
     * @return Map of resource types to trader card names
     */
    public Map<ResourceType, List<String>> getAllTwoForOneTraders() {
        return new HashMap<>(twoForOneTraders);
    }
    
    /**
     * Checks if a flag is active.
     * 
     * @param flag The flag name
     * @return True if active
     */
    public boolean hasFlag(String flag) {
        return activeFlags.contains(flag);
    }
    
    /**
     * Gets all active flags.
     * 
     * @return Set of active flags
     */
    public Set<String> getActiveFlags() {
        return new HashSet<>(activeFlags);
    }
    
    /**
     * Gets a summary of active effects for display.
     * 
     * @return Formatted string of active effects
     */
    public String getEffectsSummary() {
        StringBuilder sb = new StringBuilder();
        
        if (!twoForOneTraders.isEmpty() || !activeFlags.isEmpty()) {
            sb.append("\n=== Active Effects ===\n");
            
            // List 2:1 traders
            for (Map.Entry<ResourceType, List<String>> entry : twoForOneTraders.entrySet()) {
                for (String cardName : entry.getValue()) {
                    sb.append("  • ").append(cardName).append(": Trade 2 ")
                      .append(entry.getKey().getDisplayName())
                      .append(" for 1 other resource\n");
                }
            }
            
            // List special flags
            if (activeFlags.contains("MARKETPLACE")) {
                sb.append("  • Marketplace: Gain resources when opponent rolls better\n");
            }
            if (activeFlags.contains("TOWN_HALL")) {
                sb.append("  • Town Hall: Free card exchanges\n");
            }
            if (activeFlags.contains("PARISH_HALL")) {
                sb.append("  • Parish Hall: Reduced exchange cost (1 resource)\n");
            }
            if (activeFlags.contains("ODIN_FOUNTAIN")) {
                sb.append("  • Odin's Fountain: Exchange up to 2 cards per turn\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Rebuilds effect tracking from current board state.
     * Call this after loading or major board changes.
     * 
     * @param principality The principality to scan
     */
    public void rebuildFromBoard(Principality principality) {
        clear();
        
        // Scan all cards on board
        for (CardType type : new CardType[]{CardType.BUILDING, CardType.SHIP, CardType.UNIT}) {
            List<Principality.CardPosition> cards = principality.findCardsByType(type);
            for (Principality.CardPosition pos : cards) {
                registerCardEffects(pos.card, pos);
            }
        }
    }
    
    /**
     * Clears all tracked effects.
     */
    public void clear() {
        twoForOneTraders.clear();
        activeFlags.clear();
        activeEffectCards.clear();
    }
}