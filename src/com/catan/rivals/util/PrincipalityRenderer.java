package com.catan.rivals.util;

import com.catan.rivals.model.*;
import com.catan.rivals.player.Player;

/**
 * Handles rendering of Principality boards for display.
 */
public class PrincipalityRenderer {
    
    /**
     * Renders a principality as an ASCII grid with player stats.
     * Only renders valid rows (1, 2, 3).
     * Dynamically adjusts to grid size.
     * 
     * @param player The player whose principality to render
     * @return Formatted string representation
     */
    public static String renderPrincipality(Player player) {
        StringBuilder sb = new StringBuilder();
        Principality prin = player.getPrincipality();
        int cols = prin.getColumnCount();
        
        // Column headers
        sb.append("           ");
        for (int c = 0; c < cols; c++) {
            sb.append(String.format("%-15s ", "Col " + c));
        }
        sb.append("\n");
        
        // Separator
        appendHorizontalSeparator(sb, cols);
        
        for (int r = 0; r < prin.getInitialRows(); r++) {
            // First line: Row index and card names
            sb.append(String.format("%2d |", r));
            for (int c = 0; c < cols; c++) {
                Card card = prin.getCardAt(r, c);
                String title = cellTitle(card);
                sb.append(String.format("%-15s|", title));
            }
            sb.append("\n");

            // Second line: Resource / dice info (or blank)
            sb.append("   |");
            for (int c = 0; c < cols; c++) {
                Card card = prin.getCardAt(r, c);
                String info = cellInfo(card);
                sb.append(String.format("%-15s|", info));
            }
            sb.append("\n");

            // Separator
            appendHorizontalSeparator(sb, cols);
        }
        
        
        
        // Points summary
        sb.append(String.format("\nPoints: VP=%d CP=%d SP=%d FP=%d PP=%d\n",
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
        
        sb.append("\n").append("=".repeat(80)).append("\n");
        sb.append(centerText("OPPONENT'S PRINCIPALITY", 80)).append("\n");
        sb.append("=".repeat(80)).append("\n");
        sb.append(renderPrincipality(opponent));

        sb.append("\n").append("=".repeat(80)).append("\n");
        sb.append(centerText("YOUR PRINCIPALITY", 80)).append("\n");
        sb.append("=".repeat(80)).append("\n");
        sb.append(renderPrincipality(player));
        
        return sb.toString();
    }
    
    /**
     * Returns the title of the given card for display, including resource type shortcut.
     */
    private static String cellTitle(Card c) {
        if (c == null) {
            return "";
        }
        
        String title = c.getName();
        
        // Add resource shortcuts for regions
        switch (title) {
            case "Forest":
                return "Forest (L)";
            case "Hill":
                return "Hill (B)";
            case "Field":
                return "Field (G)";
            case "Pasture":
                return "Pasture (W)";
            case "Mountain":
                return "Mountain (O)";
            case "Gold Field":
                return "Gold Field (A)";
            default:
                return title;
        }
    }

    /**
     * Returns the info-line for the given card (dice roll, stored resources).
     */
    private static String cellInfo(Card c) {
        if (c == null) {
            return "";
        }
        
        if (CardType.REGION.equals(c.getCardType())) {
            int diceRoll = c.getDiceRoll();
            String die = (diceRoll <= 0 ? "-" : String.valueOf(diceRoll));
            int stored = Math.max(0, Math.min(3, c.getStoredResources()));
            return String.format("D:%s Stored:%d/3", die, stored);
        }
        
        // Show points for other cards
        if (c.getVictoryPoints() > 0) {
            return String.format("VP:%d", c.getVictoryPoints());
        }
        
        return "";
    }
    
    /**
     * Appends a horizontal separator line.
     */
    private static void appendHorizontalSeparator(StringBuilder sb, int cols) {
        sb.append("   ");
        for (int c = 0; c < cols; c++) {
            sb.append("+---------------");
        }
        sb.append("+\n");
    }
    
    
    /**
     * Centers text in a field of given width.
     */
    private static String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }
    
    /**
     * Renders a compact summary of player stats only.
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