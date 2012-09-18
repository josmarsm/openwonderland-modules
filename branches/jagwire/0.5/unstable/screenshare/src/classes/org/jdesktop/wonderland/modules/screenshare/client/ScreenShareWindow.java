/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.screenshare.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.webcamviewer.client.WebcamViewerControls;
import org.jdesktop.wonderland.modules.webcamviewer.client.WebcamViewerWindow;
import org.jdesktop.wonderland.modules.webcamviewer.client.cell.WebcamViewerCell;

/**
 * Screen sharing window.
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@ExperimentalAPI
public class ScreenShareWindow extends WebcamViewerWindow {
    private static final Logger LOGGER = Logger.getLogger(ScreenShareWindow.class.getName());

    private final ScreenShareCell cell;
    private ScreenShareControlPanel controls;

    public ScreenShareWindow(ScreenShareCell cell, App2D app, int width, int height,
            boolean topLevel, Vector2f pixelScale)
        throws InstantiationException 
    {
        super (cell, app, width, height, topLevel, pixelScale);

        this.cell = cell;
    }

    public void setSharing(boolean sharing) {
        if (controls != null) {
            controls.setSharing(sharing);
        }
    }

    @Override
    protected WebcamViewerControls createControls(WebcamViewerWindow window) {
        controls = new ScreenShareControlPanel(cell);
        controls.setSharing(cell.isSharing());
        return controls;
    }
}
