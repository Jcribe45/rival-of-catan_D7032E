package com.catan.rivals.player.ui;

import com.catan.rivals.player.Player;
import com.catan.rivals.model.*;
import com.catan.rivals.util.PrincipalityRenderer;
import java.util.Map;

/**
 * Handles all UI display logic for a player.
 * Facade pattern - simplifies player UI operations.
 * 
 * SOLID: Single Responsibility - only handles display
 */
public class PlayerUI {
    
    private Player player;
    
    public PlayerUI(Player player) {
        this.player = player;
    }
    
    /**
     * Displays both player's and opponent's boards.
     */
    public void displayBothBoards(Player opponent) {
        player.sendMessage(PrincipalityRenderer.renderBothPrincipalities(player, opponent));
    }
    
    /**
     * Displays player's hand with details.
     */
    public void displayHand() {
        player.sendMessage("\n=== Your Hand (" + player.getHand().size() + " cards) ===");
        
        for (int i = 0; i < player.getHand().size(); i++) {
            Card card = player.getHand().get(i);
            player.sendMessage(String.format("[%d] %s", i, formatCardInfo(card)));
        }
        
        player.sendMessage("");
    }
    
    /**
     * Displays player's resources with active effects.
     */
    public void displayResources() {
        player.sendMessage("\n=== Your Resources ===");
        
        Map<ResourceType, Integer> resources = player.getResourceBank().getResourceSummary();
        
        for (ResourceType type : ResourceType.values()) {
            int count = resources.getOrDefault(type, 0);
            player.sendMessage(String.format("  %s: %d", type.getDisplayName(), count));
        }
        
        player.sendMessage("Total: " + player.getResourceBank().getTotalResources());
        
        // Show active effects
        String effects = player.getEffectTracker().getEffectsSummary();
        if (!effects.isEmpty()) {
            player.sendMessage(effects);
        }
    }
    
    /**
     * Displays action menu.
     */
    public void displayActionMenu() {
        player.sendMessage("\n=== Action Phase ===");
        player.sendMessage("PLAY - Play card from hand");
        player.sendMessage("BUILD - Build Road/Settlement/City");
        player.sendMessage("TRADE - Trade with bank");
        player.sendMessage("VIEW - View boards again");
        player.sendMessage("END - End turn");
        player.sendMessage("");
    }
    
    /**
     * Displays complete game state.
     */
    public void displayGameState(Player opponent) {
        displayBothBoards(opponent);
        displayHand();
        displayResources();
    }
    
    /**
     * Formats card information for display.
     */
    private String formatCardInfo(Card card) {
        return String.format("%s (Cost: %s, Type: %s)",
            card.getName(),
            card.getCostString(),
            card.getCardType().getDisplayName());
    }
}