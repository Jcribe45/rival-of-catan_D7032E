package com.catan.rivals.util;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;

/**
 * Handles rendering of Principality boards for display.
 * 
 * Design Pattern: Strategy Pattern (can be extended for different formats)
 * SOLID: Single Responsibility - only handles board rendering
 * SOLID: Open-Closed - can extend for new formats without modifying
 * Separation of Concerns: View logic separated from Model (Principality)
 */
public class PrincipalityRenderer {
    
    /**
     * Renders a principality as an ASCII grid with player stats.
     * 
     * @param player The player whose principality to render
     * @return Formatted string representation
     */
    public static String renderPrincipality(Player player) {
        StringBuilder sb = new StringBuilder();
        Principality prin = player.getPrincipality();
        int rows = prin.getRowCount();
        int cols = prin.getColumnCount();
        
        // Column headers
        sb.append("           ");
        for (int c = 0; c < cols; c++) {
            sb.append(String.format("%-20s ", "Col " + c));
        }
        sb.append("\n");
        
        // Separator
        appendHorizontalSeparator(sb, cols);
        
        // Rows
        for (int r = 0; r < rows; r++) {
            sb.append(String.format("%2d |", r));
            for (int c = 0; c < cols; c++) {
                Card card = prin.getCardAt(r, c);
                String cellContent = formatCard(card);
                sb.append(String.format("%-20s|", cellContent));
            }
            sb.append("\n");
            
            // Separator
            appendHorizontalSeparator(sb, cols);
        }
        
        // Points summary
        sb.append(String.format("\nVP=%d CP=%d SP=%d FP=%d PP=%d\n",
            player.getVictoryPoints(), 
            player.getCommercePoints(), 
            player.getSkillPoints(), 
            player.getStrengthPoints(), 
            player.getProgressPoints()));
        
        return sb.toString();
    }
    
    /**
     * Renders both player's and opponent's principalities side by side.
     * 
     * @param player The viewing player
     * @param opponent The opponent player
     * @return Formatted string with both boards
     */
    public static String renderBothPrincipalities(Player player, Player opponent) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n").append("=".repeat(60)).append("\n");
        sb.append("==================== YOUR PRINCIPALITY =====================\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append(renderPrincipality(player));
        
        sb.append("\n").append("=".repeat(60)).append("\n");
        sb.append("================= OPPONENT'S PRINCIPALITY ==================\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append(renderPrincipality(opponent));
        
        return sb.toString();
    }
    
    /**
     * Renders just the principality grid without player stats.
     * Useful for debugging or partial displays.
     * 
     * @param prin The principality to render
     * @return Formatted grid string
     */
    public static String renderPrincipalityGrid(Principality prin) {
        StringBuilder sb = new StringBuilder();
        int rows = prin.getRowCount();
        int cols = prin.getColumnCount();
        
        // Column headers
        sb.append("           ");
        for (int c = 0; c < cols; c++) {
            sb.append(String.format("%-20s ", "Col " + c));
        }
        sb.append("\n");
        
        // Separator
        appendHorizontalSeparator(sb, cols);
        
        // Rows
        for (int r = 0; r < rows; r++) {
            sb.append(String.format("%2d |", r));
            for (int c = 0; c < cols; c++) {
                Card card = prin.getCardAt(r, c);
                String cellContent = formatCard(card);
                sb.append(String.format("%-20s|", cellContent));
            }
            sb.append("\n");
            
            // Separator
            appendHorizontalSeparator(sb, cols);
        }
        
        return sb.toString();
    }
    
    /**
     * Formats a card for display in a grid cell.
     * 
     * @param card The card to format (may be null)
     * @return Formatted string (max 12 characters)
     */
    private static String formatCard(Card card) {
        if (card == null) {
            return "";
        }
        
        String name = card.getName();
        if (name.length() > 10) {
            name = name.substring(0, 10);
        }
        
        // Add resource info for regions
        if (card.getCardType() == CardType.REGION) {
            int stored = card.getStoredResources();
            int dice = card.getDiceRoll();
            return String.format("%s (D%d:%d/3)", name, dice, stored);
        }
        
        return name;
    }
    
    /**
     * Appends a horizontal separator line.
     * 
     * @param sb The StringBuilder to append to
     * @param cols Number of columns
     */
    private static void appendHorizontalSeparator(StringBuilder sb, int cols) {
        sb.append("   ");
        for (int c = 0; c < cols; c++) {
            sb.append("+--------------------");
        }
        sb.append("+\n");
    }
    
    /**
     * Renders a compact summary of player stats only.
     * 
     * @param player The player
     * @return Stats string
     */
    public static String renderPlayerStats(Player player) {
        return String.format("VP=%d CP=%d SP=%d FP=%d PP=%d",
            player.getVictoryPoints(), 
            player.getCommercePoints(), 
            player.getSkillPoints(), 
            player.getStrengthPoints(), 
            player.getProgressPoints());
    }
}
