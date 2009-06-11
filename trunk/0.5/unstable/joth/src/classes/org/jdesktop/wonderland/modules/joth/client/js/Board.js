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

/**********************************************************************
 * Board: A 2D game board consisting of squares. A square may be empty, 
 * black or white.
 * @author deronj@dev.java.net
 */

/**
 * Constructor for class Board.
 * @param numRows The number of rows in the board. 
 * @param numCols The number of columns in the board. 
 */
function Board (numRows, numCols) {

     // Remember the size
     this.numRows = numRows;
     this.numCols = numCols;

     // The contents of the board: initially empty.
     this.squares = new Array(numRows);
     for (var r = 0; r < numRows; r++) {
         var row = new Array(numCols);
         for (var c = 0; c < numCols; c++) {
             row[c] = Board.Color.EMPTY;
         }
         this.squares[r] = row;
     }

     // Initialize piece counts
     this.whiteCount = 0;
     this.blackCount = 0;
     this.totalCount = 0;
}

/** Specify the UI to use to display this board. */
Board.prototype.setUI = function (ui) {
    this.ui = ui;
}

/**
 * Return the number of rows in this board.
 */
Board.prototype.getNumRows = function () {
    return this.numRows;
}

/**
 * Return the number of columns in this board.
 */
Board.prototype.getNumCols = function () {
    return this.numCols;
}

/**
 * Set the given square to the given color.
 * @param sq The square to set.
 * @param color The color to put in the square.
 */
Board.prototype.setContents = function (sq, color) {
    this.squares[sq.getRow()][sq.getCol()] = color; 
}

/** 
 * Return the contents of the given square.
 * @param row The row of the square.
 * @param col The column of the square.
 */
Board.prototype.getContents = function(row, col) {
    return this.squares[row][col];
}

/** 
 * Return the contents of the given square.
 * @param sq The square of interest..
 */
Board.prototype.getContentsOfSquare = function (sq) {
    return this.getContents(sq.getRow(), sq.getCol());
}

/** 
 * Increment the given piece count.
 * @param which The color counter to increment
 */
Board.prototype.countIncrement = function(which) {
    if (which == Board.Color.EMPTY) return;
    if (which == Board.Color.WHITE) {
        this.whiteCount++;
    } else {
        this.blackCount++;
    }
}

/** 
 * Decrement the given count.
 * @param which The type of count to decrement
 */
Board.prototype.countDecrement = function(which) {
    if (which == Board.Color.EMPTY) return;
    if (which == Board.Color.WHITE) {
        this.whiteCount--;
   } else {
        this.blackCount--;
    }
}

/**
 * Return the number of white pieces on the board.
 */
Board.prototype.getWhiteCount = function () {
    return this.whiteCount;
}

/**
 * Return the number of black pieces on the board.
 */
Board.prototype.getBlackCount = function () {
    return this.blackCount;
}

/**
 * Return the total number of  pieces on the board.
 */
Board.prototype.getTotalCount = function () {
    return this.getWhiteCount() + this.getBlackCount();
}

/**
 * Returns whether the game is over. The game is over when
 * all squares are filled with a piece.
 */
Board.prototype.isGameOver = function () {
    return this.getTotalCount() == this.numRows * this.numCols;
}

/**
 * Returns the message which says who won (i.e. who has the most pieces or a tie).
 */
Board.prototype.getWinnerMessage = function () {
    if (this.whiteCount == this.blackCount) {
        return "It's a tie.";
    } else {
        var winner = (this.whiteCount > this.blackCount) ? "White" : "Black";
        return winner + " wins!";
    }
}

/** The two piece colors. */
Board.Color = {
    EMPTY:0, WHITE:1, BLACK:2
}

/** The number of rows in the board singleton. */
Board.NUM_ROWS = 5;

/** The number of columns in the board singleton. */
Board.NUM_COLS = 5;

/**
 * Return the opposite color of the given color.
 */
Board.oppositeColor = function (color) {
    if (color == Board.Color.WHITE) {
        return Board.Color.BLACK;
    } else {
        return Board.Color.WHITE;
    }
}

/**
 * Redisplay the given square.
 */
Board.prototype.updateUI = function (sq) {
    this.ui.updateSquare(sq);
}


