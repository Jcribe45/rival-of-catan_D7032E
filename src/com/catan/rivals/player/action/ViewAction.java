package com.catan.rivals.player.action;

import com.catan.rivals.player.Player;
import com.catan.rivals.model.Deck;

/**
 * Handles viewing the game state again.
 */
public class ViewAction implements PlayerAction {
    
    @Override
    public boolean execute(Player player, Player opponent, Deck deck) {
        // Just redisplay - handled by main loop
        return false;
    }
    
    @Override
    public String getCommandName() {
        return "VIEW";
    }
    
    @Override
    public String getUsageHelp() {
        return "VIEW - Display game state again";
    }
}