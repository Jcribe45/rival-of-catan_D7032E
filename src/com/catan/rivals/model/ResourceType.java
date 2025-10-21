package com.catan.rivals.model;

/**
 * Enumeration of all resource types in the game.
 * 
 * SOLID: Single Responsibility - represents resource types only
 * Booch: Completeness - all game resources are represented
 */
public enum ResourceType {
    BRICK("Brick", "B"),
    GRAIN("Grain", "G"),
    LUMBER("Lumber", "L"),
    WOOL("Wool", "W"),
    ORE("Ore", "O"),
    GOLD("Gold", "A");
    
    private final String displayName;
    private final String code;
    
    ResourceType(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    /**
     * Gets the region name that produces this resource.
     * 
     * @return The region name
     */
    public String getRegionName() {
        switch (this) {
            case BRICK: return "Hill";
            case GRAIN: return "Field";
            case LUMBER: return "Forest";
            case WOOL: return "Pasture";
            case ORE: return "Mountain";
            case GOLD: return "Gold Field";
            default: return "Unknown";
        }
    }
    
    /**
     * Parses a ResourceType from a string.
     * 
     * @param str The string to parse
     * @return The ResourceType, or null if not found
     */
    public static ResourceType fromString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        
        String normalized = str.trim().toUpperCase();
        
        for (ResourceType type : values()) {
            if (type.name().equals(normalized) ||
                type.displayName.equalsIgnoreCase(str) ||
                type.code.equalsIgnoreCase(str)) {
                return type;
            }
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
