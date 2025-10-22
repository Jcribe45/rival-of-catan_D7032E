package com.catan.rivals.player.action;

import com.catan.rivals.player.Player;
import com.catan.rivals.model.Deck;

/**
 * Command pattern for player actions.
 * Each action type implements this interface.
 */
public interface PlayerAction {
    boolean execute(Player player, Player opponent, Deck deck);
    String getCommandName();
    String getUsageHelp();
}