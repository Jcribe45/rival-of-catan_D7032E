package com.catan.rivals.test;

import com.catan.rivals.game.*;
import com.catan.rivals.model.*;
import com.catan.rivals.player.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

/**
 * Unit tests for GameEngine.
 * 
 * Tests cover:
 * - Game initialization
 * - Player management
 * - Dice rolling
 * - Victory conditions
 */
public class GameEngineTest {
    
    private GameEngine engine;
    private HumanPlayer player1;
    private HumanPlayer player2;
    
    @BeforeEach
    public void setUp() {
        engine = new GameEngine();
        player1 = new HumanPlayer("Test Player 1");
        player2 = new HumanPlayer("Test Player 2");
    }
    
    @Test
    @DisplayName("Game engine creation")
    public void testGameEngineCreation() {
        assertNotNull(engine);
    }
    
    @Test
    @DisplayName("Adding players")
    public void testAddingPlayers() {
        engine.addPlayer(player1);
        engine.addPlayer(player2);
        
        // Should have 2 players (verified through initialization)
        assertDoesNotThrow(() -> engine.initialize("cards.json"));
    }
    
    @Test
    @DisplayName("Game initialization")
    public void testGameInitialization() {
        engine.addPlayer(player1);
        engine.addPlayer(player2);
        
        try {
            engine.initialize("cards.json");
            
            // Verify players have starting principalities
            assertNotNull(player1.getPrincipality());
            assertNotNull(player2.getPrincipality());
            
            // Verify players have initial resources
            assertTrue(player1.getResourceBank().getTotalResources() > 0);
            assertTrue(player2.getResourceBank().getTotalResources() > 0);
            
            // Verify players have initial victory points (2 settlements = 2 VP)
            assertEquals(2, player1.getVictoryPoints());
            assertEquals(2, player2.getVictoryPoints());
            
        } catch (IOException e) {
            fail("Failed to initialize game: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Dice rolling")
    public void testDiceRolling() {
        Dice dice = new Dice();
        
        int production = dice.rollProduction();
        assertTrue(production >= 1 && production <= 6, "Production die should be 1-6");
        
        int event = dice.rollEvent();
        assertTrue(event >= 1 && event <= 6, "Event die should be 1-6");
    }
    
    @Test
    @DisplayName("Dice with seed for deterministic testing")
    public void testDiceWithSeed() {
        Dice dice1 = new Dice(12345);
        Dice dice2 = new Dice(12345);
        
        int roll1a = dice1.rollProduction();
        int roll2a = dice2.rollProduction();
        
        assertEquals(roll1a, roll2a, "Same seed should produce same rolls");
    }
    
    @Test
    @DisplayName("Victory condition - introductory game")
    public void testVictoryConditionIntro() {
        VictoryCondition vc = VictoryCondition.introductoryGame();
        assertEquals(7, vc.getRequiredPoints());
        
        player1.setVictoryPoints(7);
        assertTrue(vc.hasWon(player1, player2));
        
        player1.setVictoryPoints(6);
        assertFalse(vc.hasWon(player1, player2));
    }
    
    @Test
    @DisplayName("Victory condition - trade advantage")
    public void testTradeAdvantage() {
        VictoryCondition vc = VictoryCondition.introductoryGame();
        
        player1.setVictoryPoints(6);
        player1.setCommercePoints(5);
        player2.setCommercePoints(2);
        
        // 6 base VP + 1 for trade advantage = 7 total
        assertTrue(vc.hasWon(player1, player2));
    }
    
    @Test
    @DisplayName("Victory condition - strength advantage")
    public void testStrengthAdvantage() {
        VictoryCondition vc = VictoryCondition.introductoryGame();
        
        player1.setVictoryPoints(6);
        player1.setStrengthPoints(6);
        player2.setStrengthPoints(3);
        
        // 6 base VP + 1 for strength advantage = 7 total
        assertTrue(vc.hasWon(player1, player2));
    }
    
    @Test
    @DisplayName("Victory condition - both advantages")
    public void testBothAdvantages() {
        VictoryCondition vc = VictoryCondition.introductoryGame();
        
        player1.setVictoryPoints(5);
        player1.setCommercePoints(5);
        player1.setStrengthPoints(5);
        player2.setCommercePoints(2);
        player2.setStrengthPoints(2);
        
        // 5 base VP + 1 trade + 1 strength = 7 total
        assertTrue(vc.hasWon(player1, player2));
        
        int total = vc.calculateTotalVictoryPoints(player1, player2);
        assertEquals(7, total);
    }
    
    @Test
    @DisplayName("Game phase transitions")
    public void testGamePhases() {
        GamePhase phase = GamePhase.SETUP;
        
        assertEquals(GamePhase.ROLL_DICE, phase.next());
        
        phase = GamePhase.ROLL_DICE;
        assertEquals(GamePhase.PRODUCTION, phase.next());
        
        phase = GamePhase.PRODUCTION;
        assertEquals(GamePhase.EVENT, phase.next());
    }
    
    @Test
    @DisplayName("Event die descriptions")
    public void testEventDescriptions() {
        assertEquals("Brigand Attack", Dice.getEventDescription(Dice.EVENT_BRIGAND));
        assertEquals("Trade", Dice.getEventDescription(Dice.EVENT_TRADE));
        assertEquals("Celebration", Dice.getEventDescription(Dice.EVENT_CELEBRATION));
        assertEquals("Plentiful Harvest", Dice.getEventDescription(Dice.EVENT_PLENTIFUL_HARVEST));
        assertEquals("Event Card", Dice.getEventDescription(Dice.EVENT_CARD_A));
        assertEquals("Event Card", Dice.getEventDescription(Dice.EVENT_CARD_B));
    }
    
    @Test
    @DisplayName("Player resource bank operations")
    public void testResourceBankOperations() {
        // Setup player with a region
        Principality prin = player1.getPrincipality();
        Card forest = new Card("Forest");
        forest.setCardType(CardType.REGION);
        forest.setStoredResources(2);
        prin.placeCard(1, 0, forest);
        
        ResourceBank bank = player1.getResourceBank();
        
        // Test counting
        assertEquals(2, bank.getResourceCount(ResourceType.LUMBER));
        assertEquals(2, bank.getTotalResources());
        
        // Test adding
        bank.addResources(ResourceType.LUMBER, 1);
        assertEquals(3, bank.getResourceCount(ResourceType.LUMBER));
        
        // Test removing
        assertTrue(bank.removeResources(ResourceType.LUMBER, 2));
        assertEquals(1, bank.getResourceCount(ResourceType.LUMBER));
        
        // Test cannot remove more than available
        assertFalse(bank.removeResources(ResourceType.LUMBER, 5));
    }
}
