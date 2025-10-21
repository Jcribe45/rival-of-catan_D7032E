package com.catan.rivals.util;

import com.catan.rivals.model.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Factory for creating Card instances from JSON data.
 * 
 * Design Pattern: Factory Method Pattern
 * SOLID: Single Responsibility - creates cards only
 * SOLID: Open-Closed - easily extensible for new card types
 * Booch: High cohesion - all card creation logic in one place
 */
public class CardFactory {
    
    private static final Map<String, ResourceType> RESOURCE_MAP = new HashMap<>();
    
    static {
        RESOURCE_MAP.put("Brick", ResourceType.BRICK);
        RESOURCE_MAP.put("Grain", ResourceType.GRAIN);
        RESOURCE_MAP.put("Lumber", ResourceType.LUMBER);
        RESOURCE_MAP.put("Wool", ResourceType.WOOL);
        RESOURCE_MAP.put("Ore", ResourceType.ORE);
        RESOURCE_MAP.put("Gold", ResourceType.GOLD);
    }
    
    /**
     * Loads all cards from a JSON file.
     * 
     * @param jsonPath Path to the cards.json file
     * @return A Deck containing all loaded cards
     * @throws IOException If file cannot be read
     */
    public static Deck loadCardsFromJson(String jsonPath) throws IOException {
        Deck deck = new Deck();
        
        try (FileReader reader = new FileReader(jsonPath)) {
            JsonElement root = JsonParser.parseReader(reader);
            
            if (!root.isJsonArray()) {
                throw new IOException("Invalid JSON: Expected array at root");
            }
            
            JsonArray cardArray = root.getAsJsonArray();
            
            for (JsonElement element : cardArray) {
                if (!element.isJsonObject()) {
                    continue;
                }
                
                JsonObject cardJson = element.getAsJsonObject();
                String theme = JsonUtils.getString(cardJson, "theme");
                
                // For introductory game, only load "Basic" theme cards
                if (theme == null || !theme.toLowerCase().contains("basic")) {
                    continue;
                }
                
                int number = JsonUtils.getInt(cardJson, "number", 1);
                
                // Create multiple instances if specified
                for (int i = 0; i < number; i++) {
                    Card card = createCard(cardJson);
                    if (card != null) {
                        deck.addCard(card);
                    }
                }
            }
        }
        
        return deck;
    }
    
    /**
     * Creates a single Card instance from JSON data.
     * 
     * @param json The JSON object representing the card
     * @return A Card instance, or null if creation fails
     */
    private static Card createCard(JsonObject json) {
        String name = JsonUtils.getString(json, "name");
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        Card card = new Card(name);
        
        // Basic properties
        card.setGermanName(JsonUtils.getString(json, "germanName"));
        card.setTheme(JsonUtils.getString(json, "theme"));
        card.setCardText(JsonUtils.getString(json, "cardText"));
        
        // Card type
        String typeStr = JsonUtils.getString(json, "type");
        card.setCardType(parseCardType(typeStr));
        
        // Placement
        card.setPlacement(JsonUtils.getString(json, "placement"));
        card.setOneOf(JsonUtils.getString(json, "oneOf"));
        card.setRequires(JsonUtils.getString(json, "Requires"));
        
        // Cost
        card.setCost(parseCost(JsonUtils.getString(json, "cost")));
        
        // Points
        card.setVictoryPoints(JsonUtils.parseIntSafe(JsonUtils.getString(json, "victoryPoints"), 0));
        card.setCommercePoints(JsonUtils.parseIntSafe(JsonUtils.getString(json, "CP"), 0));
        card.setSkillPoints(JsonUtils.parseIntSafe(JsonUtils.getString(json, "SP"), 0));
        card.setStrengthPoints(JsonUtils.parseIntSafe(JsonUtils.getString(json, "FP"), 0));
        card.setProgressPoints(JsonUtils.parseIntSafe(JsonUtils.getString(json, "PP"), 0));
        
        // Protection/Removal
        card.setProtectionOrRemoval(JsonUtils.getString(json, "protectionOrRemoval"));
        
        // Assign effect strategy based on card type and name
        card.setEffect(createEffectStrategy(card));
        
        return card;
    }
    
    /**
     * Parses a CardType from a string.
     * 
     * @param typeStr The type string
     * @return The corresponding CardType
     */
    private static CardType parseCardType(String typeStr) {
        if (typeStr == null) {
            return CardType.UNKNOWN;
        }
        
        String lower = typeStr.toLowerCase();
        
        if (lower.contains("region")) {
            return CardType.REGION;
        } else if (lower.contains("building")) {
            return CardType.BUILDING;
        } else if (lower.contains("unit")) {
            if (lower.contains("hero")) {
                return CardType.HERO;
            } else if (lower.contains("ship")) {
                return CardType.SHIP;
            }
            return CardType.UNIT;
        } else if (lower.contains("action")) {
            if (lower.contains("attack")) {
                return CardType.ACTION_ATTACK;
            } else if (lower.contains("neutral")) {
                return CardType.ACTION_NEUTRAL;
            }
            return CardType.ACTION;
        } else if (lower.contains("event")) {
            return CardType.EVENT;
        } else if (lower.equalsIgnoreCase("center card")) {
            return CardType.CENTER_CARD;
        }
        
        return CardType.UNKNOWN;
    }
    
    /**
     * Parses a cost string into a resource map.
     * 
     * @param costStr The cost string (e.g., "BBL" = 2 Brick, 1 Lumber)
     * @return A map of ResourceType to quantity
     */
    private static Map<ResourceType, Integer> parseCost(String costStr) {
        Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        
        if (costStr == null || costStr.trim().isEmpty()) {
            return cost;
        }
        
        // Parse single-letter format: B=Brick, G=Grain, L=Lumber, W=Wool, O=Ore, A=Gold
        for (char ch : costStr.toCharArray()) {
            if (Character.isWhitespace(ch) || ch == ',' || ch == ';' || ch == '+') {
                continue;
            }
            
            ResourceType type = letterToResourceType(ch);
            if (type != null) {
                cost.put(type, cost.getOrDefault(type, 0) + 1);
            }
        }
        
        return cost;
    }
    
    /**
     * Converts a single letter to a ResourceType.
     * 
     * @param ch The letter (B, G, L, W, O, A)
     * @return The corresponding ResourceType, or null if invalid
     */
    private static ResourceType letterToResourceType(char ch) {
        switch (Character.toUpperCase(ch)) {
            case 'B': return ResourceType.BRICK;
            case 'G': return ResourceType.GRAIN;
            case 'L': return ResourceType.LUMBER;
            case 'W': return ResourceType.WOOL;
            case 'O': return ResourceType.ORE;
            case 'A': return ResourceType.GOLD;
            default: return null;
        }
    }
    
    /**
     * Creates an appropriate CardEffect strategy based on the card.
     * Uses Strategy Pattern to assign behavior.
     * 
     * @param card The card to create an effect for
     * @return A CardEffect strategy
     */
    private static CardEffect createEffectStrategy(Card card) {
        // This will be expanded with specific effect implementations
        // For now, returns a default effect
        return new DefaultCardEffect();
    }
}
