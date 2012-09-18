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

package uk.ac.essex.demo.eventrecorder.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.eventrecorder.EventRecorder;

/**
 * Demo implementation of EventRecorder interface.
 * Report messages and metadata to logger
 * @author Bernard Horan
 */
public class EventRecorderImpl implements EventRecorder {
    private static final Logger logger = Logger.getLogger(EventRecorderImpl.class.getName());

    private boolean isRecording = false;

    public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
        logger.warning("sender: " + sender + ", clientID: " + clientID + ", message: " + message);
    }

    public boolean isRecording() {
        return isRecording;
    }

    public String getName() {
        return "Demo Event Recorder";
    }

    public void recordMetadata(CellMessage message, String metadata) {
        logger.warning("metadata: " + metadata +  ", message: " + message);
    }

    void startRecording() {
        logger.warning("XXX Starting demo recorder");
        isRecording = true;
    }

}
