/*
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
package org.jdesktop.wonderland.modules.whiteboard.client;

import com.jme.math.Vector2f;
import java.awt.Component;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.modules.appbase.client.App;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;

/**
 *
 * @author nsimpson
 */
public class WhiteboardControlWindow extends WindowSwing {
    private static final Logger logger = Logger.getLogger(WhiteboardControlWindow.class.getName());

    /**
     * Create a new instance of WhiteboardControlWindow
     *
     * @param app The app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param topLevel Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     */
    public WhiteboardControlWindow(final App app, int width, int height, boolean topLevel, Vector2f pixelScale)
            throws InstantiationException {
        super(app, width, height, topLevel, pixelScale);
        setSize(width, height);
        initializeSurface();
        setTitle("Whiteboard Control Window");
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setComponent(Component component) {
        JmeClientMain.getFrame().getCanvas3DPanel().add(component);
        super.setComponent(component);
    }
}
