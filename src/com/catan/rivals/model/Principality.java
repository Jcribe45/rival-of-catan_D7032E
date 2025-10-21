package com.catan.rivals.model;

import java.util.*;

/**
 * Represents a player's principality (their board).
 * 
 * SOLID: Single Responsibility - manages the 2D card grid
 * Booch: High cohesion - all board-related operations
 */
public class Principality {
    
    private List<List<Card>> grid;
    private static final int INITIAL_ROWS = 5;
    private static final int INITIAL_COLS = 5;
    
    /**
     * Constructor initializes an empty grid.
     */
    public Principality() {
        this.grid = new ArrayList<>();
        initializeGrid(INITIAL_ROWS, INITIAL_COLS);
    }
    
    /**
     * Initializes the grid with empty slots.
     * 
     * @param rows Number of rows
     * @param cols Number of columns
     */
    private void initializeGrid(int rows, int cols) {
        grid.clear();
        for (int r = 0; r < rows; r++) {
            List<Card> row = new ArrayList<>();
            for (int c = 0; c < cols; c++) {
                row.add(null);
            }
            grid.add(row);
        }
    }
    
    /**
     * Places a card at the specified position.
     * Automatically expands grid if needed.
     * 
     * @param row The row
     * @param col The column
     * @param card The card to place
     */
    public void placeCard(int row, int col, Card card) {
        ensureSize(row, col);
        grid.get(row).set(col, card);
    }
    
    /**
     * Gets the card at a position.
     * 
     * @param row The row
     * @param col The column
     * @return The card, or null if empty or out of bounds
     */
    public Card getCardAt(int row, int col) {
        if (row < 0 || col < 0 || row >= grid.size()) {
            return null;
        }
        
        List<Card> rowList = grid.get(row);
        if (rowList == null || col >= rowList.size()) {
            return null;
        }
        
        return rowList.get(col);
    }
    
    /**
     * Checks if a position is empty.
     * 
     * @param row The row
     * @param col The column
     * @return True if empty
     */
    public boolean isEmptyAt(int row, int col) {
        return getCardAt(row, col) == null;
    }
    
    /**
     * Removes a card from a position.
     * 
     * @param row The row
     * @param col The column
     * @return The removed card, or null if was empty
     */
    public Card removeCardAt(int row, int col) {
        if (row < 0 || col < 0 || row >= grid.size()) {
            return null;
        }
        
        List<Card> rowList = grid.get(row);
        if (rowList == null || col >= rowList.size()) {
            return null;
        }
        
        Card card = rowList.get(col);
        rowList.set(col, null);
        return card;
    }
    
    /**
     * Checks if a card with the given name exists in the principality.
     * 
     * @param cardName The card name
     * @return True if found
     */
    public boolean hasCard(String cardName) {
        for (List<Card> row : grid) {
            for (Card card : row) {
                if (card != null && card.getName().equalsIgnoreCase(cardName)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Finds all cards of a specific type.
     * 
     * @param cardType The CardType to search for
     * @return List of matching cards with their positions
     */
    public List<CardPosition> findCardsByType(CardType cardType) {
        List<CardPosition> results = new ArrayList<>();
        
        for (int r = 0; r < grid.size(); r++) {
            List<Card> row = grid.get(r);
            for (int c = 0; c < row.size(); c++) {
                Card card = row.get(c);
                if (card != null && card.getCardType() == cardType) {
                    results.add(new CardPosition(card, r, c));
                }
            }
        }
        
        return results;
    }
    
    /**
     * Expands the grid after building at an edge.
     * Returns the adjusted column position.
     * 
     * @param col The column where card was placed
     * @return The adjusted column position
     */
    public int expandAfterEdgeBuild(int col) {
        int cols = grid.isEmpty() ? 0 : grid.get(0).size();
        
        // If built in first column, add a new column on the left
        if (col == 0) {
            for (List<Card> row : grid) {
                row.add(0, null);
            }
            return col + 1;
        }
        // If built in last column, add a new column on the right
        else if (col == cols - 1) {
            for (List<Card> row : grid) {
                row.add(null);
            }
        }
        
        return col;
    }
    
    /**
     * Ensures the grid is large enough for the given position.
     * 
     * @param row The row
     * @param col The column
     */
    private void ensureSize(int row, int col) {
        // Add rows if needed
        while (grid.size() <= row) {
            List<Card> newRow = new ArrayList<>();
            int cols = grid.isEmpty() ? INITIAL_COLS : grid.get(0).size();
            for (int c = 0; c < cols; c++) {
                newRow.add(null);
            }
            grid.add(newRow);
        }
        
        // Add columns if needed
        for (List<Card> rowList : grid) {
            while (rowList.size() <= col) {
                rowList.add(null);
            }
        }
    }
    
    /**
     * Gets the number of rows.
     * 
     * @return Row count
     */
    public int getRowCount() {
        return grid.size();
    }
    
    /**
     * Gets the number of columns.
     * 
     * @return Column count
     */
    public int getColumnCount() {
        return grid.isEmpty() ? 0 : grid.get(0).size();
    }
    
    /**
     * Gets the entire grid (for rendering).
     * 
     * @return The grid
     */
    public List<List<Card>> getGrid() {
        return grid;
    }
    
    /**
     * Helper class to represent a card with its position.
     */
    public static class CardPosition {
        public final Card card;
        public final int row;
        public final int col;
        
        public CardPosition(Card card, int row, int col) {
            this.card = card;
            this.row = row;
            this.col = col;
        }
    }
}
