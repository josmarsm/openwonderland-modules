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

/*********************************************************************
 * Square: a reference to an individual square (i.e. cell) of a board.
 * @author deronj@dev.java.net
 */

/** 
 * Construct a new instance of a Square for a particular board.
 * @param board The board referred to by the square.
 * @param row The row in the board referred to by the square.
 * @param col The column in the board referred to by the square.
 */
function Square (board, row, col) {
    this.board = board;
    this.row = row;
    this.col = col;
}

/** Returns the row of the square. */
Square.prototype.getRow = function () {
    return this.row;
}

/** Returns the column of the square. */
Square.prototype.getCol = function () {
    return this.col;
}

/** Returns what color is in this square. */
Square.prototype.contents = function () {
    return this.board.getContentsOfSquare(this);
}

/** Is this square empty? */
Square.prototype.isEmpty = function () {
    return this.contents(this) == Board.Color.EMPTY;
}

/** 
 * Does this square contain the given color? 
 * @param color Either Board.Color.WHITE or BLACK.
 */
Square.prototype.contains = function (color) {
    return this.contents() == color;
}

/**
 * Place a piece of the given color in this square.
 * This overwrites any existing piece in this square.
 * @param color Either Board.Color.WHITE or BLACK.
 */
Square.prototype.placePiece = function (color) { 
    if (color == Board.Color.EMPTY) return;

    // Is square already occupied? 
    if (!this.isEmpty()) {
	this.board.countDecrement(this.contents());
    } 

    this.board.countIncrement(color);
    this.board.setContents(this, color);

    this.updateUI();
}

/**
 * Returns a new square one step in the given direction from this square.
 */
Square.prototype.addDirection = function (dir) {
    return new Square(this.board, this.row + dir.getDeltaRow(), this.col + dir.getDeltaCol());
}

/**
 * Is this square inside the board?
 */
Square.prototype.isInside = function () {
    return this.row >= 0 && this.row < numRows && this.col >= 0 && this.col < numCols;
}		    

/**
 * Find runs to flip. A run is defined as a sequence of squares
 * containing the opposite color ending in a square of the current color.
 * Returns a SquareList.
 */
Square.prototype.findToFlip = function (currentColor) {

    // Determine the opposite of the current color
    var toFlipColor = Board.oppositeColor(currentColor);

    // Holds the resulting runs
    var toFlip = new SquareList();

    // Search for a run in each direction
    for (var i=0; i < Direction.ALL_DIRECTIONS.length; i++) {
        var dir = Direction.ALL_DIRECTIONS[i];
        var sq = this.addDirection(dir);

	// Step along the direction, creating new squares as we go
	var run = [];
	while (sq.isInside() && sq.contains(toFlipColor)) {
            run.push(sq);
            sq = sq.addDirection(dir);                                           
        }        				   

	// See if there is an opposite color at the end of the run
        if (run.length > 0 && sq.isInside() && sq.contains(currentColor)) {
            // We found a run to flip
	    toFlip.add(run);
        }
    }		    

    return toFlip;					   
}

/**
 * Redisplay this square.
 */
Square.prototype.updateUI = function () {
    this.board.updateUI(this);
}

Square.prototype.toString = function () {
    return "[" + this.row + "," + this.col + "]";
}
