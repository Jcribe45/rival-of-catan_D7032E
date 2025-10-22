package com.catan.rivals.player.action;

import com.catan.rivals.player.Player;
import com.catan.rivals.model.*;
import com.catan.rivals.util.TradingHelper;

/**
 * Handles trading with the bank.
 * Uses TradingHelper to check for special trade rates.
 */
public class TradeAction implements PlayerAction {
    
    @Override
    public boolean execute(Player player, Player opponent, Deck deck) {
        return TradingHelper.promptAndExecuteTrade(player);
    }
    
    @Override
    public String getCommandName() {
        return "TRADE";
    }
    
    @Override
    public String getUsageHelp() {
        return "TRADE - Trade resources with the bank";
    }
}