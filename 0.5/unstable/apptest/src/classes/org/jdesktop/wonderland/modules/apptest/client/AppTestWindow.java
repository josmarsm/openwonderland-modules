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
package org.jdesktop.wonderland.modules.apptest.client;

import com.jme.math.Vector2f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import org.jdesktop.wonderland.modules.apptest.client.cell.AppTestCell;

/**
 *
 * The window for the App Test.
 *
 * @author deronj
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
@ExperimentalAPI
public class AppTestWindow extends WindowSwing {

    /** The logger used by this class. */
    private static final Logger logger =
            Logger.getLogger(AppTestWindow.class.getName());
    /** The cell in which this window is displayed. */
    private AppTestCell cell;

    /**
     * Create a new instance of AppTestWindow.
     *
     * @param cell The cell in which this window is displayed.
     * @param app The app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param decorated Whether the window is decorated with a frame.
     * @param pixelScale The size of the window pixels.
     */
    public AppTestWindow(AppTestCell cell, App2D app, int width,
            int height, boolean decorated, Vector2f pixelScale)
            throws InstantiationException {
        super(app, width, height, decorated, pixelScale);
        this.cell = cell;

        setTitle("App Test");

        TestPanel testPanel = new TestPanel();

        // Parent to Wonderland main window for proper focus handling
        JmeClientMain.getFrame().getCanvas3DPanel().add(testPanel);

        setComponent(testPanel);
    }
}
