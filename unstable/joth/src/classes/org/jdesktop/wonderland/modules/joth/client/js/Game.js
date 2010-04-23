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

/********************************
 * Game: The Othello game object.
 * @author deronj@dev.java.net
 */

/**
 * Create a new instance of an Othello game.
 */
function Game () {
    this.board = new Board(Board.NUM_ROWS, Board.NUM_COLS);
    this.currentColor = Board.Color.WHITE;
    this.currentTurn = "white";
}

/** Specify the UI to use to display this game. */
Game.prototype.setUI = function (ui) {
    this.ui = ui;
    this.board.setUI(ui);
}

/** Returns the UI that displays this game. */
Game.prototype.getUI = function (ui) {
    return this.ui;
}

/** Returns the board for this game. */
Game.prototype.getBoard = function () {
    return this.board;
}

/** Returns the color of the player whose turn it is. */
Game.prototype.getCurrentColor = function () {
    return this.currentColor;
}

/** Returns the name of player whose turn it is. */
Game.prototype.getCurrentTurn = function () {
    return this.currentTurn;
}

/**
 * ***** The Key Function in the Othello Program *****
 * Called when a square is clicked. Places the piece of the current
 * color and performs the necessary flipping of the opponents pieces.
 */
Game.prototype.performTurn = function (square) {
    this.ui.clearError();
    if (square.isEmpty()) {
        square.placePiece(this.currentColor);
        var toFlip = square.findToFlip(this.currentColor);
	//this.ui.debug(toFlip.toString());
	toFlip.flip(this.currentColor);
        this.flipTurn();
	this.ui.displayCounts();
	this.checkWin();
    } else {
        this.ui.error("Illegal move. Square is not empty.");
    }
}

// The current color's turn is over. Make the current color the opposite color.
Game.prototype.flipTurn = function () {
    var currentTurn;
    if (this.currentColor == Board.Color.WHITE) {
        this.currentColor = Board.Color.BLACK;
        this.currentTurn = "black";
    } else {
        this.currentColor = Board.Color.WHITE;
        this.currentTurn = "white";
    }
    this.ui.updateTurn();
}

/** Check to see if anybody has won. */
Game.prototype.checkWin = function () {
    if (this.board.isGameOver()) {
	// Game over
	var msg = "Game Over. " + this.board.getWinnerMessage();
        this.ui.notifyGameOver(msg);
    }
}




