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
package org.jdesktop.wonderland.modules.tightvncviewer.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.modules.tightvncviewer.common.cell.TightVNCViewerCellServerState;

/**
 * The cell factory for the VNC viewer.
 * 
 * @author nsimpson
 */
@CellFactory
public class TightVNCViewerCellFactory implements CellFactorySPI {

    /**
     * {@inheritDoc}
     */
    public String[] getExtensions() {
        return new String[]{"vnc", "VNC"};
    }

    /**
     * {@inheritDoc}
     */
    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        TightVNCViewerCellServerState state = new TightVNCViewerCellServerState();
        state.setName(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/tightvncviewer/client/resources/Bundle").getString("VNC"));

        // Look for the content-uri field and set if so
        if (props != null) {
            String uri = props.getProperty("content-uri");
            if (uri != null) {
                state.setVNCServer(uri);
            }
        }

        return (T) state;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/tightvncviewer/client/resources/Bundle").getString("VNC_VIEWER");
    }

    /**
     * {@inheritDoc}
     */
    public Image getPreviewImage() {
        URL url = TightVNCViewerCellFactory.class.getResource("resources/TightVNCViewerApp128x128.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
