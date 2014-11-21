/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.server;

import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;

/**
 * Server-side plugin for the text chat feature.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class AudioRecordingServerPlugin implements ServerPlugin {

    public void initialize() {
        // Register a handler for text chat connections
        CommsManager cm = WonderlandContext.getCommsManager();
        cm.registerClientHandler(new AudioRecordingConnectionHandler());

        // Add a component to all avatars to enable the "Text Chat..." context
        // menu on the client
      //  CellManagerMO.getCellManager().registerAvatarCellComponent(TextChatAvatarComponentMO.class);
    }
}

