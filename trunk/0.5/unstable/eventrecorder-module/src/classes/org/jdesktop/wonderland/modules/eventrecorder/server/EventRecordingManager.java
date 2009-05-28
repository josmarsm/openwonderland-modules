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
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.modules.eventrecorder.server;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A service for recording events in Wonderland.  This service provides a set of
 * asynchronous mechanisms for creating and closing a changes file, and writing messages to that
 * file.  Callers will be notified if the file creation/closure succeeds or fails and also the result of
 * writing a message to the file.
 * @author Bernard Horan
 */
public interface EventRecordingManager {
    /**
     * Create a file to record changes. This method will contact
     * the remote web service to create a the file, and then call the
     * given listener with the result of that call.
     * @param tapeName the name of the recording for which to create a file
     * @param listener a changes file creation listener that will be notified of
     * the result of this call
     */
    public void createChangesFile(String tapeName, ChangesFileCreationListener listener);

    /**
     * Write a message to the changes file.  This method will use a web service to
     * wrap up the parameters into a message and then use another web service to write the
     * encoded message to the changes file.  Finally, the listener will be
     * notified with the results of the call.
     * @param tapeName the name of the recording for which the message is to be recorded
     * @param clientID the id of the client that sent the message
     * @param message the message that was sent and is to be recorded
     * @param listener a message recording listener that will be notified of the result of this call
     */
    public void recordMessage(String tapeName, WonderlandClientID clientID, CellMessage message, MessageRecordingListener listener);

    /**
     * Close the file that is used to record changes. This method contacts a web service
     * to close the file and then calls the listener with the result of that call.
     * @param tapeName the name of the recording that manages the changes file
     * @param listener a changes file close listener that will be notified with the result of this call
     */
    public void closeChangesFile(String tapeName, ChangesFileCloseListener listener);

    /**
     * A listener that will be notified of the success or failure of
     * creating a changes file.  Implementations of ChangesFileCreationListener
     * must be either a ManagedObject or Serializable.
     */
    public interface ChangesFileCreationListener {
        /**
         * Notification that a file has been created successfully
         */
        public void fileCreated();

        /**
         * Notification that changes file creation has failed.
         * @param reason a String describing the reason for failure
         * @param cause an exception that caused the failure.
         */
        public void fileCreationFailed(String reason, Throwable cause);

    }

    /**
     * A listener that will be notified of the success or failure of
     * closing a changes file.  Implementations of ChangesFileCreationListener
     * must be either a ManagedObject or Serializable.
     */
    public interface ChangesFileCloseListener {
        /**
         * Notification that a changes file has been closed successfully
         */
        public void fileClosed();

        /**
         * Notification that changes file closure has failed.
         * @param reason a String describing the reason for failure
         * @param cause an exception that caused the failure.
         */
        public void fileClosureFailed(String reason, Throwable cause);

    }

    /**
     * A listener that will be notified of the result of recording a message
     * to a changes file.  Implementations of MessageRecordingListener must
     * be either a ManagedObject or Serializable
     */
    public interface MessageRecordingListener {
        /**
         * Notification of the result of recording a message
         * @param result the result of recording a message
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
