package com.catan.rivals.game.event;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;
import java.util.List;

public class PlentifulHarvestEventHandler implements GameEventHandler {
    
    private List<Player> allPlayers;
    
    public PlentifulHarvestEventHandler(List<Player> allPlayers) {
        this.allPlayers = allPlayers;
    }
    
    @Override
    public void handleEvent(Player activePlayer, Player opponent) {
        for (Player player : allPlayers) {
            player.sendMessage("Plentiful Harvest! Choose 1 resource:");
            String input = player.receiveInput();
            ResourceType type = ResourceType.fromString(input);
            
            if (type != null) {
                player.getResourceBank().addResources(type, 1);
            }
        }
    }
    
    @Override
    public String getEventName() {
        return "Plentiful Harvest";
    }
}