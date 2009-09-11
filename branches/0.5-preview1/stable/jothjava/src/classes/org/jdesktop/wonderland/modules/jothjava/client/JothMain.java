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

package org.jdesktop.wonderland.modules.jothjava.client;

import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.jothjava.client.gamejava.Game;
import org.jdesktop.wonderland.modules.jothjava.client.gamejava.Square;
import org.jdesktop.wonderland.modules.jothjava.client.uijava.JothWindow;
import org.jdesktop.wonderland.modules.jothjava.client.uijava.UIWLSimple;

/************************************
 * JothMain: The Othello main module.
 * @author deronj@dev.java.net
 */

public class JothMain {
    
    /** The cell in which the game is displayed. */
    private App2DCell cell;
    /** The window which contains the game controls. */
    private JothWindow controlWindow;
    /** The Othello game object. */
    private Game game;
    /** The UI object used to display the game in the cell. */
    private UIWLSimple ui;

    public interface MouseClickHandler {
        public void squareClicked (Square square);
    }

    /**
     * Create a new instance of JothMain.
     * @param cell The app cell in which the game is displayed.
     * @param controlWindow The window which contains the game controls.
     */
    public JothMain (App2DCell cell, JothWindow controlWindow) {
        this.cell = cell;
        this.controlWindow = controlWindow;
        
        game = new Game();
        ui = new UIWLSimple(cell, controlWindow, game, new MouseClickHandler() {
                public void squareClicked (Square sq) {
                    game.performTurn(sq);
                }
            });
        game.setUI(ui);
    }

    /**
     * Controls the visibility of the game.
     */
    public void setVisible (boolean visible) {
        ui.setVisible(visible);
    }
}

