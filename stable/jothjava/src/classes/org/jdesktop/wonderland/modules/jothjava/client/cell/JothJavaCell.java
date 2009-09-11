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
package org.jdesktop.wonderland.modules.jothjava.client.cell;

import com.jme.math.Vector2f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.jothjava.client.uijava.JothApp;
import org.jdesktop.wonderland.modules.jothjava.client.JothMain;
import org.jdesktop.wonderland.modules.jothjava.client.uijava.JothWindow;
import org.jdesktop.wonderland.modules.jothjava.common.cell.JothCellClientState;


/**
 * Client cell for the Othello game.
 *
 * @author deronj@dev.java.net
 */

public class JothJavaCell extends App2DCell {

    /** The logger used by this class */
    private static final Logger logger = Logger.getLogger(JothJavaCell.class.getName());
    /** The JothMain singleton. */
    private JothMain main;
    /** The (singleton) window created by the Othello program. */
    private JothWindow window;
    /** The cell client state message received from the server cell */
    private JothCellClientState clientState;

    /**
     * Create an instance of JothJavaCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public JothJavaCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * Initialize the cell with parameters from the server.
     *
     * @param state the client state with which initialize the cell.
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        clientState = (JothCellClientState) state;
    }

    /**
     * This is called when the status of the cell changes.
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch (status) {

            // The cell is now visible
            case ACTIVE:
                if (increasing) {
                    JothApp stApp = new JothApp("Othello", new Vector2f(0.01f, 0.01f));
                    setApp(stApp);

                    // Tell the app to be displayed in this cell.
                    stApp.addDisplayer(this);

                    // This app has only one window, so it is always top-level
                    window = new JothWindow(this, stApp, 500, 300, true, new Vector2f(0.01f, 0.01f));
                
                    main = new JothMain(this, window);
                    main.setVisible(true);
                }
                break;

            // The cell is no longer visible
            case DISK:
                if (!increasing) {
                    main.setVisible(false);
                    main = null;
                    window = null;
                }
                break;
        }
    }
}
