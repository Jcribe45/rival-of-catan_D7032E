package com.catan.rivals.test;

import com.catan.rivals.model.*;
import com.catan.rivals.util.CardFactory;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;

/**
 * Unit tests for Card and related classes.
 * 
 * Tests cover:
 * - Card creation
 * - Resource management
 * - Card effects
 * - Cost validation
 */
public class CardTest {
    
    private Card testCard;
    
    @BeforeEach
    public void setUp() {
        testCard = new Card("Test Card");
    }
    
    @Test
    @DisplayName("Card creation with name")
    public void testCardCreation() {
        assertNotNull(testCard);
        assertEquals("Test Card", testCard.getName());
    }
    
    @Test
    @DisplayName("Card type assignment")
    public void testCardType() {
        testCard.setCardType(CardType.BUILDING);
        assertEquals(CardType.BUILDING, testCard.getCardType());
    }
    
    @Test
    @DisplayName("Resource production for regions")
    public void testRegionResourceProduction() {
        Card region = new Card("Forest");
        region.setCardType(CardType.REGION);
        region.setDiceRoll(3);
        region.setStoredResources(0);
        
        // Test adding resources
        assertTrue(region.addResource());
        assertEquals(1, region.getStoredResources());
        
        // Test max capacity (3)
        region.addResource();
        region.addResource();
        assertEquals(3, region.getStoredResources());
        
        // Should not add beyond max
        assertFalse(region.addResource());
        assertEquals(3, region.getStoredResources());
    }
    
    @Test
    @DisplayName("Resource removal from regions")
    public void testResourceRemoval() {
        Card region = new Card("Mountain");
        region.setCardType(CardType.REGION);
        region.setStoredResources(2);
        
        // Test removing resources
        assertTrue(region.removeResource());
        assertEquals(1, region.getStoredResources());
        
        assertTrue(region.removeResource());
        assertEquals(0, region.getStoredResources());
        
        // Should not remove below 0
        assertFalse(region.removeResource());
        assertEquals(0, region.getStoredResources());
    }
    
    @Test
    @DisplayName("Card cost parsing")
    public void testCardCost() {
        Map<ResourceType, Integer> cost = new java.util.EnumMap<>(ResourceType.class);
        cost.put(ResourceType.BRICK, 2);
        cost.put(ResourceType.LUMBER, 1);
        
        testCard.setCost(cost);
        
        assertEquals(2, testCard.getCost().get(ResourceType.BRICK));
        assertEquals(1, testCard.getCost().get(ResourceType.LUMBER));
    }
    
    @Test
    @DisplayName("Victory points tracking")
    public void testVictoryPoints() {
        testCard.setVictoryPoints(2);
        assertEquals(2, testCard.getVictoryPoints());
        
        testCard.setVictoryPoints(5);
        assertEquals(5, testCard.getVictoryPoints());
    }
    
    @Test
    @DisplayName("Card comparison")
    public void testCardComparison() {
        Card card1 = new Card("Abbey");
        Card card2 = new Card("Brick Factory");
        Card card3 = new Card("Abbey");
        
        assertTrue(card1.compareTo(card2) < 0); // A comes before B
        assertTrue(card2.compareTo(card1) > 0); // B comes after A
        assertEquals(0, card1.compareTo(card3)); // Same name
    }
    
    @Test
    @DisplayName("Load cards from JSON")
    public void testLoadCardsFromJson() {
        try {
            Deck deck = CardFactory.loadCardsFromJson("cards.json");
            
            assertNotNull(deck);
            assertFalse(deck.getRoads().isEmpty(), "Roads should be loaded");
            assertFalse(deck.getSettlements().isEmpty(), "Settlements should be loaded");
            assertFalse(deck.getCities().isEmpty(), "Cities should be loaded");
            assertFalse(deck.getRegions().isEmpty(), "Regions should be loaded");
            
        } catch (IOException e) {
            fail("Failed to load cards.json: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Region dice roll assignment")
    public void testRegionDiceRoll() {
        Card region = new Card("Field");
        region.setCardType(CardType.REGION);
        region.setDiceRoll(4);
        
        assertEquals(4, region.getDiceRoll());
    }
    
    @Test
    @DisplayName("Card effect application")
    public void testCardEffect() {
        Card card = new Card("Test Effect Card");
        card.setCardType(CardType.ACTION);
        
        CardEffect effect = new DefaultCardEffect();
        card.setEffect(effect);
        
        assertNotNull(card.getEffect());
        assertEquals(effect, card.getEffect());
    }
    
    @Test
    @DisplayName("Resource type from region name")
    public void testResourceTypeFromRegion() {
        Card forest = new Card("Forest");
        forest.setCardType(CardType.REGION);
        assertEquals(ResourceType.LUMBER, forest.getProducedResource());
        
        Card mountain = new Card("Mountain");
        mountain.setCardType(CardType.REGION);
        assertEquals(ResourceType.ORE, mountain.getProducedResource());
        
        Card field = new Card("Field");
        field.setCardType(CardType.REGION);
        assertEquals(ResourceType.GRAIN, field.getProducedResource());
    }
}
