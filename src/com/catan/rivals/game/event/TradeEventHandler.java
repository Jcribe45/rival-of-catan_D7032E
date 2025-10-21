package com.catan.rivals.game.event;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;
import java.util.List;

public class TradeEventHandler implements GameEventHandler {
    
    private List<Player> allPlayers;
    private VictoryCondition victoryCondition;
    
    public TradeEventHandler(List<Player> allPlayers) {
        this.allPlayers = allPlayers;
        this.victoryCondition = VictoryCondition.introductoryGame();
    }
    
    @Override
    public void handleEvent(Player activePlayer, Player opponent) {
        for (Player player : allPlayers) {
            Player other = player == allPlayers.get(0) ? 
                allPlayers.get(1) : allPlayers.get(0);
            
            if (victoryCondition.hasTradeAdvantage(player, other)) {
                player.sendMessage("Trade advantage! Choose 1 resource:");
                String input = player.receiveInput();
                ResourceType type = ResourceType.fromString(input);
                
                if (type != null) {
                    player.getResourceBank().addResources(type, 1);
                }
            }
        }
    }
    
    @Override
    public String getEventName() {
        return "Trade";
    }
}