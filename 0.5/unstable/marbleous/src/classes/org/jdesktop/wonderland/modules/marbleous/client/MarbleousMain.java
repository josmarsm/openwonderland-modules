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
import org.jdesktop.wonderland.modules.marbleous.client.ui.MarbleousWindowConstruct;
import org.jdesktop.wonderland.modules.marbleous.client.ui.MarbleousWindowRun;

/************************************
 * MarbleousMain: The Marble Roller Coaster main module.
 * @author deronj@dev.java.net
 */

public class MarbleousMain {
    
    /** The cell in which the game is displayed. */
    private App2DCell cell;
    /** The window which contains the track construction UI. */
    private MarbleousWindowConstruct windowConstruct;
    /** The window which contains the run controls UI. */
    private MarbleousWindowRun windowRun;

    /**
     * Create a new instance of MarbleousMain.
     * @param cell The app cell in which the game is displayed.
     * @param windowConstruct The window which contains the track construction UI.
     * @param windowRun The window which contains the run controls UI.
     */
    public MarbleousMain (App2DCell cell, MarbleousWindowConstruct windowConstruct, 
                          MarbleousWindowRun windowRun) {
        this.cell = cell;
        this.windowConstruct = windowConstruct;
        this.windowRun = windowRun;
    }

    /**
     * Controls the visibility of the game.
     */
    public void setVisible (boolean visible) {
        windowConstruct.setVisible(visible);
        //windowRun.setVisible(visible);
    }
}

