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

package org.jdesktop.wonderland.modules.eventplayer.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.eventplayer.common.EventPlayerCellServerState;

/**
 * The cell factory for the event player.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@CellFactory
public class EventPlayerCellFactory implements CellFactorySPI {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/eventplayer/client/resources/Bundle");

    public String[] getExtensions() {
        return new String[] {};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        CellServerState state =  new EventPlayerCellServerState();
        state.setName(bundle.getString("EVENT_PLAYER"));
        return (T) state;
    }

    

    public String getDisplayName() {
        return bundle.getString("EVENT_PLAYER");
    }

    public Image getPreviewImage() {
        URL url = EventPlayerCellFactory.class.getResource("resources/eventplayer_preview.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
