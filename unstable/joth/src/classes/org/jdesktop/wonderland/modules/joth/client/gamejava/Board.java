/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.joth.client.gamejava;

/**********************************************************************
 * Board: A 2D game board consisting of squares. A square may be empty, 
 * black or white.
 * @author deronj@dev.java.net
 */
public class Board {

    /** The number of rows in the board singleton. */
    public static final int NUM_ROWS = 5;

    /** The number of columns in the board singleton. */
    public static final int NUM_COLS = 5;

    /** The number of rows in this board. */
    private int numRows;

    /** The number of columns in this board. */
    private int numCols;

    /** What a board square can contain. */
    public enum Color { EMPTY, WHITE, BLACK };

    /** The contents of the board. */
    private Color[][] squares;

    /** The number of white pieces on the board. */
    private int whiteCount = 0;

    /** The number of black pieces on the board. */
    private int blackCount = 0;

    /** The UI which displays this board. */
    private UI ui;

    /**
     * Constructor for class Board.
     * @param numRows The number of rows in the board. 
     * @param numCols The number of columns in the board. 
     */
    public Board (int numRows, int numCols) {

        // Remember the size
        this.numRows = numRows;
        this.numCols = numCols;

        // The contents of the board: initially empty.
        squares = new Color[numRows][];
        for (int r = 0; r < numRows; r++) {
            Color[] row = new Color[numCols];
            for (int c = 0; c < numCols; c++) {
                row[c] = Color.EMPTY;
            }
            squares[r] = row;
        }

        // Initialize piece counts
        whiteCount = 0;
        blackCount = 0;
    }

    /** Specify the UI to use to display this board. */
    public void setUI (UI ui) {
        this.ui = ui;
    }

    /**
     * Return the number of rows in this board.
     */
    public int getNumRows () {
        return numRows;
    }

    /**
     * Return the number of columns in this board.
     */
    public int getNumCols () {
        return numCols;
    }

    /**
     * Set the given square to the given color.
     * @param sq The square to set.
     * @param color The color to put in the square.
     */
    public void setContents (Square sq, Color color) {
        squares[sq.getRow()][sq.getCol()] = color; 
    }

    /** 
     * Return the contents of the given square.
     * @param row The row of the square.
     * @param col The column of the square.
     */
    public Color getContents (int row, int col) {
        return squares[row][col];
    }

    /** 
     * Return the contents of the given square.
     * @param sq The square of interest..
     */
    public Color getContentsOfSquare (Square sq) {
        return getContents(sq.getRow(), sq.getCol());
    }

    /** 
     * Increment the given piece count.
     * @param which The color counter to increment
     */
    public void countIncrement (Color which) {
        if (which == Color.EMPTY) return;
        if (which == Color.WHITE) {
            whiteCount++;
        } else {
            blackCount++;
        }
    }

    /** 
     * Decrement the given count.
     * @param which The type of count to decrement
     */
    public void countDecrement (Color which) {
        if (which == Color.EMPTY) return;
        if (which == Color.WHITE) {
            this.whiteCount--;
        } else {
            this.blackCount--;
        }
    }

    /**
     * Return the number of white pieces on the board.
     */
    public int getWhiteCount () {
        return whiteCount;
    }

    /**
     * Return the number of black pieces on the board.
     */
    public int getBlackCount () {
        return blackCount;
    }

    /**
     * Return the total number of  pieces on the board.
     */
    public int getTotalCount  () {
        return getWhiteCount() + getBlackCount();
    }

    /**
     * Returns whether the game is over. The game is over when
     * all squares are filled with a piece.
     */
    public boolean isGameOver  () {
        return getTotalCount() == numRows * numCols;
    }

    /**
     * Returns the message which says who won (i.e. who has the most pieces or a tie).
     */
    public String getWinnerMessage  () {
        if (whiteCount == blackCount) {
            return "It's a tie.";
        } else {
            String winner = (whiteCount > blackCount) ? "White" : "Black";
            return winner + " wins!";
        }
    }

    /**
     * Return the opposite color of the given color.
     */
    public static Color oppositeColor (Color color) {
        if (color == Color.WHITE) {
            return Color.BLACK;
        } else {
            return Color.WHITE;
        }
    }

    /**
     * Redisplay the given square.
     */
    public void updateUI (Square sq) {
        ui.updateSquare(sq);
    }
}

