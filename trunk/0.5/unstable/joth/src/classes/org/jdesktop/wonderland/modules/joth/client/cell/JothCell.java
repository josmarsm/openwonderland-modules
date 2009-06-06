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
package org.jdesktop.wonderland.modules.joth.client.cell;


/**
 * Client cell for the Othello game.
 *
 * @author deronj@dev.java.net
 */

public class JothCell extends App2DCell {

    /** The logger used by this class */
    private static final Logger logger = Logger.getLogger(JothCell.class.getName());
    /** The (singleton) window created by the Othello program. */
    private JothWindow window;
    /** The cell client state message received from the server cell */
    private JothCellClientState clientState;

    /**
     * Create an instance of JothCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public JothCell(CellID cellID, CellCache cellCache) {
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
    public boolean setStatus(CellStatus status) {
        boolean ret = super.setStatus(status);

        switch (status) {

            // The cell is now visible
            case ACTIVE:

                JothApp stApp = new JothApp("Othello", clientState.getPixelScale());
                setApp(stApp);

                // Tell the app to be displayed in this cell.
                stApp.addDisplayer(this);

                // This app has only one window, so it is always top-level
                try {
                    window = new JothWindow(this, stApp, clientState.getPreferredWidth(), 
                                                 clientState.getPreferredHeight(), true, pixelScale);
                } catch (InstantiationException ex) {
                    throw new RuntimeException(ex);
                }

                // Both the app and the user want this window to be visible
                window.setVisibleApp(true);
                window.setVisibleUser(this, true);
                break;

            // The cell is no longer visible
            case DISK:
                window.setVisibleApp(false);
                window = null;
                break;
        }

        return ret;
    }
}
