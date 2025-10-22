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
            sb.append(String.format("%-20s ", "Col " + c));
        }
        sb.append("\n");
        
        // Separator
        appendHorizontalSeparator(sb, cols);

        // Only render valid rows (1, 2, 3)
        for (int r = prin.getMinValidRow(); r <= prin.getMaxValidRow(); r++) {
            // First line: Row index and card names
            sb.append(String.format("%2d |", r));
            for (int c = 0; c < cols; c++) {
                Card card = prin.getCardAt(r, c);
                String title = cellTitle(card);
                sb.append(String.format("%-20s|", title));
            }
            sb.append("\n");

            // Second line: Resource / dice info (or blank)
            sb.append("   |");
            for (int c = 0; c < cols; c++) {
                Card card = prin.getCardAt(r, c);
                String info = cellInfo(card);
                sb.append(String.format("%-20s|", info));
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
     * Returns the title of the given card for display, including shortcut descriptor for resource regions.
     *
     * @param c the card (may be null)
     * @return the display title (empty string if card is null)
     */
    private static String cellTitle(Card c) {
        if (c == null) {
            return "";
        }
        String title = c.getName();
        if ("Forest".equals(title)) {
            title += " (L):Lumber";
        } else if ("Hill".equals(title)) {
            title += " (B):Brick";
        } else if ("Field".equals(title)) {
            title += " (G):Grain";
        } else if ("Pasture".equals(title)) {
            title += " (W):Wool";
        } else if ("Mountain".equals(title)) {
            title += " (O):Ore";
        } else if ("Gold Field".equals(title)) {
            title += " (A):Gold";
        }
        return title == null ? "Unknown" : title;
    }

    /**
     * Returns the info-line for the given card for display (e.g., dice roll, stored resources).
     *
     * @param c the card (may be null)
     * @return the info string (empty string if card is null or non-region)
     */
    private static String cellInfo(Card c) {
        if (c == null) {
            return "";
        }
        if (CardType.REGION.equals(c.getCardType())) {
            int diceRoll = c.getDiceRoll();
            String die = (diceRoll <= 0 ? "-" : String.valueOf(diceRoll));
            int stored = Math.max(0, Math.min(3, c.getStoredResources()));
            return "d" + die + "  " + stored + "/3";
        }
        return "";
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