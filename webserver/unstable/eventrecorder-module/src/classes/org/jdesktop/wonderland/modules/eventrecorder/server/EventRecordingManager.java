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


package org.jdesktop.wonderland.modules.eventrecorder.server;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A service for recording position data and events/changes in Wonderland.  This service provides a set of
 * asynchronous mechanisms for recording position data in a file, creating and closing a changes file, and writing changes to that
 * file.  Callers will be notified if the recording of position or file creation/closure succeeds or fails and also the result of
 * writing a change to the file.
 * @author Bernard Horan
 */
public interface EventRecordingManager {

    /**
     * Record the position of a cell on a file. This method will contact the
     * remote web service to create the file, write the position data to it
     * and then call the given listner with the result of that call.
     * @param tapeName the name of the recording for which to record the position
     * @param positionState the position data, including translation, rotation, scale and bounds
     * @param listener a position recording listener that will be notfified of the result of this call
     */
    public void recordPosition(String tapeName, PositionComponentServerState positionState, PositionRecordedListener listener);
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
     * Record the details of a loaded cell onto the changes file.
     * @param tapeName the name of the recording to which the change should be recorded
     * @param cellID the id of the cell that has been loaded
     * @param parentID the id of the parent cell
     * @param listener a loaded cell recording listener that will be notified of the result of this call
     */
    public void recordLoadedCell(String tapeName, CellID cellID, CellID parentID, LoadedCellRecordingListener listener);

    
    /**
     * Record the details of an unloaded cell onto the changes file.
     * @param tapeName the name of the recording to which the change should be recorded
     * @param cellID the id of the cell that has been unloaded
     * @param listener a loaded cell recording listener that will be notified of the result of this call
     */
    public void recordUnloadedCell(String tapeName, CellID cellID, UnloadedCellsRecordingListener listener);

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
     * Write a message to the changes file.  This method will use a web service to
     * wrap up the parameters into a message and then use another web service to write the
     * encoded message to the changes file.  Finally, the listener will be
     * notified with the results of the call.
     * @param tapeName the name of the recording for which the message is to be recorded
     * @param messageID the id of the message
     * @param metadata the actual metadata, can be any string
     * @param listener a message recording listener that will be notified of the result of this call
     */
    public void recordMetadata(String tapeName, MessageID messageID, String metadata, MetadataRecordingListener listener);


    /**
     * Close the file that is used to record changes. This method contacts a web service
     * to close the file and then calls the listener with the result of that call.
     * @param tapeName the name of the recording that manages the changes file
     * @param listener a changes file close listener that will be notified with the result of this call
     */
    public void closeChangesFile(String tapeName, ChangesFileCloseListener listener);

    /**
     * A listener that will be notified when the position of the event recorder
     * has been recorded. Implementations of PositionRecordedListener
     * must be either a ManagedObject or Serializable.
     */
    public interface PositionRecordedListener {

        /**
         * Notification that a position has been recorded
         * @param exception if non-null the recording of the position has failed
         */
        public void recordPositionResult(Exception exception);
        
    }

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
        public void messageRecordingResult(ChangeRecordingResult result);
    }

    /**
     * A listener that will be notified of the result of recording metadata
     * to a changes file.  Implementations of MetadataRecordingListener must
     * be either a ManagedObject or Serializable
     */
    public interface MetadataRecordingListener {
        /**
         * Notification of the result of recording a message
         * @param result the result of recording a message
         */
        public void metadataRecordingResult(ChangeRecordingResult result);
    }

    /**
     * The result of recording a message or metadata to a changes file
     */
    public interface ChangeRecordingResult {
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

    /**
     * A listener that will be notified with the result of recording that
     * a cell has been loaded.
     */
    public interface LoadedCellRecordingListener {

        /**
         * The result of recording a change in which a cell was loaded
         * @param cellID the cell for which the change was recorded
         * @param exception any exception if the recording of the change failed
         */
        public void recordLoadedCellResult(CellID cellID, Exception exception);
        
    }

    /**
     * A listener that will be notified with the result of recording that
     * a cell has been unloaded.
     */
    public interface UnloadedCellsRecordingListener {

        /**
         * The result of recording a change in which a cell was unloaded
         * @param cellID the cell for which the change was recorded
         * @param exception any exception if the recording of the change failed
         */
        public void recordUnloadedCellResult(CellID cellID, Exception exception);


    }

    


}
