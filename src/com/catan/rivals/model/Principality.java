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
    
    // Valid rows are 1, 2, 3 only (rows 0 and 4 are buffer rows)
    private static final int MIN_VALID_ROW = 1;
    private static final int MAX_VALID_ROW = 3;
    
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
     * Validates if a row is valid for placement.
     * Only rows 1, 2, 3 are valid (rows 0 and 4 are buffer rows).
     * 
     * @param row The row to validate
     * @return True if valid
     */
    public boolean isValidRow(int row) {
        return row >= MIN_VALID_ROW && row <= MAX_VALID_ROW;
    }
    
    /**
     * Places a card at the specified position.
     * Automatically expands grid if needed.
     * 
     * @param row The row
     * @param col The column
     * @param card The card to place
     * @return True if successfully placed
     */
    public boolean placeCard(int row, int col, Card card) {
        if (!isValidRow(row)) {
            return false;
        }
        
        ensureSize(row, col);
        grid.get(row).set(col, card);
        return true;
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
     * @return True if empty and valid
     */
    public boolean isEmptyAt(int row, int col) {
        if (!isValidRow(row)) {
            return false;
        }
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
        if (!isValidRow(row) || row < 0 || col < 0 || row >= grid.size()) {
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
        for (int r = MIN_VALID_ROW; r <= MAX_VALID_ROW; r++) {
            if (r >= grid.size()) continue;
            List<Card> row = grid.get(r);
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
        
        for (int r = MIN_VALID_ROW; r <= MAX_VALID_ROW; r++) {
            if (r >= grid.size()) continue;
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
     * Expands the grid and places regions when building a settlement.
     * Official Rules: When building a settlement, two new regions are added
     * diagonally above and below the settlement.
     * 
     * @param settlementRow The settlement row (must be row 2)
     * @param settlementCol The settlement column
     * @param region1 The region to place above (row 1)
     * @param region2 The region to place below (row 3)
     * @return The adjusted column position of the settlement
     */
    public int expandForSettlement(int settlementRow, int settlementCol, 
                                   Card region1, Card region2) {
        if (settlementRow != 2) {
            throw new IllegalArgumentException("Settlements must be built in row 2");
        }
        
        int cols = grid.get(0).size();
        int adjustedCol = settlementCol;
        
        // If building on left edge (col 0), expand left
        if (settlementCol == 0) {
            // Add two new columns on the left
            for (List<Card> row : grid) {
                row.add(0, null); // Add first column
                row.add(0, null); // Add second column
            }
            adjustedCol = settlementCol + 2;
            
            // Place regions in the new columns
            if (region1 != null) {
                grid.get(1).set(0, region1); // Row 1, leftmost column
            }
            if (region2 != null) {
                grid.get(3).set(0, region2); // Row 3, leftmost column
            }
        }
        // If building on right edge (last column), expand right
        else if (settlementCol == cols - 1) {
            // Add two new columns on the right
            for (List<Card> row : grid) {
                row.add(null); // Add first column
                row.add(null); // Add second column
            }
            
            // Place regions in the new columns
            int newCol = cols;
            if (region1 != null) {
                grid.get(1).set(newCol, region1); // Row 1
            }
            if (region2 != null) {
                grid.get(3).set(newCol, region2); // Row 3
            }
        }
        // If building in middle, place regions diagonally adjacent
        else {
            // Find empty diagonal positions
            if (region1 != null) {
                // Try col-1 first, then col+1
                if (isEmptyAt(1, settlementCol - 1)) {
                    grid.get(1).set(settlementCol - 1, region1);
                } else if (isEmptyAt(1, settlementCol + 1)) {
                    grid.get(1).set(settlementCol + 1, region1);
                }
            }
            
            if (region2 != null) {
                // Try col-1 first, then col+1
                if (isEmptyAt(3, settlementCol - 1)) {
                    grid.get(3).set(settlementCol - 1, region2);
                } else if (isEmptyAt(3, settlementCol + 1)) {
                    grid.get(3).set(settlementCol + 1, region2);
                }
            }
        }
        
        return adjustedCol;
    }
    
    /**
     * Expands the grid when building a road on an edge.
     * 
     * @param roadRow The road row (must be row 2)
     * @param roadCol The road column
     * @return The adjusted column position
     */
    public int expandForRoad(int roadRow, int roadCol) {
        if (roadRow != 2) {
            throw new IllegalArgumentException("Roads must be built in row 2");
        }
        
        int cols = grid.get(0).size();
        int adjustedCol = roadCol;
        
        // If building on left edge (col 0), add column on left
        if (roadCol == 0) {
            for (List<Card> row : grid) {
                row.add(0, null);
            }
            adjustedCol = roadCol + 1;
        }
        // If building on right edge (last column), add column on right
        else if (roadCol == cols - 1) {
            for (List<Card> row : grid) {
                row.add(null);
            }
        }
        
        return adjustedCol;
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
     * Gets the minimum valid row (1).
     * 
     * @return Minimum valid row
     */
    public int getMinValidRow() {
        return MIN_VALID_ROW;
    }
    
    /**
     * Gets the maximum valid row (3).
     * 
     * @return Maximum valid row
     */
    public int getMaxValidRow() {
        return MAX_VALID_ROW;
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