package com.catan.rivals.game.phase;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;
import java.util.List;

/**
 * Handles the exchange phase of a turn.
 * Players can exchange cards from hand with draw stacks.
 * 
 * Design Pattern: Strategy Pattern - one strategy for exchange phase
 * SOLID: Single Responsibility - handles only exchange logic
 */
public class ExchangePhaseHandler {
    
    private Deck deck;
    
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
     * Rules:
     * - Player can exchange up to 2 cards per turn
     * - Each exchange costs 1 resource per card
     * - Player chooses card to discard and which stack to draw from
     * 
     * @param player The player exchanging cards
     */
    public void executeExchange(Player player) {
        int maxExchanges = player.getFlags().contains("ODIN_FOUNTAIN") ? 2 : 1;
        int exchangesMade = 0;
        
        while (exchangesMade < maxExchanges) {
            player.sendMessage("\n=== Exchange Phase ===");
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
        List<Card> hand = player.getHand();
        
        if (hand.isEmpty()) {
            player.sendMessage("No cards to exchange!");
            return false;
        }
        
        // Check if player has resources to pay for exchange
        boolean hasFreeExchange = player.getFlags().contains("TOWN_HALL");
        boolean hasParishHall = player.getFlags().contains("PARISH_HALL");
        
        if (!hasFreeExchange && !hasParishHall) {
            // Standard exchange: costs 1 resource
            if (player.getResourceBank().getTotalResources() < 1) {
                player.sendMessage("Not enough resources to exchange (need 1 resource)");
                return false;
            }
        }
        
        // Show hand
        player.sendMessage("\n=== Your Hand ===");
        for (int i = 0; i < hand.size(); i++) {
            player.sendMessage(String.format("[%d] %s", i, hand.get(i).getName()));
        }
        
        // Choose card to discard
        player.sendMessage("\nChoose card to exchange (0-" + (hand.size() - 1) + ") or 'C' to cancel:");
        String input = player.receiveInput();
        
        if (input.trim().toUpperCase().equals("C")) {
            return false;
        }
        
        int cardIndex;
        try {
            cardIndex = Integer.parseInt(input.trim());
            if (cardIndex < 0 || cardIndex >= hand.size()) {
                player.sendMessage("Invalid card index!");
                return false;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid input!");
            return false;
        }
        
        // Choose draw stack
        player.sendMessage("\nChoose draw stack (1-4):");
        String stackInput = player.receiveInput();
        
        int stackNum;
        try {
            stackNum = Integer.parseInt(stackInput.trim());
            if (stackNum < 1 || stackNum > 4) {
                player.sendMessage("Invalid stack number!");
                return false;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid input!");
            return false;
        }
        
        // Check if stack is empty
        Card newCard = deck.drawFromStack(stackNum);
        if (newCard == null) {
            player.sendMessage("Stack " + stackNum + " is empty!");
            return false;
        }
        
        // Pay cost if required
        if (!hasFreeExchange) {
            if (hasParishHall) {
                // Parish Hall: reduced cost
                if (!payExchangeCost(player, 1)) {
                    // Return card if can't pay
                    returnCardToStack(newCard, stackNum);
                    return false;
                }
            } else {
                // Standard: choose any resource
                if (!payExchangeCost(player, 1)) {
                    returnCardToStack(newCard, stackNum);
                    return false;
                }
            }
        }
        
        // Perform exchange
        Card discarded = hand.remove(cardIndex);
        hand.add(newCard);
        
        player.sendMessage("Exchanged: " + discarded.getName() + " → " + newCard.getName());
        
        return true;
    }
    
    /**
     * Pays the cost for an exchange.
     * 
     * @param player The player paying
     * @param cost The cost in resources
     * @return True if paid successfully
     */
    private boolean payExchangeCost(Player player, int cost) {
        if (cost <= 0) {
            return true;
        }
        
        player.sendMessage("Exchange cost: " + cost + " resource");
        player.sendMessage("Choose resource to pay (Brick/Grain/Lumber/Wool/Ore/Gold):");
        String resourceInput = player.receiveInput();
        
        ResourceType type = ResourceType.fromString(resourceInput);
        if (type == null) {
            player.sendMessage("Invalid resource type!");
            return false;
        }
        
        if (player.getResourceBank().getResourceCount(type) < cost) {
            player.sendMessage("Not enough " + type.getDisplayName() + "!");
            return false;
        }
        
        return player.getResourceBank().removeResources(type, cost);
    }
    
    /**
     * Returns a card to a draw stack (if exchange fails).
     * 
     * @param card The card to return
     * @param stackNum The stack number
     */
    private void returnCardToStack(Card card, int stackNum) {
        List<Card> stack = deck.getDrawStack(stackNum);
        if (stack != null) {
            stack.add(0, card); // Return to top
        }
    }
    
    /**
     * Checks if player can perform exchanges.
     * 
     * @param player The player
     * @return True if exchanges are possible
     */
    public boolean canExchange(Player player) {
        // Need cards in hand
        if (player.getHand().isEmpty()) {
            return false;
        }
        
        // Need resources unless has free exchange
        if (player.getFlags().contains("TOWN_HALL")) {
            return true; // Free exchanges
        }
        
        return player.getResourceBank().getTotalResources() >= 1;
    }
}
