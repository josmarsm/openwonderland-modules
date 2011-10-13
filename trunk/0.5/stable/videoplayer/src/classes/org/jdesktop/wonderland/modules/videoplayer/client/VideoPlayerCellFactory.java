/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
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
/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.videoplayer.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.modules.videoplayer.common.cell.VideoPlayerCellServerState;

/**
 * The cell factory for the Video Player.
 * 
 * @author nsimpson
 */
@CellFactory
public class VideoPlayerCellFactory implements CellFactorySPI {
    private static final String[] EXTENSIONS = new String[] { 
        "mov", "mp4", "avi", "ogg", "flv", "m4a", "3gp", "3g2", "mj2", "m4v",
        "wmv", "mpeg"
    };

    /**
     * {@inheritDoc}
     */
    public String[] getExtensions() {
        List<String> out = new ArrayList<String>(EXTENSIONS.length * 2);

        for (String ext : EXTENSIONS) {
            out.add(ext.toLowerCase());
            out.add(ext.toUpperCase());
        }
        
        return out.toArray(new String[out.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        VideoPlayerCellServerState state = new VideoPlayerCellServerState();
        state.setName(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle").getString("VIDEO"));

        // Look for the content-uri field and set if so
        if (props != null) {
            String uri = props.getProperty("content-uri");
            if (uri != null) {
                state.setMediaURI(uri);
            }
        }

        return (T) state;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle").getString("VIDEO_PLAYER");
    }

    /**
     * {@inheritDoc}
     */
    public Image getPreviewImage() {
        URL url = VideoPlayerCellFactory.class.getResource("resources/VideoPlayerApp128x128.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
