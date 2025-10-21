package com.catan.rivals.model;

import java.util.*;

/**
 * Manages resource storage in regions for a player.
 * 
 * SOLID: Single Responsibility - handles resource counting and transactions
 * Booch: High cohesion - all resource operations in one place
 */
public class ResourceBank {
    
    private Principality principality;
    
    /**
     * Constructor.
     * 
     * @param principality The principality to manage resources for
     */
    public ResourceBank(Principality principality) {
        this.principality = principality;
    }
    
    /**
     * Gets the count of a specific resource type across all regions.
     * 
     * @param type The resource type
     * @return The total count
     */
    public int getResourceCount(ResourceType type) {
        int count = 0;
        
        List<Principality.CardPosition> regions = principality.findCardsByType(CardType.REGION);
        
        for (Principality.CardPosition pos : regions) {
            Card region = pos.card;
            if (region.getProducedResource() == type) {
                count += region.getStoredResources();
            }
        }
        
        return count;
    }
    
    /**
     * Gets total resources across all regions.
     * 
     * @return Total resource count
     */
    public int getTotalResources() {
        int total = 0;
        
        List<Principality.CardPosition> regions = principality.findCardsByType(CardType.REGION);
        
        for (Principality.CardPosition pos : regions) {
            total += pos.card.getStoredResources();
        }
        
        return total;
    }
    
    /**
     * Adds resources to the appropriate regions.
     * Prioritizes regions with lower storage.
     * 
     * @param type The resource type
     * @param amount The amount to add
     * @return The amount actually added
     */
    public int addResources(ResourceType type, int amount) {
        int added = 0;
        
        // Get all regions that produce this resource
        List<Card> matchingRegions = new ArrayList<>();
        List<Principality.CardPosition> allRegions = principality.findCardsByType(CardType.REGION);
        
        for (Principality.CardPosition pos : allRegions) {
            if (pos.card.getProducedResource() == type) {
                matchingRegions.add(pos.card);
            }
        }
        
        if (matchingRegions.isEmpty()) {
            return 0;
        }
        
        // Sort by current storage (ascending) to fill lowest first
        matchingRegions.sort(Comparator.comparingInt(Card::getStoredResources));
        
        for (Card region : matchingRegions) {
            while (added < amount && region.getStoredResources() < 3) {
                region.addResource();
                added++;
            }
        }
        
        return added;
    }
    
    /**
     * Removes resources from regions.
     * Prioritizes regions with higher storage.
     * 
     * @param type The resource type
     * @param amount The amount to remove
     * @return True if all requested resources were removed
     */
    public boolean removeResources(ResourceType type, int amount) {
        // Get all regions that produce this resource
        List<Card> matchingRegions = new ArrayList<>();
        List<Principality.CardPosition> allRegions = principality.findCardsByType(CardType.REGION);
        
        for (Principality.CardPosition pos : allRegions) {
            if (pos.card.getProducedResource() == type) {
                matchingRegions.add(pos.card);
            }
        }
        
        if (matchingRegions.isEmpty()) {
            return false;
        }
        
        // Check if we have enough
        int available = 0;
        for (Card region : matchingRegions) {
            available += region.getStoredResources();
        }
        
        if (available < amount) {
            return false;
        }
        
        // Sort by current storage (descending) to remove from highest first
        matchingRegions.sort((a, b) -> Integer.compare(b.getStoredResources(), a.getStoredResources()));
        
        int removed = 0;
        for (Card region : matchingRegions) {
            while (removed < amount && region.getStoredResources() > 0) {
                region.removeResource();
                removed++;
            }
        }
        
        return removed == amount;
    }
    
    /**
     * Checks if player can afford a cost.
     * 
     * @param cost The cost map
     * @return True if affordable
     */
    public boolean canAfford(Map<ResourceType, Integer> cost) {
        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            if (getResourceCount(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Pays a cost by removing resources.
     * 
     * @param cost The cost map
     * @return True if successfully paid
     */
    public boolean payCost(Map<ResourceType, Integer> cost) {
        // Check if affordable first
        if (!canAfford(cost)) {
            return false;
        }
        
        // Remove resources
        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            if (!removeResources(entry.getKey(), entry.getValue())) {
                // Should not happen if canAfford was true
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Gets a summary of all resources.
     * 
     * @return A map of resource types to counts
     */
    public Map<ResourceType, Integer> getResourceSummary() {
        Map<ResourceType, Integer> summary = new EnumMap<>(ResourceType.class);
        
        for (ResourceType type : ResourceType.values()) {
            summary.put(type, getResourceCount(type));
        }
        
        return summary;
    }
}
