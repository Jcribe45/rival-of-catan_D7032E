package com.catan.rivals.game.phase;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;
import com.catan.rivals.util.ResourceTransactionHelper;
import java.util.List;

/**
 * Handles the exchange phase of a turn.
 * Players can exchange cards from hand with draw stacks.
 * 
 * REFACTORED: Delegates to Player for UI, Deck for card management,
 * and ResourceTransactionHelper for payments.
 * 
 * Design Pattern: Strategy Pattern + Facade Pattern
 * SOLID: Single Responsibility - orchestrates exchange flow only
 */
public class ExchangePhaseHandler {
    
    private Deck deck;
    
    // Exchange costs
    private static final int FREE_EXCHANGE_COST = 0;
    private static final int PAID_EXCHANGE_COST = 2;
    private static final int PARISH_HALL_COST = 1;
    
    /**
     * Constructor.
     * 
     * @param deck The game deck
     */
    public ExchangePhaseHandler(Deck deck) {
        this.deck = deck;
    }
    
    /**
     * Executes the exchange phase for a player.
     * 
     * @param player The player exchanging cards
     */
    public void executeExchange(Player player) {
        int maxExchanges = player.getFlags().contains("ODIN_FOUNTAIN") ? 2 : 1;
        int exchangesMade = 0;
        
        while (exchangesMade < maxExchanges) {
            player.sendMessage("=== Exchange Phase ===");
            player.sendMessage("Exchanges available: " + (maxExchanges - exchangesMade));
            player.sendMessage("Exchange a card? (Y/N)");
            
            String response = player.receiveInput();
            
            if (response == null || !response.trim().toUpperCase().startsWith("Y")) {
                break;
            }
            
            if (performExchange(player)) {
                exchangesMade++;
                player.sendMessage("Exchange successful! (" + exchangesMade + "/" + maxExchanges + ")");
            } else {
                player.sendMessage("Exchange failed or cancelled.");
                break;
            }
        }
        
        if (exchangesMade > 0) {
            player.sendMessage("Exchange phase complete. " + exchangesMade + " card(s) exchanged.");
        }
    }
    
    /**
     * Performs a single card exchange.
     * 
     * @param player The player
     * @return True if exchange was successful
     */
    private boolean performExchange(Player player) {
        if (player.getHand().isEmpty()) {
            player.sendMessage("No cards to exchange!");
            return false;
        }
        
        // Step 1: Choose exchange type
        int cost = getExchangeCost(player);
        boolean canChoose = (cost > 0 || player.getFlags().contains("TOWN_HALL"));
        
        if (cost < 0) {
            return false; // Cancelled
        }
        
        // Step 2: Choose card from hand
        int cardIndex = player.chooseCardFromHand("Choose card to exchange", true);
        if (cardIndex < 0) {
            return false;
        }
        
        // Step 3: Choose draw stack
        int stackNum = player.chooseDrawStack(deck, "Choose draw stack", true);
        if (stackNum < 0) {
            return false;
        }
        
        // Step 4: Pay cost
        ResourceType paidType = null;
        if (cost > 0) {
            paidType = ResourceTransactionHelper.payResourceCost(player, cost, "Exchange");
            if (paidType == null) {
                return false; // Cannot afford
            }
        }
        
        // Step 5: Draw new card
        Card newCard;
        if (canChoose) {
            List<Card> stack = deck.getDrawStack(stackNum);
            newCard = player.chooseCardFromStack(stack, "Choose card from stack", true);
        } else {
            newCard = deck.drawFromStack(stackNum);
        }
        
        if (newCard == null) {
            player.sendMessage("Failed to draw card!");
            if (paidType != null && cost > 0) {
                ResourceTransactionHelper.refundResources(player, paidType, cost);
            }
            return false;
        }
        
        // Step 6: Complete exchange
        List<Card> hand = player.getHand();
        Card discarded = hand.remove(cardIndex);
        hand.add(newCard);
        deck.returnCardToStackBottom(discarded, stackNum);
        
        player.sendMessage("Exchanged: " + discarded.getName() + " → " + newCard.getName());
        
        return true;
    }
    
    /**
     * Determines exchange cost based on player's choice and buildings.
     * 
     * @param player The player
     * @return Cost (0 for free, >0 for paid), or -1 if cancelled
     */
    private int getExchangeCost(Player player) {
        boolean hasTownHall = player.getFlags().contains("TOWN_HALL");
        boolean hasParishHall = player.getFlags().contains("PARISH_HALL");
        
        // Town Hall: Paid exchange is free
        if (hasTownHall) {
            player.sendMessage("Town Hall: You can view and choose from entire stack for free!");
            return FREE_EXCHANGE_COST;
        }
        
        int paidCost = hasParishHall ? PARISH_HALL_COST : PAID_EXCHANGE_COST;
        
        player.sendMessage("Exchange Options:");
        player.sendMessage("1. FREE: Draw random card from top of stack");
        player.sendMessage("2. PAID (" + paidCost + " resource" + (paidCost > 1 ? "s" : "") + 
                          "): View entire stack and choose any card");
        player.sendMessage("Choose option (1/2) or 'C' to cancel:");
        
        String input = player.receiveInput();
        
        if (input.trim().toUpperCase().equals("C")) {
            return -1;
        }
        
        if (input.trim().equals("1")) {
            return FREE_EXCHANGE_COST;
        } else if (input.trim().equals("2")) {
            if (player.getResourceBank().getTotalResources() < paidCost) {
                player.sendMessage("Not enough resources! (Need " + paidCost + ")");
                return -1;
            }
            return paidCost;
        } else {
            player.sendMessage("Invalid option!");
            return -1;
        }
    }
}