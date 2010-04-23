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
package org.jdesktop.wonderland.modules.webcamviewer.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.modules.webcamviewer.common.cell.WebcamViewerCellServerState;

/**
 * The cell factory for the Webcam Viewer.
 * 
 * @author nsimpson
 */
@CellFactory
public class WebcamViewerCellFactory implements CellFactorySPI {

    /**
     * {@inheritDoc}
     */
    public String[] getExtensions() {
        return new String[]{};
    }

    /**
     * {@inheritDoc}
     */
    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        WebcamViewerCellServerState state = new WebcamViewerCellServerState();
        state.setName(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcamviewer/client/resources/Bundle").getString("WEBCAM"));

        // Look for the content-uri field and set if so
        if (props != null) {
            String uri = props.getProperty("content-uri");
            if (uri != null) {
                state.setCameraURI(uri);
            }
        }

        return (T) state;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcamviewer/client/resources/Bundle").getString("WEBCAM_VIEWER");
    }

    /**
     * {@inheritDoc}
     */
    public Image getPreviewImage() {
        URL url = WebcamViewerCellFactory.class.getResource("resources/WebCamApp128x128.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
