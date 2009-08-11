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

package org.jdesktop.wonderland.modules.marbleous.client;

import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.marbleous.client.ui.UI;
import org.jdesktop.wonderland.modules.marbleous.common.Track;

/************************************
 * MarbleousMain: The Marble Roller Coaster main module.
 * @author deronj@dev.java.net
 */

public class MarbleousMain {
    
    /** The cell in which the game is displayed. */
    private App2DCell cell;
    /** The window which contains the track construction UI. */
    private UI ui;
    /** A single track. */
    private Track track;

    /**
     * Create a new instance of MarbleousMain.
     * @param cell The app cell in which the game is displayed.
     */
    public MarbleousMain (App2DCell cell) {

        this.cell = cell;

        track = new Track();
        ui = new UI(track);
    }

    /**
     * Controls the visibility of the game.
     */
    public void setVisible (boolean visible) {
        ui.setVisible(visible);
        // track.setVisible(visible);
    }
}

