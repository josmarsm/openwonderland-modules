/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.common;


import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.comms.ConnectionType;

/**
 * The ConnectionType of the text chat connection
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@InternalAPI
public class AudioRecordingConnectionType extends ConnectionType {
    /** the client type for the cell client */
    public static final ConnectionType CLIENT_TYPE =
            new AudioRecordingConnectionType("__StandardSheetClient");

    private AudioRecordingConnectionType(String type) {
        super (type);
    }
}

