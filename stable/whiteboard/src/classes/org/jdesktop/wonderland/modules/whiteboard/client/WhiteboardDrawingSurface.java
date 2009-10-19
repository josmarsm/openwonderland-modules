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
package org.jdesktop.wonderland.modules.whiteboard.client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.logging.Logger;
import org.apache.batik.swing.gvt.Overlay;
import org.jdesktop.wonderland.modules.appbase.client.DrawingSurfaceBufferedImage;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * @author paulby,deronj
 */
@ExperimentalAPI
public class WhiteboardDrawingSurface extends DrawingSurfaceBufferedImage {

    private static final Logger logger = Logger.getLogger(WhiteboardDrawingSurface.class.getName());

    public WhiteboardDrawingSurface(int width, int height) {
        setSize(width, height);
    }

    @Override
    protected void initSurface(Graphics2D g) {
    }

    public void addToDisplay(final Overlay overlay) {
        // TODO: temporary
        final DrawingSurfaceBufferedImage.DirtyTrackingGraphics graphics =
                (DrawingSurfaceBufferedImage.DirtyTrackingGraphics) getGraphics();

        graphics.executeAtomic(new Runnable() {

            public void run() {
                overlay.paint((Graphics2D) getGraphics());

                // TODO: must damage everything
                graphics.addDirtyRectangle(0, 0, getWidth(), getHeight());
            }
        });
    }

    public void repaint() {
        // TODO: temporary
        final DrawingSurfaceBufferedImage.DirtyTrackingGraphics graphics =
                (DrawingSurfaceBufferedImage.DirtyTrackingGraphics) getGraphics();

        graphics.executeAtomic(new Runnable() {

            public void run() {
                // TODO: must damage everything
                graphics.addDirtyRectangle(0, 0, getWidth(), getHeight());
            }
        });
    }

    public void erase() {
        // TODO: temporary
        final DrawingSurfaceBufferedImage.DirtyTrackingGraphics graphics =
                (DrawingSurfaceBufferedImage.DirtyTrackingGraphics) getGraphics();

        graphics.executeAtomic(new Runnable() {

            public void run() {
                graphics.setBackground(Color.WHITE);
                graphics.clearRect(0, 0, getWidth(), getHeight());

                // TODO: must damage everything
                graphics.addDirtyRectangle(0, 0, getWidth(), getHeight());
            }
        });
    }
}
