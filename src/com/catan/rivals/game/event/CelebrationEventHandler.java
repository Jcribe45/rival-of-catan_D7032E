package com.catan.rivals.game.event;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;
import java.util.List;

public class CelebrationEventHandler implements GameEventHandler {
    
    private List<Player> allPlayers;
    
    public CelebrationEventHandler(List<Player> allPlayers) {
        this.allPlayers = allPlayers;
    }
    
    @Override
    public void handleEvent(Player activePlayer, Player opponent) {
        int skill0 = allPlayers.get(0).getSkillPoints();
        int skill1 = allPlayers.get(1).getSkillPoints();
        
        if (skill0 == skill1) {
            // Tie: both players gain 1 resource
            for (Player player : allPlayers) {
                player.sendMessage("Celebration (tie)! Choose 1 resource:");
                String input = player.receiveInput();
                ResourceType type = ResourceType.fromString(input);
                
                if (type != null) {
                    player.getResourceBank().addResources(type, 1);
                }
            }
        } else {
            // Winner gets 1 resource
            Player winner = skill0 > skill1 ? allPlayers.get(0) : allPlayers.get(1);
            winner.sendMessage("Celebration (most skill)! Choose 1 resource:");
            String input = winner.receiveInput();
            ResourceType type = ResourceType.fromString(input);
            
            if (type != null) {
                winner.getResourceBank().addResources(type, 1);
            }
        }
    }
    
    @Override
    public String getEventName() {
        return "Celebration";
    }
}