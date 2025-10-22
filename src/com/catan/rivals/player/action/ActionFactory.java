package com.catan.rivals.player.action;

import java.util.*;

/**
 * Factory for creating player actions.
 * Uses Factory pattern to instantiate appropriate action handlers.
 */
public class ActionFactory {
    
    private static final Map<String, PlayerAction> actions = new HashMap<>();
    
    static {
        registerAction(new PlayCardAction());
        registerAction(new BuildAction());
        registerAction(new TradeAction());
        registerAction(new ViewAction());
        registerAction(new EndAction());
    }
    
    private static void registerAction(PlayerAction action) {
        actions.put(action.getCommandName(), action);
    }
    
    /**
     * Gets an action by command name.
     * 
     * @param command The command (e.g., "PLAY", "BUILD")
     * @return The action, or null if not found
     */
    public static PlayerAction getAction(String command) {
        return actions.get(command.toUpperCase());
    }
    
    /**
     * Gets all available actions.
     * 
     * @return Collection of all actions
     */
    public static Collection<PlayerAction> getAllActions() {
        return actions.values();
    }
    
    /**
     * Gets help text for all actions.
     * 
     * @return Formatted help string
     */
    public static String getActionHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Available Actions ===\n");
        
        for (PlayerAction action : actions.values()) {
            sb.append("  ").append(action.getUsageHelp()).append("\n");
        }
        
        return sb.toString();
    }
}