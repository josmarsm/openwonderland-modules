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

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.screenshare.common.ScreenShareServerState;
import org.jdesktop.wonderland.modules.webcamviewer.common.cell.WebcamViewerCellServerState;

/**
 * The cell factory for the screen sharer
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@CellFactory
public class ScreenShareCellFactory implements CellFactorySPI {
    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle("org/jdesktop/wonderland/modules/screenshare/client/resources/Bundle");

    public String[] getExtensions() {
        return new String[]{};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        ScreenShareServerState state = new ScreenShareServerState();
        state.setName(BUNDLE.getString("Screen_Share"));
        state.setCameraURI("");
        
        return (T) state;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return BUNDLE.getString("Screen_Share");
    }

    /**
     * {@inheritDoc}
     */
    public Image getPreviewImage() {
        URL url = ScreenShareCellFactory.class.getResource("resources/WebCamApp128x128.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
