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
package org.jdesktop.wonderland.modules.marbleous.client.cell;

import com.jme.math.Vector2f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.marbleous.client.MarbleousMain;
import org.jdesktop.wonderland.modules.marbleous.client.ui.MarbleousApp;
import org.jdesktop.wonderland.modules.marbleous.client.ui.MarbleousWindowConstruct;
import org.jdesktop.wonderland.modules.marbleous.client.ui.MarbleousWindowRun;
import org.jdesktop.wonderland.modules.marbleous.common.cell.MarbleousCellClientState;


/**
 * Client cell for the Marble Roller Coaster.
 *
 * @author deronj@dev.java.net
 */

public class MarbleousCell extends App2DCell {

    /** The logger used by this class */
    private static final Logger logger = Logger.getLogger(MarbleousCell.class.getName());
    /** The MarbleousMain singleton -- the game logic. */
    private MarbleousMain main;
    /** The track construction UI window. */
    private MarbleousWindowConstruct windowConstruct;
    /** The run simulation UI window. */
    private MarbleousWindowRun windowRun;
    /** The cell client state message received from the server cell */
    private MarbleousCellClientState clientState;

    /**
     * Create an instance of MarbleousCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public MarbleousCell(CellID cellID, CellCache cellCache) {
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
        clientState = (MarbleousCellClientState) state;
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
                    MarbleousApp app = new MarbleousApp("Marble RollerCoaster", new Vector2f(0.01f, 0.01f));
                    setApp(app);

                    // Tell the app to be displayed in this cell.
                    app.addDisplayer(this);

                    // Create the UI windows
                    windowConstruct = new MarbleousWindowConstruct(this, app, 400, 200, true, 
                                                          new Vector2f(0.01f, 0.01f));
                    //windowRun = new MarbleousWindowRun(this, app, 400, 200, true, 
                    //                             new Vector2f(0.01f, 0.01f));
                    windowRun = null;

                    main = new MarbleousMain(this, windowConstruct, windowRun);
                    main.setVisible(true);
                }
                break;

            // The cell is no longer visible
            case DISK:
                if (!increasing) {
                    main.setVisible(false);
                    main = null;
                    windowConstruct = null;
                    windowRun = null;
                }
                break;
        }
    }
}
