package com.catan.rivals.model;

import com.catan.rivals.player.Player;
import java.util.Map;
import java.util.EnumMap;

/**
 * Represents a card in the game.
 * 
 * SOLID: Single Responsibility - represents card data and behavior
 * SOLID: Open-Closed - extensible through CardEffect strategy
 * Booch: High cohesion - all card-related data in one place
 */
public class Card implements Comparable<Card> {
    
    // Identity
    private String name;
    private String germanName;
    private String theme;
    
    // Type and placement
    private CardType cardType;
    private String placement;
    private String oneOf;  // Restrictions like "1x"
    private String requires;  // Prerequisites
    
    // Cost
    private Map<ResourceType, Integer> cost;
    
    // Points
    private int victoryPoints;
    private int commercePoints;
    private int skillPoints;
    private int strengthPoints;
    private int progressPoints;
    
    // Text and protection
    private String cardText;
    private String protectionOrRemoval;
    
    // Region-specific (for Region cards)
    private int diceRoll;  // Production die face (1-6)
    private int storedResources;  // 0-3 resources stored
    
    // Effect strategy
    private CardEffect effect;
    
    /**
     * Constructor with name.
     * 
     * @param name The card name
     */
    public Card(String name) {
        this.name = name;
        this.cost = new EnumMap<>(ResourceType.class);
        this.cardType = CardType.UNKNOWN;
        this.effect = new DefaultCardEffect();
    }
    
    /**
     * Applies this card's effect.
     * Uses Strategy pattern for polymorphic behavior.
     * 
     * @param activePlayer The player playing the card
     * @param opponent The opponent
     * @param row Target row (-1 for actions)
     * @param col Target column (-1 for actions)
     * @return True if successfully applied
     */
    public boolean applyEffect(Player activePlayer, Player opponent, int row, int col) {
        if (effect == null) {
            effect = new DefaultCardEffect();
        }
        return effect.apply(this, activePlayer, opponent, row, col);
    }
    
    /**
     * Checks if this card's effect can be applied.
     * 
     * @param activePlayer The player attempting to play
     * @param opponent The opponent
     * @param row Target row
     * @param col Target column
     * @return True if can be applied
     */
    public boolean canApplyEffect(Player activePlayer, Player opponent, int row, int col) {
        if (effect == null) {
            effect = new DefaultCardEffect();
        }
        return effect.canApply(this, activePlayer, opponent, row, col);
    }
    
    /**
     * For regions: adds one resource (up to max 3).
     * 
     * @return True if resource was added
     */
    public boolean addResource() {
        if (cardType == CardType.REGION && storedResources < 3) {
            storedResources++;
            return true;
        }
        return false;
    }
    
    /**
     * For regions: removes one resource (down to min 0).
     * 
     * @return True if resource was removed
     */
    public boolean removeResource() {
        if (cardType == CardType.REGION && storedResources > 0) {
            storedResources--;
            return true;
        }
        return false;
    }
    
    /**
     * For regions: gets the resource type produced.
     * 
     * @return The ResourceType, or null if not a region
     */
    public ResourceType getProducedResource() {
        if (cardType != CardType.REGION) {
            return null;
        }
        
        // Map region name to resource
        for (ResourceType type : ResourceType.values()) {
            if (type.getRegionName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        
        return null;
    }
    
    /**
     * Gets total cost as a formatted string.
     * 
     * @return Cost string (e.g., "2 Brick, 1 Lumber")
     */
    public String getCostString() {
        if (cost == null || cost.isEmpty()) {
            return "Free";
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(entry.getValue()).append(" ").append(entry.getKey().getDisplayName());
        }
        
        return sb.toString();
    }
    
    @Override
    public int compareTo(Card other) {
        if (other == null) {
            return 1;
        }
        return this.name.compareToIgnoreCase(other.name);
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Card)) return false;
        Card other = (Card) obj;
        return name != null && name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
    
    // Getters and Setters
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getGermanName() { return germanName; }
    public void setGermanName(String germanName) { this.germanName = germanName; }
    
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    
    public CardType getCardType() { return cardType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }
    
    public String getPlacement() { return placement; }
    public void setPlacement(String placement) { this.placement = placement; }
    
    public String getOneOf() { return oneOf; }
    public void setOneOf(String oneOf) { this.oneOf = oneOf; }
    
    public String getRequires() { return requires; }
    public void setRequires(String requires) { this.requires = requires; }
    
    public Map<ResourceType, Integer> getCost() { return cost; }
    public void setCost(Map<ResourceType, Integer> cost) { this.cost = cost; }
    
    public int getVictoryPoints() { return victoryPoints; }
    public void setVictoryPoints(int victoryPoints) { this.victoryPoints = victoryPoints; }
    
    public int getCommercePoints() { return commercePoints; }
    public void setCommercePoints(int commercePoints) { this.commercePoints = commercePoints; }
    
    public int getSkillPoints() { return skillPoints; }
    public void setSkillPoints(int skillPoints) { this.skillPoints = skillPoints; }
    
    public int getStrengthPoints() { return strengthPoints; }
    public void setStrengthPoints(int strengthPoints) { this.strengthPoints = strengthPoints; }
    
    public int getProgressPoints() { return progressPoints; }
    public void setProgressPoints(int progressPoints) { this.progressPoints = progressPoints; }
    
    public String getCardText() { return cardText; }
    public void setCardText(String cardText) { this.cardText = cardText; }
    
    public String getProtectionOrRemoval() { return protectionOrRemoval; }
    public void setProtectionOrRemoval(String protectionOrRemoval) { 
        this.protectionOrRemoval = protectionOrRemoval; 
    }
    
    public int getDiceRoll() { return diceRoll; }
    public void setDiceRoll(int diceRoll) { this.diceRoll = diceRoll; }
    
    public int getStoredResources() { return storedResources; }
    public void setStoredResources(int storedResources) { 
        this.storedResources = Math.max(0, Math.min(3, storedResources)); 
    }
    
    public CardEffect getEffect() { return effect; }
    public void setEffect(CardEffect effect) { this.effect = effect; }
}
