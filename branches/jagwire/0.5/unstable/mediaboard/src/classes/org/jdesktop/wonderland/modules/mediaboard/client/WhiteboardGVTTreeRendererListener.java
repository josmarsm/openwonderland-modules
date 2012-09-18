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
package org.jdesktop.wonderland.modules.mediaboard.client;

import java.util.logging.Logger;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;

/**
 * Simple class to implement GVTTreeRendererListener interface.<br>
 * Generally does nothing, except that it calls repaintCanvas() on the whiteboardApp
 * when the GVT rendering is complete.
 * @author bh37721
 */
public class WhiteboardGVTTreeRendererListener implements GVTTreeRendererListener {

    private static final Logger logger =
            Logger.getLogger(WhiteboardGVTTreeRendererListener.class.getName());
    private WhiteboardWindow whiteboardWindow;

    WhiteboardGVTTreeRendererListener(WhiteboardWindow whiteboardWindow) {
        this.whiteboardWindow = whiteboardWindow;
    }

    /**
     * Called when a rendering is in its preparing phase.
     */
    public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
        logger.fine("whiteboard: rendering prepare: " + e);
    }

    /**
     * Called when a rendering started.
     */
    public void gvtRenderingStarted(GVTTreeRendererEvent e) {
        logger.fine("whiteboard: rendering started: " + e);
    }

    /**
     * Called when a rendering was completed.
     */
    public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
        logger.fine("whiteboard: rendering completed: " + e);
        whiteboardWindow.repaintCanvas();
    }

    /**
     * Called when a rendering was cancelled.
     */
    public void gvtRenderingCancelled(GVTTreeRendererEvent e) {
        logger.fine("whiteboard: rendering canceled: " + e);
    }

    /**
     * Called when a rendering failed.
     */
    public void gvtRenderingFailed(GVTTreeRendererEvent e) {
        logger.fine("whiteboard: rendering failed: " + e);
    }
}
