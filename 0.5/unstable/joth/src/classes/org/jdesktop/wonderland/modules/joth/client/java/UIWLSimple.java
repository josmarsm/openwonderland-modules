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

/*********************************************************
 * UI: A simple Wonderland user interface to Othello.
 *
 * @author deronj@dev.java.net
 */

public class UIWLSimple implements UI {

    /** The WL cell in which the UI is displayed. */
    private JothCell cell;

    /** The game displayed by this UI. */
    private Game game;

    /** The board entity. */
    private Entity boardEntity;

    /** The control panel child cell. Used for displaying messages. */
    private ControlPanelCell controlPanelCell;
        
    /**
     * Constructor for the UI which uses simple WL graphics to display game results.
     * @param game The game which is being displayed by this UI.
     */
    public UIWLSimple (JothCell cell, Game game) {
        this.cell = cell;
        this.game = game;
        initialize();
    }

    /**
     * Initialize the UI.
     */
    private void initialize () {
        createComponents();
    }

    /**
     * Set the error message to blank.
     */
    public void clearError () {
        controlPanelCell.clearErrorMessage();
    }

    /**
     * Display the given message.
     */
    public void error (String message) {
        controlPanelCell.setErrorMessage(message);
    }

    /**
     * Display the current color counts.
     */
    public void displayCounts () {
        controlPanelCell.displayCounts();
    }

    /**
     * Update the display of a particular square with its
     * current contents in the board.
     */
    public void updateSquare (Square square) {
        board.updateSquare(square);
    }

    /** Display whose turn it is. */
    public void updateTurn () {
        controlPanelCell.updateTurn();
    }

    /** The game is over. Notify the user of the result. */
    public void notifyGameOver (String msg) {
        controlPanelCell.notifyGameOver();
    }

    /** Given a click event, determine which square it is in. */
    public Square eventToSquare (Event event) {
        // TODO: When each quad of the board is created use a subclass
        // of Quad to hold the row/col of each quad.
        // Map event to node. Cast the node to the subclass. And get the 
        // row/col. Then create the square based on that.
    }

    /** 
     * Create the various components and attach them to the cell.
     */
    private void createComponents() {

        // Create individual components
        boardEntity = new BoardGraph(game.getBoard(););
        controlPanelCell = new ControlPanelCell(game);

        // TODO: Attach board to cell

        // Attach control panel to cell as a child cell
        cell.addChild(controlPanelCell);

        // Move control panel into desired location with respect to the board graph.
        controlPanelCell.setTranslation(Vector3f(0f, 0f, 0f)/*TODO*/);
    }
}
