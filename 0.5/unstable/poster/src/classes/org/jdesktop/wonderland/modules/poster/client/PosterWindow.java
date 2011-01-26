/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.poster.client;

import com.jme.math.Vector2f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D;

/**
 *
 * The window for the poster app.
 *
 */
@ExperimentalAPI
public class PosterWindow extends WindowSwing {
    private static final Logger LOGGER = Logger.getLogger(PosterWindow.class.getName());

    /** The cell in which this window is displayed. */
    private final PosterCell cell;

    /** The panel displayed within this window. */
    private PosterPanel posterPanel;

    /** The poster node to render the label */
    private final PosterNode posterNode;

    /**
     * Create a new instance of PollWindow.
     *
     * @param cell The cell in which this window is displayed.
     * @param app The app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param decorated Whether the window is decorated with a frame.
     * @param pixelScale The size of the window pixels.
     * @throws InstantiationException
     */
    public PosterWindow(final PosterCell cell, App2D app, int width,
            int height, boolean decorated, Vector2f pixelScale)
            throws InstantiationException {
        super(app, width, height, decorated, pixelScale);
        this.cell = cell;

        setTitle("Poster");
        View2D view = getView(cell);
        posterNode = new PosterNode(view);
        view.setGeometryNode(posterNode);



        try {
            SwingUtilities.invokeAndWait(new Runnable () {
                public void run () {
                    // This must be invoked on the AWT Event Dispatch Thread
                    posterPanel = new PosterPanel(cell);
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        // Parent to Wonderland main window for proper focus handling
        JmeClientMain.getFrame().getCanvas3DPanel().add(posterPanel);

        setComponent(posterPanel);
    }

    void updateLabel() {
        posterNode.setBillboard(cell.getBillboardMode());
        posterPanel.updateLabel();
    }

}
