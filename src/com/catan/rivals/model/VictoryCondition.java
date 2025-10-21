package com.catan.rivals.model;

import com.catan.rivals.player.Player;

/**
 * Defines victory conditions and checks for game end.
 * 
 * SOLID: Single Responsibility - handles only victory checking
 * SOLID: Open-Closed - can be extended for different victory conditions
 * Booch: Completeness - all victory logic in one place
 */
public class VictoryCondition {
    
    private final int requiredPoints;
    
    /**
     * Constructor.
     * 
     * @param requiredPoints The number of victory points required to win
     */
    public VictoryCondition(int requiredPoints) {
        this.requiredPoints = requiredPoints;
    }
    
    /**
     * Checks if a player has won.
     * Victory Points include:
     * - Base VP from cards
     * - +1 VP if player has trade advantage (≥3 commerce points ahead)
     * - +1 VP if player has strength advantage (≥3 strength points ahead)
     * 
     * @param player The player to check
     * @param opponent The opponent
     * @return True if player has won
     */
    public boolean hasWon(Player player, Player opponent) {
        return calculateTotalVictoryPoints(player, opponent) >= requiredPoints;
    }
    
    /**
     * Calculates total victory points including advantage tokens.
     * 
     * @param player The player
     * @param opponent The opponent
     * @return Total victory points
     */
    public int calculateTotalVictoryPoints(Player player, Player opponent) {
        int total = player.getVictoryPoints();
        
        // Trade advantage: +1 VP if ≥3 commerce points ahead
        if (hasTradeAdvantage(player, opponent)) {
            total += 1;
        }
        
        // Strength advantage: +1 VP if ≥3 strength points ahead
        if (hasStrengthAdvantage(player, opponent)) {
            total += 1;
        }
        
        return total;
    }
    
    /**
     * Checks if player has trade advantage.
     * 
     * @param player The player
     * @param opponent The opponent
     * @return True if has advantage
     */
    public boolean hasTradeAdvantage(Player player, Player opponent) {
        return player.getCommercePoints() - opponent.getCommercePoints() >= 3;
    }
    
    /**
     * Checks if player has strength advantage.
     * 
     * @param player The player
     * @param opponent The opponent
     * @return True if has advantage
     */
    public boolean hasStrengthAdvantage(Player player, Player opponent) {
        return player.getStrengthPoints() - opponent.getStrengthPoints() >= 3;
    }
    
    /**
     * Gets the required victory points.
     * 
     * @return Required points
     */
    public int getRequiredPoints() {
        return requiredPoints;
    }
    
    /**
     * Gets a summary of victory points for a player.
     * 
     * @param player The player
     * @param opponent The opponent
     * @return A formatted string summary
     */
    public String getVictoryPointsSummary(Player player, Player opponent) {
        StringBuilder sb = new StringBuilder();
        
        int baseVP = player.getVictoryPoints();
        int total = calculateTotalVictoryPoints(player, opponent);
        
        sb.append("Victory Points: ").append(total).append("/").append(requiredPoints).append("\n");
        sb.append("  Base VP: ").append(baseVP).append("\n");
        
        if (hasTradeAdvantage(player, opponent)) {
            sb.append("  Trade Advantage: +1\n");
        }
        
        if (hasStrengthAdvantage(player, opponent)) {
            sb.append("  Strength Advantage: +1\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Factory method for standard introductory game.
     * 
     * @return A VictoryCondition for 7 points
     */
    public static VictoryCondition introductoryGame() {
        return new VictoryCondition(7);
    }
    
    /**
     * Factory method for theme game.
     * 
     * @return A VictoryCondition for 12 points
     */
    public static VictoryCondition themeGame() {
        return new VictoryCondition(12);
    }
}
