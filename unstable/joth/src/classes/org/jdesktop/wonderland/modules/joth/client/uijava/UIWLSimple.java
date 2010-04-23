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

package org.jdesktop.wonderland.modules.joth.client.uijava;

import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.joth.client.JothMain;
import org.jdesktop.wonderland.modules.joth.client.gamejava.Game;
import org.jdesktop.wonderland.modules.joth.client.gamejava.Square;
import org.jdesktop.wonderland.modules.joth.client.gamejava.UI;

/*********************************************
 * UIWLSimple: A simple Othello Wonderland UI.
 * @author deronj@dev.java.net
 */

public class UIWLSimple implements UI {

    /** The cell in which the game is displayed. */
    private App2DCell cell;
    /** The window which contains the game controls. */
    private JothWindow controlWindow;
    /** The handler called when a mouse click event occurs. */
    private JothMain.MouseClickHandler eventHandler;
    /** The game object. */
    private Game game;
    /** The visual display of the board. */
    private BoardDisplay boardDisplay;

    /**
     * Create a new instance of UIWLSimple.
     * @param cell The app cell in which the game is displayed.
     * @param controlWindow The window which contains the game controls.
     * @param game The game object.
     * @param eventHandler The handler called when a mouse click event occurs.
     */
    public UIWLSimple (App2DCell cell, JothWindow controlWindow, Game game,
                       JothMain.MouseClickHandler eventHandler) {
        this.cell = cell;
        this.controlWindow = controlWindow;
        this.eventHandler = eventHandler;
        this.game = game;

        boardDisplay = new BoardDisplay(cell, game.getBoard(), this);
    }

    /** {@inheritDoc} */
    @Override
    public void clearError () {
        controlWindow.clearError();
    }

    /** {@inheritDoc} */
    @Override
    public void error (String message) {
        controlWindow.error(message);
    }

    /** {@inheritDoc} */
    @Override
    public void displayCounts () {
        controlWindow.displayCounts(game.getBoard().getWhiteCount(),
                                    game.getBoard().getBlackCount());
    }

    /** {@inheritDoc} */
    @Override
    public void updateSquare (Square square) {
        boardDisplay.updateSquare(square);
    }

    /** {@inheritDoc} */
    @Override
    public void updateTurn () {
        controlWindow.setTurn(game.getCurrentTurn());
    }

    /** {@inheritDoc} */
    @Override
    public void notifyGameOver (String msg) {
        controlWindow.notifyGameOver(msg);
    }

    /** {@inheritDoc} */
    @Override
    public Square eventToSquare (Event event) {
        return boardDisplay.eventToSquare(event);
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible (boolean visible) {
        controlWindow.setVisible(visible);
        boardDisplay.setVisible(visible);
    }

    /**
     * Called by the board display when an event occurs.
     */
    void squareClicked (Event event) {
        eventHandler.squareClicked(eventToSquare(event));
    }
}
