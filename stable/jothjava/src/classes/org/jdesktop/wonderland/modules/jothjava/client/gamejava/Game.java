/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

import java.util.ResourceBundle;

/********************************
 * Game: The Othello game object.
 * @author deronj@dev.java.net
 */
public class Game {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/jothjava/client/uijava/Bundle");
    /** The Othello game board. */
    private Board board;
    /** The color of the player whose turn it is. */
    private Board.Color currentColor;
    /** The name of the player whose turn it is. */
    private String currentTurn;
    /** The user interface for the game. */
    private UI ui;

    /**
     * Construct a new instance of an Othello game.
     */
    public Game() {
        board = new Board(Board.NUM_ROWS, Board.NUM_COLS);
        currentColor = Board.Color.WHITE;
        currentTurn = "white";
    }

    /** Specify the UI to use to display this game. */
    public void setUI(UI ui) {
        this.ui = ui;
        board.setUI(ui);
    }

    /** Returns the UI that displays this game. */
    public UI getUI() {
        return ui;
    }

    /** Returns the board for this game. */
    public Board getBoard() {
        return board;
    }

    /** Returns the color of the player whose turn it is. */
    public Board.Color getCurrentColor() {
        return currentColor;
    }

    /** Returns the name of player whose turn it is. */
    public String getCurrentTurn() {
        return currentTurn;
    }

    /**
     * ***** The Key Function in the Othello Program *****
     * Called when a square is clicked. Places the piece of the current
     * color and performs the necessary flipping of the opponents pieces.
     */
    public void performTurn(Square square) {
        ui.clearError();
        if (square.isEmpty()) {
            square.placePiece(currentColor);
            SquareList toFlip = square.findToFlip(this.currentColor);
            //ui.debug(toFlip.toString());
            toFlip.flip(currentColor);
            flipTurn();
            ui.displayCounts();
            checkWin();
        } else {
            ui.error(BUNDLE.getString("Illegal_Move"));
        }
    }

    // The current color's turn is over. Make the current color the opposite color.
    private void flipTurn() {
        if (currentColor == Board.Color.WHITE) {
            currentColor = Board.Color.BLACK;
            currentTurn = "black";
        } else {
            currentColor = Board.Color.WHITE;
            currentTurn = "white";
        }
        ui.updateTurn();
    }

    // Check to see if anybody has won.
    private void checkWin() {
        if (board.isGameOver()) {
            // Game over
            String msg = BUNDLE.getString("Game_Over") + ' '
                    + board.getWinnerMessage();
            ui.notifyGameOver(msg);
        }
    }
}



