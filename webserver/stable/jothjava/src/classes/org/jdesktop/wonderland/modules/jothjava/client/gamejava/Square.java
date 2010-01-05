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

package org.jdesktop.wonderland.modules.jothjava.client.gamejava;

import java.util.LinkedList;

/*********************************************************************
 * Square: a reference to an individual square (i.e. cell) of a board.
 * @author deronj@dev.java.net
 */

public class Square {

    /** The board in which this square lies. */
    private Board board;
    /** The row of the square. */
    private int row;
    /** The column of the square. */
    private int col;

    /** 
     * Construct a new instance of a Square for a particular board.
     * @param board The board referred to by the square.
     * @param row The row in the board referred to by the square.
     * @param col The column in the board referred to by the square.
     */
    public Square (Board board, int row, int col) {
        this.board = board;
        this.row = row;
        this.col = col;
    }

    /** Returns the row of the square. */
    public int getRow() {
        return row;
    }

    /** Returns the col of the square. */
    public int getCol() {
        return col;
    }

    /** Returns what color is in this square. */
    public Board.Color contents () {
        return board.getContentsOfSquare(this);
    }

    /** Is this square empty? */
    public boolean isEmpty () {
        return contents() == Board.Color.EMPTY;
    }

    /** Does this square contain the given color? */
    public boolean contains (Board.Color color) {
        return contents() == color;
    }

    /**
     * Place a piece of the given color in this square.
     * This overwrites any existing piece in this square.
     * @param color Either Board.Color.WHITE or BLACK.
     */
    public void placePiece (Board.Color color) { 
        if (color == Board.Color.EMPTY) return;

        // Is square already occupied? 
        if (!isEmpty()) {
            board.countDecrement(contents());
        } 

        board.countIncrement(color);
        board.setContents(this, color);

        updateUI();
    }

    /**
     * Returns a new square one step in the given direction from this square.
     */
    public Square addDirection (Direction dir) {
        return new Square(board, row + dir.getDeltaRow(), col + dir.getDeltaCol());
    }

    /**
     * Is this square inside the board?
     */
    public boolean isInside () {
        return row >= 0 && row < board.getNumRows() && 
               col >= 0 && col < board.getNumCols();
    }		    

    /**
     * Find runs to flip. A run is defined as a sequence of squares
     * containing the opposite color ending in a square of the current color.
     */
    public SquareList findToFlip (Board.Color currentColor) {

        // Determine the opposite of the current color
        Board.Color toFlipColor = Board.oppositeColor(currentColor);

        // Holds the resulting runs
        SquareList toFlip = new SquareList();

        // Search for a run in each direction
        for (int i=0; i < Direction.ALL_DIRECTIONS.length; i++) {
            Direction dir = Direction.ALL_DIRECTIONS[i];
            Square sq = addDirection(dir);

            // Step along the direction, creating new squares as we go
            LinkedList<Square> run = new LinkedList<Square>();
            while (sq.isInside() && sq.contains(toFlipColor)) {
                run.addLast(sq);
                sq = sq.addDirection(dir);                                           
            }        				   

            // See if there is an opposite color at the end of the run
            if (run.size() > 0 && sq.isInside() && sq.contains(currentColor)) {
                // We found a run to flip
                toFlip.add(run);
            }
        }		    

        return toFlip;					   
    }

    /**
     * Redisplay this square.
     */
    public void updateUI () {
        board.updateUI(this);
    }

    public String toString () {
        return "[" + row + "," + col + "]";
    }
}