/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.modules.eventrecorder.server;

import java.util.Set;
import org.jdesktop.wonderland.server.eventrecorder.*;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.modules.eventrecorder.server.ChangesFile;
import org.jdesktop.wonderland.common.wfs.WorldRoot;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author Bernard Horan
 */
public interface EventRecordingManager {
    /**
     *
     * @param tapeName
     * @param listener 
     */
    public void createChangesFile(String tapeName, ChangesFileCreationListener listener);

    /**
     *
     * @param tapeName
     * @param clientID
     * @param message
     */
    public void recordMessage(String tapeName, WonderlandClientID clientID, CellMessage message, MessageRecordingListener listener);

    /**
     *
     * @param tapeName
     * @param listener
     */
    public void closeChangesFile(String tapeName, ChangesFileCloseListener listener);

    /**
     * A listener that will be notified of the success or failure of
     * creating a changes file.  Implementations of ChangesFileCreationListener
     * must be either a ManagedObject or Serializable.
     */
    public interface ChangesFileCreationListener {
        /**
         * Notification that a snapshot has been created successfully
         * @param changesFile the changes file that was closed
         */
        public void fileCreated(ChangesFile changesFile);

        /**
         * Notification that changes file creation has failed.
         * @param reason a String describing the reason for failure
         * @param cause an exception that caused the failure.
         */
        public void fileCreationFailed(String reason, Throwable cause);

    }

    /**
     * A listener that will be notified of the success or failure of
     * creating a snapshot.  Implementations of ChangesFileCreationListener
     * must be either a ManagedObject or Serializable.
     */
    public interface ChangesFileCloseListener {
        /**
         * Notification that a changes file has been closed successfully
         * @param cFile the changes file that was closed
         */
        public void fileClosed(ChangesFile cFile);

        /**
         * Notification that snapshot creation has failed.
         * @param reason a String describing the reason for failure
         * @param cause an exception that caused the failure.
         */
        public void fileClosureFailed(String reason, Throwable cause);

    }

    /**
     * A listener that will be notified of the result of recording a message
     * to c changes file.  Implementations of MessageRecordingListener must
     * be either a ManagedObject or Serializable
     */
    public interface MessageRecordingListener {
        /**
         * Notification of the result of recording a message
         * @param result
         */
        public void messageRecordingResult(MessageRecordingResult result);
    }

    /**
     * The result of recording a message
     */
    public interface MessageRecordingResult {
        /**
         * Whether or not the recording was successful
         * @return true if the recording was successful, or false if not
         */
        public boolean isSuccess();

        /**
         * The id of the message that was recorded
         * @return the id of the message
         */
        public MessageID getMessageID();

        /**
         * If the recording failed, return the reason
         * @return the reason for failure, or null
         */
        public String getFailureReason();

        /**
         * If the recording failed, return the root cause exception
         * @return the root cause of the failure, or null
         */
        public Throwable getFailureCause();
    }


}
