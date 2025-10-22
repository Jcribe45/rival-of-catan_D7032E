package com.catan.rivals.player.action;

import com.catan.rivals.player.Player;
import com.catan.rivals.model.Deck;

/**
 * Handles ending the turn.
 */
public class EndAction implements PlayerAction {
    
    @Override
    public boolean execute(Player player, Player opponent, Deck deck) {
        return true; // Signal to end action phase
    }
    
    @Override
    public String getCommandName() {
        return "END";
    }
    
    @Override
    public String getUsageHelp() {
        return "END - End your turn";
    }
}